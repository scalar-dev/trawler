import dataclasses
import json

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
        object = Object(table_name, [], [], inspector.get_table_comment(table_name)["text"], {})
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

    with open("out.json", "w") as f:
        json.dump(out, f, cls=EnhancedJSONEncoder, indent=1)



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