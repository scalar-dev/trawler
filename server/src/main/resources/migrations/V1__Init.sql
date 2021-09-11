
CREATE TABLE project(
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE facet_type(
    id UUID PRIMARY KEY,
    uri TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    meta_type TEXT NOT NULL,
    project_id UUID REFERENCES project
);

CREATE TABLE facet_log(
    id UUID PRIMARY KEY,
    project_id UUID REFERENCES project NOT NULL,
    entity_urn TEXT NOT NULL,
    type_id UUID REFERENCES facet_type NOT NULL,
    version BIGINT NOT NULL,
    timestamp TIMESTAMP,
    value JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE(entity_urn, type_id, version)
);

CREATE TABLE entity_type(
    id UUID PRIMARY KEY,
    uri TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    project_id UUID REFERENCES project
);

CREATE TABLE entity(
    id UUID PRIMARY KEY,
    type_id UUID REFERENCES entity_type,
    urn TEXT UNIQUE NOT NULL,
    project_id UUID REFERENCES project NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE(urn, project_id)
);
