Authentication Application

Tech stack: Java, Spring boot, Mongodb

TODO:

1. remove delete role api and change status to deleted. and if role deleted then that role will not be added to any user.
    do something with user that already have the role.

2. password reset or forgot password

5. Revoke all sessions endpoint. No way for a user to invalidate all their active refresh tokens at once (e.g. "sign out everywhere").

rate limiting—account lockout on failed attempts—scheduler to lock and unlock accounts.
Basic-level (7.5) Production-level (10)
───────────────────────────────────────────
ConcurrentHashMap → Redis-backed Bucket4j
request.getRemoteAddr() → X-Forwarded-For aware
No admin unlock → PATCH /api/v1/user/{id}/unlock
No notifications → Email alert on lock
Manual constants → Configurable via application.properties