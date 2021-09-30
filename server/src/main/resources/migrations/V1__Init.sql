
CREATE TABLE project(
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE entity_type(
    id UUID PRIMARY KEY,
    uri TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    project_id UUID REFERENCES project,
    is_deprecated BOOLEAN
);

CREATE TABLE facet_type(
    id UUID PRIMARY KEY,
    uri TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    meta_type TEXT NOT NULL,
    project_id UUID REFERENCES project,
    is_deprecated BOOLEAN DEFAULT FALSE,
    index_time_series BOOLEAN DEFAULT FALSE
);

CREATE TABLE entity(
    id UUID PRIMARY KEY,
    urn TEXT UNIQUE NOT NULL,
    project_id UUID REFERENCES project NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE(urn, project_id)
);

CREATE TABLE facet_log(
    id UUID PRIMARY KEY,
    tx_id UUID NOT NULL,
    project_id UUID REFERENCES project NOT NULL,
    entity_urn TEXT NOT NULL,
    type_id UUID REFERENCES facet_type NOT NULL,
    version BIGINT NOT NULL,
    timestamp TIMESTAMP,
    value JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    entity_id UUID REFERENCES entity,
    UNIQUE(entity_urn, type_id, version)
);

CREATE TABLE facet_value(
    project_id UUID REFERENCES project NOT NULL,
    entity_id UUID REFERENCES entity NOT NULL,
    type_id UUID REFERENCES facet_type NOT NULL,
    index SMALLINT NOT NULL,
    version BIGINT NOT NULL,
    value JSONB,
    target_entity_id UUID REFERENCES entity,
    entity_type_id UUID REFERENCES entity_type,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE(entity_id, type_id, index)
);

CREATE TABLE facet_time_series(
    entity_id UUID REFERENCES entity NOT NULL,
    type_id UUID REFERENCES facet_type NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    value_double DOUBLE PRECISION,
    value_long BIGINT,
    version BIGINT NOT NULL,
    UNIQUE(entity_id, type_id, timestamp)
);
