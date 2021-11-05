# Agent
Trawler agent can currently extract metadata from relational databases. Support
for additional systems will come in the near future.

To get started with configuring `trawler-agent`, check out the [Getting Started](/getting-started) guide.

## Configuration
The agent's tasks can either be configured directly using the command line or
from a supplied YAML file. 

To run a single task from a configuration file, `example.yml`:

```bash
docker run --net=host \
    -v $(pwd)/example.yml:/app/config/example.yml \
    -e TRAWLER_TOKEN=<token> \
    -e TRAWLER_ENDPOINT=http://localhost:9090 \
    scalardev/trawler-agent \
    run --now /app/config/example.yml
```

This mode may also be useful if you want to use your own job scheduler.  To make
life easy, the agent comes with a very simple scheduler which uses the Python
[schedule package](https://github.com/dbader/schedule). To run the agent in
daemon mode using any supplied job schedules:

```bash
docker run --net=host \
    -v $(pwd)/example.yml:/app/config/example.yml \
    -e TRAWLER_TOKEN=<token> \
    -e TRAWLER_ENDPOINT=http://localhost:9090 \
    scalardev/trawler-agent \
    run --now /app/config/example.yml 
```