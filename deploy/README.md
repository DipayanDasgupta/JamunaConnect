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

`nginx-jamuna.conf` proxies HTTP to `127.0.0.1:8080` and forwards
`X-Forwarded-For`, so the API's per-IP rate limit sees the real client address.

Production HTTPS is terminated on the instance with a Let's Encrypt certificate for
`jamuna.13.204.197.82.nip.io` (certbot's nginx plugin redirects HTTP to HTTPS and
installs an auto-renew timer). Because Vercel rewrites to that HTTPS origin, Spring
receives `X-Forwarded-Proto: https` and `X-Forwarded-Host` and keeps redirects on the
public host.

## Environment

See `.env.example`. In production these come from `/opt/jamuna/app.env`
(`EnvironmentFile` in the systemd unit), so staging and production differ only by
environment, as the design doc requires. Leaving `SMTP_HOST` blank keeps notifications
in the in-app queue; the complaint workflow still works.

Production must also set:

- `STAFF_OFFICE_USERNAME`, `STAFF_OFFICE_PASSWORD_HASH`, `STAFF_OFFICE_DISPLAY_NAME`
- `STAFF_WARDEN_USERNAME`, `STAFF_WARDEN_PASSWORD_HASH`, `STAFF_WARDEN_DISPLAY_NAME`
- `SESSION_COOKIE_SECURE=true` behind HTTPS

`StaffCredentialsSeeder` upserts those BCrypt hashes on boot, replacing the
development `office123` / `warden123` accounts. Keep the plaintext passwords out of
git and out of shell history.

## Re-deploying

The normal path is a push to `main`: CI runs `mvn verify`, uploads the jar to S3, and
restarts the service over SSM. The deploy step now polls
`http://127.0.0.1:8080/actuator/health` and prints recent journal lines if the app does
not become healthy. Flyway applies new migrations before the app serves traffic.
Development staff credentials are never used in production; the environment hashes
replace them.