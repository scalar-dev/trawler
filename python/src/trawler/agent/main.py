from datetime import datetime, date
from typing import Optional
from pprint import pprint
import sqlparse
import click
from trawler.sql.extract import extract_sql

@click.group()
def main():
    pass

@main.command()
@click.argument("uri")
@click.option("--override-host")
@click.option("--override-dbname")
def sql(uri: str, override_host: Optional[str], override_dbname: Optional[str]):
    pprint(extract_sql(uri, override_dbname))

if __name__ == "__main__":
    main()
