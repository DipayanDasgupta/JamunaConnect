# Deploying JamunaConnect

The design doc targets a small always-on JVM host (Render/Fly.io, or in this case a
single EC2 instance) rather than a serverless platform: the app needs a long-lived
process for form-login sessions, the `@Scheduled` escalation scan, and a persistent
PostgreSQL connection with `pg_trgm`.

## AWS (EC2 + PostgreSQL on the same instance)

`ec2-user-data.sh` provisions an Ubuntu 24.04 instance:

- OpenJDK 21 (JRE) and PostgreSQL with `postgresql-contrib` (`pg_trgm`)
- a `jamuna` database/user with a random password written to `/opt/jamuna/app.env`
  (mode 0600) — never in the repository
- a `jamuna.service` systemd unit that runs `java -jar /opt/jamuna/app.jar` and
  restarts on failure

Then deploy a built jar and front it with nginx:

```bash
mvn -DskipTests package
cat target/jamunaconnect-*.jar | ssh ubuntu@<host> \
  'cat > /tmp/app.jar && sudo mv /tmp/app.jar /opt/jamuna/app.jar && \
   sudo chown jamuna:jamuna /opt/jamuna/app.jar'
ssh ubuntu@<host> 'sudo systemctl restart jamuna'
```

`nginx-jamuna.conf` proxies port 80 to `127.0.0.1:8080` and forwards
`X-Forwarded-For`, so the API's per-IP rate limit sees the real client address.

## Environment

See `.env.example`. In production these come from `/opt/jamuna/app.env`
(`EnvironmentFile` in the systemd unit), so staging and production differ only by
environment, as the design doc requires. Leaving `SMTP_HOST` blank keeps notifications
in the in-app queue; the complaint workflow still works.

## Re-deploying

Rebuild, copy the jar, `systemctl restart jamuna`; Flyway applies any new migrations on
startup. The seeded office/warden credentials are development-only and must be rotated
via a migration before the hostel office uses the system.