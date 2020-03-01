# migration testing

1. copy heroki/live db to backup_v_$(version)  \
    ```
    pg_dump -C -h $DB_SERVER -U postgres pentagame -f "backup_v$(VERSION).sql"
    psql -U username -c "CREATE DATABASE "backup_v$(VERSION)"
    psql -U postgres -d "backup_v$(VERSION)" -f "backup_v$(VERSION).sql"
   ```

2. copy to `test_v$(version)` \
    ```sql
    DROP DATABASE pentagame_test_v$(VERSION);
    CREATE DATABASE pentagame_test_v$(VERSION)
    WITH TEMPLATE backup_v$(VERSION);
   ```

3. run migration code on `test_v$(VERSION)` \
    ```
     ./gradlew flyway
   ```


2. run db_test on `test_v$(VERSION)` \
   ```bash
    ./gradlew :backend:jvmTests
   ```

# creating migration

https://www.apgdiff.com/

1. copy heroki/live db to backup_v_$(version)  \
    ```
    pg_dump -C -h $DB_SERVER -U postgres pentagame -f "backup_v$(VERSION).sql"
   ```

2 dump current and create new empty database \
    ```shell script
    DROP DATABASE pentagame_new;
    CREATE DATABASE pentagame_new;
    ./gradlew :backend:test
    
    pg_dump -C -h localhost -U postgres pentagame_new -f "pentagame_new.sql"
    ```

3. run diff as gradle task \
    ```shell script
    # java -jar apgdiff-2.4.jar --ignore-start-with original.sql new.sql > upgrade.sql
    java -jar apgdiff-2.4.jar --ignore-start-with backup_v$(VERSION).sql pentagame_new.sql > upgrade.sql
    migra $(backup_v$(version) psql://localhost/dev_db
    ```


# Notes: 

`choco install postgresql11 /Password:postgres`