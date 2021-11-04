from functools import partial
from dataclasses import dataclass
from typing import Any
import os

import requests

def to_json(value):
    if isinstance(value, EntityStub):
        return value.json()
    elif isinstance(value, list):
        return [to_json(v) for v in value]
    elif isinstance(value, dict):
        return {k: to_json(v) for k, v in value.items()}
    else:
        return value


@dataclass
class EntityStub:
    id: str
    type: str
    attributes: dict[str, Any]

    def __init__(self, typ, id, **kwargs):
        self.id = id
        self.type = typ
        self.attributes = {
            key.replace("__", ":"): value for key, value in kwargs.items()
        }

    def json(self):
        out = {"@type": self.type, "@id": self.id}

        for uri, value in self.attributes.items():
            out[uri] = to_json(value)
        return out


class Graph:
    def __init__(self):
        self._entities = []

    def add(self, entity):
        self._entities.append(entity)

    def entities(self):
        return self._entities

    def __getattr__(self, name):
        return partial(EntityStub, f"tr:{name}")

    def json(self, context=None):
        return {
            "@context": "http://trawler.dev/schema/core",
            "@graph": [entity.json() for entity in self._entities],
        }

    def store(self, project=None):
        out = self.json()

        token = os.environ["TRAWLER_TOKEN"]
        endpoint = os.environ.get("TRAWLER_ENDPOINT", "https://api.trawler.dev")
        project = project or os.environ.get("TRAWLER_PROJECT", "63255f7a-e383-457a-9c30-4c7f95308749")

        r = requests.post(
            f"{endpoint}/api/collect/{project}",
            json=out,
            headers={"X-API-Key": token}
        )
        r.raise_for_status()
        return r.json()
