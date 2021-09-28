
# Queries
- For a given database object give me all of the current properties
- For a given database object and some field, give me the history of changes
- Find me all objects linked to a given object (with a particular relationship)
- Search objects by name


# Tables

## entity
id: uuid
urn: text
type_id: uuid


## entity_type
id: UUID
uri: text
name: text
is_deleted: BOOLEAN

## facet_type
id: SMALLINT
uri: text
name: text
meta_type: text

## facet_log
entity_id: UUID
facet_type_id: SMALLINT
version: BIGINT
index: SMALLINT

value_text: TEXT
value_entity_id: UUID
value_json: JSONB

16 + 2 + 8 + 2
28 + unknown


```sql
-- Upsert entity
INSERT INTO entity(id, urn, type_id) VALUES(?, ?, ?) ON CONFLICT DO UPDATE SET urn = ?

-- Get previous version
SELECT * from facet_log where entity_id = ? AND facet_type_id = ? ORDER BY version DESC LIMIT 1 FOR UPDATE

-- Optionally delete stuff
DELETE FROM  facet_log WHERE ...

-- Insert new value
INSERT INTO facet_log(...)
```