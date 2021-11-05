# Trawler

Trawler is an open source metadata platform for mapping and monitoring your data
and systems. It uses standard semantic web formats (such as
JSON-LD) to build a live knowledge graph all the way down to individual fields
or columns. It comes with a lightweight agent to collect metadata as well as a
web-based UI to browse available data and visualise relationships and metrics.

Find out more by perusing our [documentation](https://docs.trawler.dev) or
checking out the [live demo instance](https://app.trawler.dev).

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

Full instructions for configuring the agent are available
[here](https://docs.trawler.dev/getting-started).


## Scalar
Trawler is proudly developed and sponsored by [Scalar](https://www.scalar.dev) a
consultancy specialising in novel data engineering solutions.
