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

echo "## current pid" >> "$LOG_FILE"
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
nohup java -jar "$DEPLOY_JAR" >> /home/ec2-user/spring-deploy.log 2> /home/ec2-user/action/spring-deploy_err.log &

sleep 2

NEW_PID=$(pgrep -f "$JAR_NAME")
if [ -z "$NEW_PID" ]; then
  log_fail "----> 애플리케이션 실행 실패"
  exit 1
else
  log_success "----> 애플리케이션 실행 성공 (PID: $NEW_PID)"
fi

echo "" >> "$LOG_FILE"
