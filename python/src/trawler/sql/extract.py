from urllib.parse import urlparse
from sqlalchemy.inspection import inspect
from sqlalchemy.engine import create_engine

from trawler.schema import Table, Field, Relation
from trawler.graph import Graph
from trawler.sql.metrics import get_column_metrics, get_table_metrics

def database_urn(scheme, host, database):
    return f"urn:tr:sql-table::{scheme}/{host}/{database}"


def table_urn(scheme, host, database, schema, table):
    return f"urn:tr:sql-table::{scheme}/{host}/{database}/{schema}/{table}"


def column_urn(scheme, host, database, schema, table, field):
    return f"urn:tr:sql-column::{scheme}/{host}/{database}/{schema}/{table}/{field}"


def constraint_urn(scheme, host, database, schema, table, relation):
    return (
        f"urn:tr:sql-constraint::{scheme}/{host}/{database}/{schema}/{table}/{relation}"
    )


def inspect_table(inspector, schema, table_name):
    comments = inspector.get_table_comment(table_name, schema=schema)["text"]

    table = Table(table_name, [], [], comments)
    primary_key_columns = set(
        inspector.get_pk_constraint(table_name, schema=schema)["constrained_columns"]
    )
    for column in inspector.get_columns(table_name, schema=schema):
        table.fields.append(
            Field(
                column["name"],
                column["type"].__visit_name__,
                column["comment"],
                column["nullable"],
                column["name"] in primary_key_columns,
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


def extract_sql(uri: str):
    """Capture a schema from a running database via sqlalchemy"""
    engine = create_engine(uri)
    inspector = inspect(engine)

    parsed_uri = urlparse(uri)
    host = parsed_uri.netloc.split("@")[-1]
    database = parsed_uri.path.strip("/")
    scheme = parsed_uri.scheme

    g = Graph()

    tables = []
    constraints = []

    for schema in inspector.get_schema_names():
        for table_name in inspector.get_table_names(schema):
            print(schema, table_name)
            table = inspect_table(inspector, schema, table_name)

            sql_table = g.SqlTable(
                table_urn(scheme, host, database, schema, table_name),
                name=table_name,
                tr__has=[
                    g.SqlColumn(
                        column_urn(
                            scheme,
                            host,
                            database,
                            schema,
                            table_name,
                            field.name,
                        ),
                        name=field.name,
                        tr__type=field.type,
                        tr__isNullable=field.nullable,
                        tr__comment=field.comment,
                        tr__foreignKeyConstraints=[
                            constraint_urn(
                                scheme,
                                host,
                                database,
                                schema,
                                table_name,
                                relation.name,
                            )
                            for relation in table.relations
                            if field.name in relation.source_fields
                        ],
                        **get_column_metrics(
                            engine, f'{schema}."{table_name}"', f'"{field.name}"'
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
                            scheme, host, database, schema, table_name, relation.name
                        ),
                        tr__has=[
                            {
                                "@id": column_urn(
                                    parsed_uri.scheme,
                                    host,
                                    database,
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
        database_urn(scheme, host, database),
        name=parsed_uri.path.strip("/"),
        tr__has=tables + constraints,
    )

    g = Graph()
    g.add(db)
    print(g.store())
