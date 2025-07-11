#!/bin/bash

LOG_FILE=/home/ec2-user/action/spring-deploy.log # 전체 배포 흐름 기록
BUILD_JAR=$(ls /home/ec2-user/action/build/libs/*.jar)
JAR_NAME=$(basename "$BUILD_JAR")
DEPLOY_PATH=/home/ec2-user/action/
UPSTREAM_CONF="/etc/nginx/conf.d/upstream.conf"

# 1. 현재 nginx가 사용하는 포트 감지 (포트 스위칭 방식)
CURRENT_PORT=$(grep -oP '(?<=proxy_pass http://127.0.0.1:)\d+' "$UPSTREAM_CONF")
if [ "$CURRENT_PORT" = "8081" ]; then
  IDLE_PORT=8082
else
  IDLE_PORT=8081
fi

log_success() {
  echo "[SUCCESS] $1" >> "$LOG_FILE"
}

log_fail() {
  echo "[FAIL] $1" >> "$LOG_FILE"
}

echo "========== DEPLOY START: $(date) ==========" >> "$LOG_FILE"
echo "## build file name : $JAR_NAME" >> "$LOG_FILE"

echo "## copy build file" >> "$LOG_FILE"
cp "$BUILD_JAR" "$DEPLOY_PATH"

DEPLOY_JAR="$DEPLOY_PATH$JAR_NAME"
echo "## deploy JAR file to port $IDLE_PORT" >> "$LOG_FILE"
export SPRING_PROFILES_ACTIVE=dev

# IDLE 포트가 점유 중이면 강제 종료
IDLE_PID=$(lsof -ti tcp:$IDLE_PORT)
if [ -n "$IDLE_PID" ]; then
  echo "[WARN] IDLE 포트 $IDLE_PORT 점유 중 - 강제 종료 시도 (PID: $IDLE_PID)" >> "$LOG_FILE"
  kill -9 $IDLE_PID
  sleep 1
else
  echo "[INFO] IDLE 포트 $IDLE_PORT 사용 중 아님 - 문제 없음" >> "$LOG_FILE"

nohup java -Xms256m -Xmx512m -jar "$DEPLOY_JAR" --spring.profiles.active=dev --server.port=$IDLE_PORT >> "/home/ec2-user/action/spring-deploy_$IDLE_PORT.log" 2>&1 &
# 표준 출력 로그 기록(해당 포트의 애플리케이션 로그) / 표준 에러 로그 기록

# 2. 새 포트로 헬스체크
HEALTH_CHECK_URL="http://127.0.0.1:$IDLE_PORT/actuator/health"
MAX_RETRIES=25
RETRY_INTERVAL=1
RETRY_COUNT=0
SUCCESS=false
CHECK_DISK=false
APP_START_TIME=$(date +%s)

echo "## 헬스 체크 시작: $HEALTH_CHECK_URL" >> "$LOG_FILE"

while [ "$RETRY_COUNT" -lt "$MAX_RETRIES" ]; do
  if curl -s "$HEALTH_CHECK_URL" | grep -q '"status":"UP"'; then
    NEW_PID=$(pgrep -f "server.port=$IDLE_PORT")
    END_TIME=$(date +%s) # 헬스체크 성공 시각 기록

    log_success "---> 새 애플리케이션 실행 성공 (PID: $NEW_PID, PORT: $IDLE_PORT)"
    echo "## [INFO] 애플리케이션 실행 완료 시각: $(date -d "@$END_TIME")" >> "$LOG_FILE"
    STARTUP_TIME=$((END_TIME - APP_START_TIME))
    echo "[INFO] 애플리케이션 실행 소요 시간: ${STARTUP_TIME}초" >> "$LOG_FILE"
    SUCCESS=true
    break
  else
    echo "## 헬스 체크 대기 중... (${RETRY_COUNT}s)" >> "$LOG_FILE"
    sleep "$RETRY_INTERVAL"
    RETRY_COUNT=$((RETRY_COUNT + 1))
  fi
done

if [ "$SUCCESS" = false ]; then
  END_TIME=$(date +%s)
  log_fail "---> 애플리케이션 헬스 체크 실패 (최대 ${MAX_RETRIES}s 대기)"
  STARTUP_TIME=$((END_TIME - APP_START_TIME))
  echo "[INFO] 애플리케이션 실행 소요 시간: ${STARTUP_TIME}초 (헬스체크는 실패했으나 기록)" >> "$LOG_FILE"
  CHECK_DISK=true
else
  # 3.Nginx 포트 스위칭
  echo "## Nginx upstream.conf 변경: $IDLE_PORT" >> "$LOG_FILE"
  cat > "$UPSTREAM_CONF" <<EOF
proxy_pass http://127.0.0.1:$IDLE_PORT;
proxy_set_header Host \$host;
proxy_set_header X-Real-IP \$remote_addr;
proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
proxy_set_header X-Forwarded-Proto \$scheme;
EOF

  nginx -s reload
  log_success "Nginx 포트 스위칭 완료 (이제 $IDLE_PORT를 바라봄)"

  # 4. 기존 포트 앱 종료
  echo "## 기존 애플리케이션 종료 (PORT: $CURRENT_PORT)" >> "$LOG_FILE"
    OLD_PID=$(pgrep -f "server.port=$CURRENT_PORT")
    if [ -n "$OLD_PID" ]; then
      kill -15 "$OLD_PID"
      sleep 5
      if ps -p $OLD_PID > /dev/null; then
        # 여전히 살아 있으면 강제 종료
        echo "[WARN] 기존 애플리케이션 PID $OLD_PID 종료 실패 - 강제 종료 시도" >> "$LOG_FILE"
        kill -9 "$OLD_PID"
        echo "kill -9 $OLD_PID 완료" >> "$LOG_FILE"
      else
        echo "kill -15 $OLD_PID 완료" >> "$LOG_FILE"
      fi
    else
      echo "기존 앱 프로세스 찾지 못함 (이미 종료되었을 가능성 있음)" >> "$LOG_FILE"
    fi
fi

# 5. 디스크 사용량 확인
echo "" >> "$LOG_FILE"
echo "## 디스크 사용량 확인" >> "$LOG_FILE"
DISK_USAGE=$(df / | awk 'NR==2 {print $5}' | sed 's/%//')
if [ "$DISK_USAGE" -ge 85 ]; then
  echo "[WARN] 디스크 사용량 ${DISK_USAGE}% - 로그 정리 시작" >> "$LOG_FILE"

  LOG_DIR="/home/ec2-user/action/logs"
  MAX_SIZE_MB=100

  for FILE in "$LOG_DIR"/*.log; do
    if [ -f "$FILE" ]; then
      SIZE_MB=$(du -m "$FILE" | cut -f1)
      if [ "$SIZE_MB" -ge "$MAX_SIZE_MB" ]; then
        echo "  > $FILE (${SIZE_MB}MB) - 앞 절반 삭제" >> "$LOG_FILE"
        TEMP_FILE="${FILE}.tmp"
        LINE_COUNT=$(wc -l < "$FILE")
        TAIL_START=$((LINE_COUNT / 2))
        tail -n "$TAIL_START" "$FILE" > "$TEMP_FILE" && mv "$TEMP_FILE" "$FILE"
      fi
    fi
  done
else
  echo "[INFO] 디스크 사용량 ${DISK_USAGE}% - 정리 불필요" >> "$LOG_FILE"
fi

if [ "$CHECK_DISK" = true ]; then
  exit 1
fi
