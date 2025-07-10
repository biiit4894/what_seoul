#!/bin/bash

LOG_FILE=/home/ec2-user/action/spring-deploy.log
BUILD_JAR=$(ls /home/ec2-user/action/build/libs/*.jar)
JAR_NAME=$(basename "$BUILD_JAR")

log_success() {
  echo "[SUCCESS] $1" >> "$LOG_FILE"
}

log_fail() {
  echo "[FAIL] $1" >> "$LOG_FILE"
}

echo "========== DEPLOY START: $(date) ==========" >> "$LOG_FILE"

echo "## build file name : $JAR_NAME" >> "$LOG_FILE"

echo "## copy build file" >> "$LOG_FILE"
DEPLOY_PATH=/home/ec2-user/action/
cp "$BUILD_JAR" "$DEPLOY_PATH"

echo "## find current pid" >> "$LOG_FILE"
CURRENT_PID=$(pgrep -f "$JAR_NAME")

if [ -z "$CURRENT_PID" ]; then
  echo "## 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다." >> "$LOG_FILE"
else
  echo "## kill -15 $CURRENT_PID" >> "$LOG_FILE"
  kill -15 "$CURRENT_PID"
  sleep 5
fi

DEPLOY_JAR="$DEPLOY_PATH$JAR_NAME"
echo "## deploy JAR file" >> "$LOG_FILE"
export SPRING_PROFILES_ACTIVE=dev
nohup java -Xms256m -Xmx512m -jar "$DEPLOY_JAR" --spring.profiles.active=dev >> /home/ec2-user/action/spring-deploy.log 2> /home/ec2-user/action/spring-deploy_err.log &

# 애플리케이션 기동 후 헬스체크 대기
HEALTH_CHECK_URL="http://127.0.0.1:8089/actuator/health"
MAX_RETRIES=10
RETRY_INTERVAL=1
RETRY_COUNT=0
SUCCESS=false

echo "## 애플리케이션 헬스 체크 시작" >> "$LOG_FILE"

while [ "$RETRY_COUNT" -lt "$MAX_RETRIES" ]; do
  if curl -s "$HEALTH_CHECK_URL" | grep -q '"status":"UP"'; then
    NEW_PID=$(pgrep -f "$JAR_NAME")
    log_success "----> 애플리케이션 실행 성공 (PID: $NEW_PID)"
    SUCCESS=true
    break
  else
    echo "## 헬스 체크 대기 중... (${RETRY_COUNT}s)" >> "$LOG_FILE"
    sleep "$RETRY_INTERVAL"
    RETRY_COUNT=$((RETRY_COUNT + 1))
  fi
done

if [ "$SUCCESS" = false ]; then
  log_fail "----> 애플리케이션 헬스 체크 실패 (최대 ${MAX_RETRIES}s 대기)"
  exit 1
fi


echo "" >> "$LOG_FILE"

echo "## 디스크 사용량 확인" >> "$LOG_FILE"
DISK_USAGE=$(df / | awk 'NR==2 {print $5}' | sed 's/%//') # 루트 디렉토리 사용률 (%)
if [ "$DISK_USAGE" -ge 85 ]; then
  echo "[WARN] 디스크 사용량이 ${DISK_USAGE}% 입니다. 오래된 로그를 정리합니다." >> "$LOG_FILE"

  LOG_DIR="/home/ec2-user/action/logs"
  MAX_SIZE_MB=100

  for FILE in "$LOG_DIR"/*.log; do
    if [ -f "$FILE" ]; then
      SIZE_MB=$(du -m "$FILE" | cut -f1)
      if [ "$SIZE_MB" -ge "$MAX_SIZE_MB" ]; then
        echo "  > $FILE (크기: ${SIZE_MB}MB) - 앞 절반 삭제" >> "$LOG_FILE"
        TEMP_FILE="${FILE}.tmp"
        LINE_COUNT=$(wc -l < "$FILE")
        TAIL_START=$((LINE_COUNT / 2))
        tail -n "$TAIL_START" "$FILE" > "$TEMP_FILE" && mv "$TEMP_FILE" "$FILE"
      fi
    fi
  done
else
  echo "디스크 사용량 ${DISK_USAGE}%, 로그 정리는 필요하지 않음" >> "$LOG_FILE"
fi

