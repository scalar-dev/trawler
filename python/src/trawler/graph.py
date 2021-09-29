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

    def store(self):
        out = self.json()

        endpoint = os.environ.get("TRAWLER_ENDPOINT", "http://localhost:9090")
        token = os.environ.get(
            "TRAWLER_TOKEN",
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzZTQwY2U5MC1kN2M4LTQ5YzQtOTI3ZS05OWU3MGNhNmY3YmMiLCJpYXQiOjE2MjI5OTYxNjl9.DODGqPd8pj3OiTTm7VV2XhxGC5gyV7qV97HRVEUiOEY",
        )

        r = requests.post(
            f"{endpoint}/api/collect/63255f7a-e383-457a-9c30-4c7f95308749",
            json=out,
            headers={"Authorization": f"Bearer {token}"},
        )
        r.raise_for_status()
        return r.json()
