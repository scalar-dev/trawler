from datetime import datetime, date
from typing import Optional
from pprint import pprint
import click
from trawler.sql.extract import extract_sql
from trawler.agent.jobs import run_scheduler

@click.group()
def main():
    pass

@main.command()
@click.argument("uri")
@click.option("--override-host")
@click.option("--override-dbname")
def sql(uri: str, override_dbname: Optional[str]):
    pprint(extract_sql(uri, override_dbname))

@main.command()
@click.argument("config")
@click.option("--now", is_flag=True)
def run(config: str, now: bool):
    run_scheduler(config, now)

if __name__ == "__main__":
    main()
