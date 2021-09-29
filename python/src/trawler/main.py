import dataclasses
import json
from datetime import datetime, date
from decimal import Decimal

import sqlparse
from urllib.parse import urlparse
import click
from sqlalchemy.inspection import inspect
from sqlalchemy.engine import create_engine

from trawler.schema import Object, Field, Relation
from trawler.graph import Graph


@click.group()
def main():
    pass


def sql_query(conn, sql):
    try:
        val = next(conn.execute(sql))[0]
        if isinstance(val, datetime):
            return str(val)
        elif isinstance(val, date):
            return str(val)
        if isinstance(val, Decimal):
            return str(val)
        else:
            return val
    except:
        return None


def get_column_metrics(engine, table_name, column_name):
    with engine.connect() as conn:
        return {
            "metrics__nullRatio": sql_query(
                conn,
                f"""
                SELECT
                CAST(SUM(CASE WHEN {column_name} IS NULL THEN 1 ELSE 0 END) as DOUBLE PRECISION) / COUNT(*)
                FROM {table_name};
                """,
            ),
            "metrics__max": sql_query(
                conn,
                f"""
                SELECT
                MAX({column_name})
                FROM {table_name};
                """,
            ),
            "metrics__min": sql_query(
                conn,
                f"""
                SELECT
                MIN({column_name})
                FROM {table_name};
                """,
            ),
        }


def get_table_metrics(engine, table_name):
    with engine.connect() as conn:
        return {
            "metrics__count": next(
                conn.execute(
                    f"""
                SELECT COUNT(*)
                FROM {table_name};
                """
                )
            )[0]
        }


@main.command()
@click.argument("uri")
def sql(uri: str):
    """Capture a schema from a running database via sqlalchemy"""
    engine = create_engine(uri)
    inspector = inspect(engine)

    parsed_uri = urlparse(uri)
    host = parsed_uri.netloc.split("@")[-1]

    tables = []
    constraints = []

    g = Graph()

    for schema in inspector.get_schema_names():
        for table_name in inspector.get_table_names(schema):
            print(schema, table_name)
            comments = inspector.get_table_comment(table_name, schema=schema)["text"]

            object = Object(table_name, [], [], comments)
            primary_key_columns = set(
                inspector.get_pk_constraint(table_name, schema=schema)[
                    "constrained_columns"
                ]
            )
            for column in inspector.get_columns(table_name, schema=schema):
                object.fields.append(
                    Field(
                        column["name"],
                        column["type"].__visit_name__,
                        column["comment"],
                        column["nullable"],
                        column["name"] in primary_key_columns,
                    )
                )
            for fkey in inspector.get_foreign_keys(table_name, schema=schema):
                object.relations.append(
                    Relation(
                        fkey["name"],
                        fkey["constrained_columns"],
                        fkey["referred_table"],
                        fkey["referred_columns"],
                    )
                )

            table = g.SqlTable(
                f"urn:tr:table:{parsed_uri.scheme}/{host}/{parsed_uri.path.strip('/')}/{schema}/{table_name}",
                name=table_name,
                tr__has=[
                    g.SqlColumn(
                        f"urn:tr:sql-table::{parsed_uri.scheme}/{host}/{parsed_uri.path.strip('/')}/{schema}/{table_name}/{field.name}",
                        name=field.name,
                        tr__type=field.type,
                        tr__isNullable=field.nullable,
                        tr__comment=field.comment,
                        tr__foreignKeyConstraints=[
                            f"urn:tr:constraint:{parsed_uri.scheme}/{host}/{parsed_uri.path.strip('/')}/{schema}/{table_name}/{relation.name}"
                            for relation in object.relations
                            if field.name in relation.source_fields
                        ],
                        **get_column_metrics(
                            engine, f'{schema}."{table_name}"', f'"{field.name}"'
                        ),
                    )
                    for field in object.fields
                ],
                **get_table_metrics(engine, f'{schema}."{table_name}"'),
            )
            tables.append(table)

            for relation in object.relations:
                constraints.append(
                    g.SqlConstraint(
                        f"urn:tr:sql-constraint::{parsed_uri.scheme}/{host}/{parsed_uri.path.strip('/')}/{schema}/{table_name}/{relation.name}",
                        tr__has=[
                            {
                                "@id": f"urn:tr:sql-column::{parsed_uri.scheme}/{host}/{parsed_uri.path.strip('/')}/{schema}/{relation.target_object}/{field}",
                            }
                            for field in relation.target_fields
                        ],
                    )
                )

    db = g.SqlDatabase(
        f"urn:tr:sql-database::{parsed_uri.scheme}/{host}/{parsed_uri.path.strip('/')}",
        name=parsed_uri.path.strip("/"),
        tr__has=tables + constraints,
    )

    g = Graph()
    g.add(db)
    print(g.store())


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
