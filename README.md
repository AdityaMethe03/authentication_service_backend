Authentication Application

Tech stack: Java, Spring boot, Mongodb

TODO:

1. delete user and role - permission to sudo admin only

2. Expired refresh tokens accumulate forever. The refresh_tokens collection has no TTL index. Revoked and expired records pile up indefinitely, bloating MongoDB.

3. Race condition in role seeding. DataSeeder uses findByName then save without any unique index or atomic upsert. Concurrent startups can insert duplicate roles.
   (
      If two instances of your app start at exactly the same time (e.g. rolling deployment, docker replicas), 
      both can execute step 1 simultaneously, both find nothing, and both proceed to step 2 — inserting duplicate roles. 
      In practice this is rare with a single-instance app, but it's a real risk in any scaled or containerized deployment.
   )
 


rate limiting—account lockout on failed attempts—scheduler to lock and unlock accounts.
Basic-level (7.5) Production-level (10)
───────────────────────────────────────────
ConcurrentHashMap → Redis-backed Bucket4j
request.getRemoteAddr() → X-Forwarded-For aware
No admin unlock → PATCH /api/v1/user/{id}/unlock
No notifications → Email alert on lock
Manual constants → Configurable via application.properties