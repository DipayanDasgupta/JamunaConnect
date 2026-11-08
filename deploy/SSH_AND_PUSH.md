# SSH into the VM and pushing changes — JamunaConnect

Backend host: one EC2 instance (Ubuntu 24.04, Java 21 JRE, PostgreSQL with
`pg_trgm` on the same box). The app runs as a systemd service named `jamuna`,
as the `jamuna` user, from `/opt/jamuna/app.jar`, on port 8080, behind nginx
(HTTP/HTTPS → `127.0.0.1:8080`). The public origin is
`https://jamuna.13.204.197.82.nip.io`; certbot renews the certificate automatically. Secrets live in `/opt/jamuna/app.env`
(mode 0600) — never in the repo. Instance: `i-0c3c936ad498e2861`,
artifact bucket: `s3://jamuna-artifacts-552916920281/jamuna.jar`.

## 1. SSH into the VM

You need the `.pem` key used at instance launch and the instance's public
DNS/IP (EC2 console → Instances → `i-0c3c936ad498e2861`).

```bash
chmod 400 jamuna-key.pem
ssh -i jamuna-key.pem ubuntu@<host>
```

Once in, the only paths that matter:

```bash
systemctl status jamuna              # is the app running?
systemctl is-active jamuna           # prints "active" when healthy
journalctl -u jamuna -f              # live app logs (Ctrl-C to exit)
journalctl -u jamuna --since "30 min ago" | tail -50
curl -s localhost:8080/actuator/health   # {"status":"UP"} means serving
ss -tlnp | grep 8080                 # confirm the JVM holds the port
cat /opt/jamuna/app.env              # effective config (DB, mail, thresholds)
sudo -u postgres psql -d jamunaconnect -c '\dt'   # tables present?
```

If port 22 is closed (the security group may allow no inbound SSH — CI
deploys over SSM for exactly this reason), open a shell through Session
Manager instead (needs the `session-manager-plugin` installed locally):

```bash
aws ssm start-session --target i-0c3c936ad498e2861 --region <AWS_REGION>
```

## 2. Pushing changes — the normal way (git push, CI does the rest)

Every push to `main` runs `.github/workflows/ci.yml`: `mvn -B verify`
against real PostgreSQL → package jar → deploy static `frontend/` to Vercel
production → upload jar to S3 → restart the EC2 service over SSM and wait
for `active`. So the standard push is:

```bash
git status --short
git add <files>
git commit -m "fix(C): what and why"
git push origin main
```

Then watch it: GitHub → Actions → latest `ci` run → `build` must go green
first; `deploy-frontend` and `deploy-backend` only run from green `main`.
Verify after:

```bash
curl -s https://jamuna-connect.vercel.app/ | head -3          # frontend live
curl -s https://jamuna.13.204.197.82.nip.io/actuator/health  # backend UP over TLS
```

## 3. Pushing manually (emergency / viva demo)

If CI is broken or you must show the deploy by hand, build locally and copy
the jar exactly as `deploy/README.md` describes:

```bash
mvn -DskipTests package
cat target/jamunaconnect-*.jar | ssh -i jamuna-key.pem ubuntu@<host> \
  'cat > /tmp/app.jar && sudo mv /tmp/app.jar /opt/jamuna/app.jar && \
   sudo chown jamuna:jamuna /opt/jamuna/app.jar'
ssh -i jamuna-key.pem ubuntu@<host> 'sudo systemctl restart jamuna'
ssh -i jamuna-key.pem ubuntu@<host> 'systemctl is-active jamuna && curl -s localhost:8080/actuator/health'
```

Flyway applies any new migrations on startup, so a schema change ships as
part of the same restart. If the service fails to come up, read
`journalctl -u jamuna` first — a failed migration or a bad env value is the
usual cause — fix forward, re-copy, restart. To roll back, rebuild the jar
from the last green commit (`git checkout <sha>`, package, copy, restart)
and return to `main` after.

## 4. What to say if asked in the viva

SSH proves you can reach the box; the push story proves you understand the
pipeline. Say: code ships through git to `main`, CI verifies against real
PostgreSQL and only then deploys — Vercel takes the static directory as-is
with no build step, EC2 gets the jar via S3 plus an SSM remote restart, and
Flyway migrates on boot. Secrets are environment-only, scoped to the
instance, and the CI user is a least-privilege access key. Manual SCP plus
`systemctl restart` exists for emergencies but is not the normal path.

## 5. Starter prompt for Gemini Pro

Paste this whole file into Gemini Pro first, then use prompts like these:

```
Using SSH_AND_PUSH.md as the runbook, give me the exact commands to check
whether the jamuna service is healthy on the EC2 instance, assuming my key
is ~/keys/jamuna-key.pem and the host is <host>.
```

```
Using SSH_AND_PUSH.md, I changed the escalation window and committed to
main but the backend still shows the old behaviour. List the verification
commands in order: CI status, jar timestamp on the instance, service state,
effective env value, and scheduler log lines.
```

```
Using SSH_AND_PUSH.md, write me the manual rollback commands to redeploy
the jar built from commit <sha> and confirm the service is active.
```
