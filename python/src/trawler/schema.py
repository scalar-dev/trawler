from dataclasses import dataclass
from enum import Enum
from typing import Any, Dict, List, Optional
from sqlalchemy.sql.sqltypes import TypeEngine

class FieldType(Enum):
    INT = "int"
    FLOAT = "float"
    STRING = "str"


@dataclass
class Field:
    name: str
    field_type: TypeEngine
    nullable: bool
    is_primary_key: bool
    comment: Optional[str]

@dataclass
class Relation:
    name: str
    source_fields: List[str]
    target_object: str
    target_fields: List[str]

@dataclass
class Table:
    name: str
    fields: List[Field]
    relations: List[Relation]
    comment: Optional[str]


@dataclass
class Schema:
    source: str

