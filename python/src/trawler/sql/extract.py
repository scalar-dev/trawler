from urllib.parse import urlparse
from sqlalchemy.inspection import inspect
from sqlalchemy.engine import create_engine
from typing import Optional

from trawler.schema import Table, Field, Relation
from trawler.graph import Graph
from trawler.sql.metrics import get_column_metrics, get_table_metrics
import logging
import os

LOG = logging.getLogger(__name__)

def database_urn(scheme, database):
    return f"urn:tr:sql-table::{scheme}/{database}"


def table_urn(scheme, database, schema, table):
    return f"urn:tr:sql-table::{scheme}/{database}/{schema}/{table}"


def column_urn(scheme, database, schema, table, field):
    return f"urn:tr:sql-column::{scheme}/{database}/{schema}/{table}/{field}"


def constraint_urn(scheme, database, schema, table, relation):
    return (
        f"urn:tr:sql-constraint::{scheme}/{database}/{schema}/{table}/{relation}"
    )


def inspect_table(inspector, schema, table_name):
    comments = inspector.get_table_comment(table_name, schema=schema)["text"]

    table = Table(table_name, [], [], comments)
    primary_key_columns = set(
        inspector.get_pk_constraint(table_name, schema=schema)["constrained_columns"]
    )
    for column in inspector.get_columns(table_name, schema=schema):
        LOG.debug(type(column["type"]))
        table.fields.append(
            Field(
                name=column["name"],
                field_type=column["type"],
                comment=column["comment"],
                nullable=column["nullable"],
                is_primary_key=column["name"] in primary_key_columns,
            )
        )
    for fkey in inspector.get_foreign_keys(table_name, schema=schema):
        table.relations.append(
            Relation(
                fkey["name"],
                fkey["constrained_columns"],
                fkey["referred_table"],
                fkey["referred_columns"],
            )
        )

    return table


def extract_sql(uri: str, override_dbname: Optional[str] = None, project=None):
    """Capture a schema from a running database via sqlalchemy"""
    engine = create_engine(uri)
    inspector = inspect(engine)

    parsed_uri = urlparse(uri)
    host = parsed_uri.netloc.split("@")[-1]
    database = parsed_uri.path.strip("/")
    scheme = parsed_uri.scheme

    db_name = override_dbname or f"{host}-{database}"

    g = Graph()

    tables = []
    constraints = []

    LOG.info(f"starting capture: {db_name}")
    for schema in inspector.get_schema_names():
        for table_name in inspector.get_table_names(schema):
            LOG.info(f"catpure: {schema}/{table_name}")
            table = inspect_table(inspector, schema, table_name)

            sql_table = g.SqlTable(
                table_urn(scheme, db_name, schema, table_name),
                name=table_name,
                tr__hasField=[
                    g.SqlColumn(
                        column_urn(
                            scheme,
                            db_name,
                            schema,
                            table_name,
                            field.name,
                        ),
                        name=field.name,
                        tr__dataType=field.field_type.__visit_name__,
                        tr__isNullable=field.nullable,
                        tr__comment=field.comment,
                        tr__hasConstraint=[
                            constraint_urn(
                                scheme,
                                db_name,
                                schema,
                                table_name,
                                relation.name,
                            )
                            for relation in table.relations
                            if field.name in relation.source_fields
                        ],
                        **get_column_metrics(
                            engine, field.field_type, f'{schema}."{table_name}"', f'"{field.name}"'
                        ),
                    )
                    for field in table.fields
                ],
                **get_table_metrics(engine, f'{schema}."{table_name}"'),
            )
            tables.append(sql_table)

            for relation in table.relations:
                constraints.append(
                    g.SqlConstraint(
                        constraint_urn(
                            scheme, db_name, schema, table_name, relation.name
                        ),
                        name=relation.name,
                        tr__constrains=[
                            {
                                "@id": column_urn(
                                    scheme,
                                    db_name,
                                    schema,
                                    relation.target_object,
                                    field,
                                ),
                            }
                            for field in relation.target_fields
                        ],
                    )
                )

    db = g.SqlDatabase(
        database_urn(scheme, db_name),
        name=database,
        tr__has=tables + constraints,
    )
    LOG.info(f"Finished capture: {db_name}")

    g = Graph()
    g.add(db)
    return g.store(project=project)
