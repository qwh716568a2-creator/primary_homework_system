# Docker Deployment

## 1. Prepare files

Copy these files to your cloud server:

- `Dockerfile`
- `.dockerignore`
- `docker-compose.prod.yml`
- `.env.prod.example`
- source code or the whole `back-end` directory

## 2. Create env file

```bash
cd /opt/primary-homework/back-end
cp .env.prod.example .env
vim .env
```

At minimum, change:

- `DB_PASSWORD`
- `MYSQL_ROOT_PASSWORD`
- `APP_JWT_SECRET`

## 3. Start containers

```bash
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
```

## 4. Check status

```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f backend
```

## 5. Initialize database

After MySQL starts, import your SQL scripts into the `primary_homework_system` database.

You can enter the MySQL container:

```bash
docker exec -it primary-homework-mysql mysql -u root -p
```

Then import your schema and test data.

## 6. Stop / restart

```bash
docker compose -f docker-compose.prod.yml down
docker compose -f docker-compose.prod.yml restart backend
```

## 7. Upgrade backend

```bash
git pull
docker compose -f docker-compose.prod.yml --env-file .env up -d --build backend
```
