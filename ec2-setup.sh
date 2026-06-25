#!/bin/bash
# EC2 Ubuntu 최초 1회 실행 스크립트
# 사용: bash ec2-setup.sh

set -e

echo "=== 1. 패키지 업데이트 ==="
sudo apt-get update -y

echo "=== 2. Java 21 설치 ==="
sudo apt-get install -y openjdk-21-jdk
java -version

echo "=== 3. Docker 설치 ==="
sudo apt-get install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
  | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update -y
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo usermod -aG docker ubuntu
echo "✅ Docker 설치 완료 (재로그인 필요)"

echo "=== 4. Nginx 설치 ==="
sudo apt-get install -y nginx

echo "=== 5. Nginx 설정 ==="
sudo tee /etc/nginx/sites-available/yamyam > /dev/null << 'NGINX'
server {
    listen 80;
    server_name _;

    root /home/ubuntu/frontend;
    index index.html;

    # Vue SPA fallback
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Spring API 프록시
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_read_timeout 60s;
    }

    # 영상 파일 프록시
    location /videos/ {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header Range $http_range;
        proxy_set_header If-Range $http_if_range;
        proxy_buffering on;
        proxy_request_buffering on;
        proxy_read_timeout 120s;
        add_header Cache-Control "public, max-age=2592000" always;
        add_header Accept-Ranges bytes always;
    }

    # WebSocket 프록시
    location /ws/ {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_read_timeout 3600s;
    }
}
NGINX

sudo ln -sf /etc/nginx/sites-available/yamyam /etc/nginx/sites-enabled/yamyam
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t && sudo systemctl reload nginx
echo "✅ Nginx 설정 완료"

echo "=== 6. 디렉토리 생성 ==="
mkdir -p /home/ubuntu/app/logs
mkdir -p /home/ubuntu/frontend
mkdir -p /home/ubuntu/uploads/videos

echo "=== 7. Spring Backend systemd 서비스 등록 ==="
sudo tee /etc/systemd/system/app.service > /dev/null << 'SERVICE'
[Unit]
Description=YamYam Spring Backend
Requires=docker.service
After=network-online.target docker.service
Wants=network-online.target

[Service]
User=ubuntu
WorkingDirectory=/home/ubuntu/app
EnvironmentFile=/home/ubuntu/app/.env
Environment="JAVA_OPTS=-Xms256m -Xmx384m -XX:+UseG1GC"
ExecStart=/usr/bin/java $JAVA_OPTS -Dspring.profiles.active=prod -jar /home/ubuntu/app/app.jar
SuccessExitStatus=143
Restart=always
RestartSec=5
StandardOutput=append:/home/ubuntu/app/logs/app.log
StandardError=append:/home/ubuntu/app/logs/app.log

[Install]
WantedBy=multi-user.target
SERVICE
sudo systemctl daemon-reload
sudo systemctl enable app

echo ""
echo "✅ EC2 셋업 완료!"
echo ""
echo "다음 단계:"
echo "  1. 재로그인 (Docker 권한 반영)"
echo "  2. GitHub Secrets 등록:"
echo "     - EC2_SSH_KEY : yamyam-secret.pem 파일 전체 내용"
echo "     - EC2_HOST    : ec2-13-124-245-69.ap-northeast-2.compute.amazonaws.com"
echo "  3. publish 브랜치에 push → 자동 배포"
