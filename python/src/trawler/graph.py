from functools import partial
from dataclasses import dataclass
from typing import Any

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
      key.replace("__", ":"): value
      for key, value in kwargs.items()
    }

  def json(self):
    out = {
      "@type": self.type,
      "@id": self.id
    }

    for uri, value in self.attributes.items():
      out[uri] = to_json(value)
    return out

class Context:
  def __getattr__(self, name):
    return partial(EntityStub, f"tr:{name}")


class Graph:
  def __init__(self):
    self._entities = []

  def add(self, entity):
    self._entities.append(entity)

  def entities(self):
    return self._entities

  def json(self, context=None):
    return {
      "@context": "http://trawler.dev/schema/core",
      "@graph": [
        entity.json() for entity in self._entities
      ]
    }