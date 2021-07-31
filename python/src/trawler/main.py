import dataclasses
import json
import requests
from datetime import datetime

import sqlparse
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

    out = {
        "source": {
            "url": engine.url.render_as_string(),
            "dialect": engine.dialect.name
        },
        "objects": []
    }

    for table_name in inspector.get_table_names():
        object = Object(table_name, [], [], inspector.get_table_comment(table_name)["text"])
        primary_key_columns = set(inspector.get_pk_constraint(table_name)["constrained_columns"])
        for column in inspector.get_columns(table_name):
            object.fields.append(
                Field(column["name"], column["type"].__visit_name__, column["comment"], column["nullable"], 
                    column["name"] in primary_key_columns
                )
            )
        for fkey in inspector.get_foreign_keys(table_name):
            object.relations.append(
                Relation(fkey["name"], fkey["constrained_columns"], fkey["referred_table"], fkey["referred_columns"])
            )

        out["objects"].append(object) 

    data = json.dumps({
        "locator": "/foo",
        "timestamp": datetime.utcnow().isoformat()[:-3]+'Z',
        "schemas": [out]
    }, cls=EnhancedJSONEncoder, indent=1)


    r = requests.post(
        "http://localhost:9090/api/collect/v1/290cdbd9-d428-4a1a-9d72-0a34ca035d90", data,
        headers={
            "Authorization": "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzZTQwY2U5MC1kN2M4LTQ5YzQtOTI3ZS05OWU3MGNhNmY3YmMiLCJpYXQiOjE2MjI5OTYxNjl9.DODGqPd8pj3OiTTm7VV2XhxGC5gyV7qV97HRVEUiOEY"
        }
    )
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