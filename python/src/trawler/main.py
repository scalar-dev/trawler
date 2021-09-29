from datetime import datetime, date
import sqlparse
import click
from trawler.sql.extract import extract_sql


@click.group()
def main():
    pass


@main.command()
@click.argument("uri")
def sql(uri: str):
    extract_sql(uri)


@main.command()
@click.argument("path")
def file(path: str):
    with open(path) as f:
        parsed = sqlparse.parse(f.read())
        for stmt in parsed:
            if stmt.get_type() == "CREATE":
                for token in stmt.tokens:
                    print(token)


if __name__ == "__main__":
    main()
