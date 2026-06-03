Authentication Application

Tech stack: Java, Spring boot, Mongodb

TODO:
APIs:

restructure folder : DONE

USER api:
add status field
update user
delete user
get user by id

ROLE apis:
add status createdOn fields
register update lookupAll lookupById 

rate limiting—account lockout on failed attempts—scheduler to lock and unlock accounts.
Basic-level (7.5) Production-level (10)
───────────────────────────────────────────
ConcurrentHashMap → Redis-backed Bucket4j
request.getRemoteAddr() → X-Forwarded-For aware
No admin unlock → PATCH /api/v1/user/{id}/unlock
No notifications → Email alert on lock
Manual constants → Configurable via application.properties