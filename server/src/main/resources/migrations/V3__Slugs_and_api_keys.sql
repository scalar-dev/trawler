
ALTER TABLE project
ADD COLUMN slug TEXT UNIQUE;

ALTER TABLE project
ADD COLUMN description TEXT;

CREATE TABLE api_key(
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES project,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
