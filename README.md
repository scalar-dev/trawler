# Trawler

Trawler is an open source metadata platform for mapping and monitoring your data
and data-intensive systems. It uses standard semantic web formats (such as
JSON-LD) to build a live knowledge graph all the way down to individual fields
or columns. It comes with a lightweight agent to collect metadata as well as a
web-based UI to browse available data and visualise relationships and metrics.

## Getting started
The platform itself comes in a single easy-to-deploy docker image that you can
grab from Docker hub. All you need is a running postgres database. 

```bash

docker run --env PGHOST=<host> --env PGUSER=<user> --env PGPASSWORD=<password> --env PGDATABASE=<database> scalardev/trawler

```