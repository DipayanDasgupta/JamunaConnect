#!/bin/bash
# JamunaConnect EC2 bootstrap: Java 21, PostgreSQL with pg_trgm, systemd unit.
# Secrets are generated on the instance and written to /opt/jamuna/app.env (0600).
set -euxo pipefail

export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get install -y openjdk-21-jre-headless postgresql postgresql-contrib curl

systemctl enable --now postgresql

DB_PASS="$(openssl rand -hex 16)"
sudo -u postgres psql -v ON_ERROR_STOP=1 <<SQL
CREATE USER jamuna WITH PASSWORD '${DB_PASS}';
CREATE DATABASE jamunaconnect OWNER jamuna;
SQL
sudo -u postgres psql -d jamunaconnect -c "CREATE EXTENSION IF NOT EXISTS pg_trgm;"

id -u jamuna >/dev/null 2>&1 || useradd --system --home /opt/jamuna --shell /usr/sbin/nologin jamuna
mkdir -p /opt/jamuna
chown jamuna:jamuna /opt/jamuna

cat > /opt/jamuna/app.env <<EOF
DATABASE_URL=jdbc:postgresql://localhost:5432/jamunaconnect
DATABASE_USERNAME=jamuna
DATABASE_PASSWORD=${DB_PASS}
PORT=8080
OFFICE_EMAIL=jamuna-office@smail.iitm.ac.in
ESCALATION_EMAIL=jamunagensec@smail.iitm.ac.in
SMTP_HOST=
EOF
chmod 600 /opt/jamuna/app.env
chown jamuna:jamuna /opt/jamuna/app.env

cat > /etc/systemd/system/jamuna.service <<'UNIT'
[Unit]
Description=JamunaConnect (Spring Boot)
After=network.target postgresql.service
Wants=postgresql.service

[Service]
User=jamuna
WorkingDirectory=/opt/jamuna
EnvironmentFile=/opt/jamuna/app.env
ExecStart=/usr/bin/java -jar /opt/jamuna/app.jar
Restart=on-failure
RestartSec=5
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
UNIT

systemctl daemon-reload
systemctl enable jamuna.service
echo "bootstrap complete" > /opt/jamuna/bootstrap.done