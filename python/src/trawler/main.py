import dataclasses
import json
import requests
from datetime import datetime

import sqlparse
from urllib.parse import urlparse
import click
from sqlalchemy.inspection import inspect
from sqlalchemy.engine import create_engine

from trawler.schema import Object, Field, Relation

class EnhancedJSONEncoder(json.JSONEncoder):
        def default(self, o):
            if dataclasses.is_dataclass(o):
                return dataclasses.asdict(o)
            return super().default(o)

@click.group()
def main():
    pass

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

    for schema in inspector.get_schema_names():
        for table_name in inspector.get_table_names(schema):
            print(schema, table_name)
            comments = inspector.get_table_comment(table_name, schema=schema)["text"]

            object = Object(table_name, [], [], comments)
            primary_key_columns = set(inspector.get_pk_constraint(table_name, schema=schema)["constrained_columns"])
            for column in inspector.get_columns(table_name, schema=schema):
                object.fields.append(
                    Field(column["name"], column["type"].__visit_name__, column["comment"], column["nullable"], 
                        column["name"] in primary_key_columns
                    )
                )
            for fkey in inspector.get_foreign_keys(table_name, schema=schema):
                object.relations.append(
                    Relation(fkey["name"], fkey["constrained_columns"], fkey["referred_table"], fkey["referred_columns"])
                )

            table = {
                "@id": f"urn:tr:table:{parsed_uri.scheme}/{host}/{parsed_uri.path.strip('/')}/{schema}/{table_name}",
                "@type": "tr:Table",
                "name": table_name,
                "tr:hasFields": [
                    {
                        "@id": f"urn:tr:field:{parsed_uri.scheme}/{host}/{parsed_uri.path.strip('/')}/{schema}/{table_name}/{field.name}",
                        "@type": "tr:Field",
                        "name": field.name,
                        "tr:type": field.type,
                        "tr:isNullable": field.nullable,
                        "tr:comment": field.comment,
                        "tr:foreignKeyConstraints": [
                            f"urn:tr:constraint:{parsed_uri.scheme}/{host}/{parsed_uri.path.strip('/')}/{schema}/{table_name}/{relation.name}"
                            for relation in object.relations
                            if field.name in relation.source_fields
                        ]
                    }
                    for field in object.fields
                ],
            }
            tables.append(table)

            for relation in object.relations:
                constraints.append({
                    "@id": f"urn:tr:constraint:{parsed_uri.scheme}/{host}/{parsed_uri.path.strip('/')}/{schema}/{table_name}/{relation.name}",
                    "@type": "tr:Constraint",
                    "tr:hasFields": [
                            {
                                "@id": f"urn:tr:column:{parsed_uri.scheme}/{host}/{parsed_uri.path.strip('/')}/{schema}/{relation.target_object}/{field}",
                            }
                            for field in relation.target_fields
                    ]
                })

    out = [{
        "@context": "http://trawler.dev/schema/core/0.1",
        "@id": f"urn:tr:Database:{parsed_uri.scheme}/{host}/{parsed_uri.path.strip('/')}",
        "@type": "tr:Database",
        "name": parsed_uri.path.strip('/'),
        "tr:has": tables + constraints
    }]

    r = requests.post("http://localhost:9090/api/collect/63255f7a-e383-457a-9c30-4c7f95308749", json=out,
    headers={"Authorization": "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzZTQwY2U5MC1kN2M4LTQ5YzQtOTI3ZS05OWU3MGNhNmY3YmMiLCJpYXQiOjE2MjI5OTYxNjl9.DODGqPd8pj3OiTTm7VV2XhxGC5gyV7qV97HRVEUiOEY"})
    r.raise_for_status()

    print(r.json())

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