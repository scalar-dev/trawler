# Agent
Trawler agent can currently extract metadata from relational databases. Support
for additional systems will come in the near future.

To get started with configuring `trawler-agent` using `docker`, check out the [Getting
Started](/getting-started) guide. You can also install the agent via `pip`.

```bash
pip install trawler-python
export TRAWLER_TOKEN=<token>
export TRAWLER_ENDPOINT=http://localhost:9090
```

## Configuration
The agent's tasks can either be configured directly using the command line or
from a supplied YAML file. 

To run a single task from a configuration file, `example.yml`:

```bash
    trawler run --now example.yml
```

This mode may also be useful if you want to use your own job scheduler.  To make
life easy, the agent comes with a very simple scheduler which uses the Python
[schedule package](https://github.com/dbader/schedule). To run the agent in
daemon mode using any supplied job schedules:

```bash
    trawler run /app/config/example.yml 
```