# Trawler

Trawler is an open source metadata platform for mapping and monitoring your data
and systems. It uses standard semantic web formats (such as
JSON-LD) to build a live knowledge graph all the way down to individual fields
or columns. It comes with a lightweight agent to collect metadata as well as a
web-based UI to browse available data and visualise relationships and metrics.

## Trawler platform
The trawler platform can be run as a single docker image. All it needs is a
running `postgres` database. The simplest way to get started is using
`docker-compose`.

```bash
curl https://raw.githubusercontent.com/scalar-dev/trawler/master/docker-compose.example.yml
docker-compose -f docker-compose.example.yml up
```

## Agent
The trawler agent connects to your databases and other systems in order to
extract metadata and upload it to the trawler platform. This is also most easily
run with `docker`.

First, you'll need to create a user to upload data:

```bash
curl -g \
-X POST \
-H "Content-Type: application/json" \
-d '{"query":"mutation { createUser(email: \"me@example.com\", password: \"password\") }"}' \
http://localhost:8080
```

This should give you a user id.

You'll then need to create yourself a project and a role in the postgres:

```sql
insert into project values('63255f7a-e383-457a-9c30-4c7f95308749', 'test')
insert into account_role(account_id, project_id, role) values('<userid>', '63255f7a-e383-457a-9c30-4c7f95308749', 'admin');
```

Then login to get a user JWT:

```bash
curl -g \
-X POST \
-H "Content-Type: application/json" \
-d '{"query":"mutation { login(email: \"me@example.com\", password: \"password\") { jwt } }"}' \
http://localhost:8080
```

Finally take this token and grab yourself a JWT for the collect API

```bash
curl -g \
-X POST \
-H "Content-Type: application/json" \
-H "Authorization: Bearer <token>" \
-d '{"query":"mutation { collectToken { jwt } }"}' \
http://localhost:8080
```

The JWT from this step needs to be used as `TRAWLER_TOKEN` below.

Firstly, create a configuration file, `example.yml`:

```yaml
jobs:
- type: sql
  schedule:
    every: minutes
    interval: 60
    on_startup: true
  override_dbname: "my-db-name"
  # Replace this with your actual database URI
  uri: postgresql://postgres:postgres@localhost/postgres%       
```

Then run:

```bash
docker run --net=host \
    -v $(pwd)/example.yml:/app/config/example.yml \
    -e TRAWLER_TOKEN=<token> \
    scalardev/trawler-agent:54bf319fb00f0ff4674d7e7bcf74b9a1061b0510 \
    run --now /app/config/example.yml 
```
