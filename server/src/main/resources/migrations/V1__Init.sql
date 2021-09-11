
CREATE TABLE project(
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE entity_type(
    id UUID PRIMARY KEY,
    uri TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    project_id UUID REFERENCES project
);

CREATE TABLE entity(
    id UUID PRIMARY KEY,
    type_id UUID REFERENCES entity_type NOT NULL,
    urn TEXT UNIQUE NOT NULL,
    project_id UUID REFERENCES project NOT NULL,
    UNIQUE(urn, project_id)
);

CREATE TABLE facet_type(
    id UUID PRIMARY KEY,
    uri TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    meta_type TEXT NOT NULL,
    project_id UUID REFERENCES project
);

CREATE TABLE facet_log(
    project_id UUID REFERENCES project NOT NULL,
    entity_id UUID REFERENCES entity NOT NULL,
    type_id UUID REFERENCES facet_type NOT NULL,
    index SMALLINT NOT NULL,
    version BIGINT NOT NULL,
    timestamp TIMESTAMP,
    value JSONB,
    target_entity_id UUID REFERENCES entity,
    UNIQUE(entity_id, type_id, version, index)
);
