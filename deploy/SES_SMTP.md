# Amazon SES SMTP for JamunaConnect

Mail is notification-only. If this setup is absent or SES rejects a message,
the complaint is still persisted and stays on the staff dashboard queue
(`MailNotificationService`). SMTP never gates the complaint workflow.

## Where the configuration lives

| Layer | File | Notes |
|-------|------|-------|
| Env contract | `.env.example` | `SMTP_HOST/PORT/USERNAME/PASSWORD/FROM/AUTH/STARTTLS` |
| Spring mapping | `src/main/resources/application.properties` | `spring.mail.*` reads those env vars |
| Production values | `/opt/jamuna/app.env` on EC2 | mode 0600, loaded by `deploy/jamuna.service` |
| Consumer | `src/main/java/edu/iitm/jamunaconnect/notify/MailNotificationService.java` | async, best-effort |

Never commit `app.env`, SMTP passwords, or IAM secret keys.

## Current production setup

- Region: `ap-south-1` (same as the instance)
- SMTP endpoint: `email-smtp.ap-south-1.amazonaws.com`, port `587`, STARTTLS
- Sender (`SMTP_FROM`): `deep.dasgupta2006@gmail.com` (verified SES identity)
- IAM user: `jamuna-ses-smtp`, inline policy `JamunaSesSendOnly`
  (`ses:SendEmail`, `ses:SendRawEmail` on the sender identity only)
- Recipients in `app.env`:
  - `OFFICE_EMAIL=jamuna@smail.iitm.ac.in`
  - `ESCALATION_EMAIL=CE24B094@smail.iitm.ac.in`

## Sandbox limitation

The account is still in the SES sandbox (`ProductionAccessEnabled: false`,
200 messages/day, 1/second). In the sandbox **every recipient must also be a
verified identity**; otherwise SES returns `554 Message rejected: Email address
is not verified`.

Request production access in the SES console (Account dashboard → Request
production access). Until approval, verify each recipient:

```bash
aws sesv2 create-email-identity \
  --email-identity jamuna@smail.iitm.ac.in --region ap-south-1
aws sesv2 create-email-identity \
  --email-identity CE24B094@smail.iitm.ac.in --region ap-south-1

# Click the verification link in each inbox, then check:
aws sesv2 list-email-identities --region ap-south-1 \
  --query 'EmailIdentities[].[IdentityName,VerificationStatus]' --output table
```

A `PENDING` identity delivers nothing. `SUCCESS` is required.

## Rotating the SMTP credentials

1. Create a replacement access key for `jamuna-ses-smtp`:

   ```bash
   aws iam create-access-key --user-name jamuna-ses-smtp
   ```

2. Convert the secret access key to an SES SMTP password. The constants are
   fixed: date `11111111`, service `ses`, terminal `aws4_request`, message
   `SendRawEmail`, version byte `0x04`, region `ap-south-1`.

   ```python
   import base64, hashlib, hmac
   secret = "REPLACE_WITH_SECRET_ACCESS_KEY"
   k = hmac.new(("AWS4" + secret).encode(), b"11111111", hashlib.sha256).digest()
   k = hmac.new(k, b"ap-south-1", hashlib.sha256).digest()
   k = hmac.new(k, b"ses", hashlib.sha256).digest()
   k = hmac.new(k, b"aws4_request", hashlib.sha256).digest()
   k = hmac.new(k, b"SendRawEmail", hashlib.sha256).digest()
   print(base64.b64encode(b"\x04" + k).decode())
   ```

3. Open an SSM shell so secrets never enter command history:

   ```bash
   aws ssm start-session --target i-0c3c936ad498e2861 --region ap-south-1
   sudo sed -i '/^SMTP_/d' /opt/jamuna/app.env
   sudo tee -a /opt/jamuna/app.env >/dev/null <<'EOF'
   SMTP_HOST=email-smtp.ap-south-1.amazonaws.com
   SMTP_PORT=587
   SMTP_USERNAME=<new access key id>
   SMTP_PASSWORD=<derived SMTP password>
   SMTP_FROM=deep.dasgupta2006@gmail.com
   SMTP_AUTH=true
   SMTP_STARTTLS=true
   EOF
   sudo chown jamuna:jamuna /opt/jamuna/app.env
   sudo chmod 600 /opt/jamuna/app.env
   sudo systemctl restart jamuna
   ```

4. Verify authentication before deleting the old key. From any host:

   ```bash
   python3 - <<'PY'
   import smtplib, ssl
   with smtplib.SMTP("email-smtp.ap-south-1.amazonaws.com", 587, timeout=20) as s:
       s.starttls(context=ssl.create_default_context())
       s.login("<access key id>", "<derived SMTP password>")
   print("SMTP login OK")
   PY
   ```

5. Delete the previous access key:

   ```bash
   aws iam list-access-keys --user-name jamuna-ses-smtp
   aws iam delete-access-key --user-name jamuna-ses-smtp --access-key-id <old key id>
   ```

## Troubleshooting

| Symptom | Cause |
|---------|-------|
| `535 Authentication Credentials Invalid` | Wrong derivation constant/region, or temporary STS credentials were used |
| `554 Message rejected: Email address is not verified` | SES sandbox and the recipient is not a verified identity |
| Mail fails but complaint exists | Expected: `MailNotificationService` logs and falls back to the in-app queue |

Check the service side:

```bash
systemctl is-active jamuna
curl -s localhost:8080/actuator/health
journalctl -u jamuna --since "30 min ago" | grep -iE "mail|smtp|notification"
```
