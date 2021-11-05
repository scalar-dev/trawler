FROM python:3.9.7-slim

ENV PYTHONFAULTHANDLER=1 \
  PYTHONUNBUFFERED=1 \
  PYTHONHASHSEED=random \
  PIP_NO_CACHE_DIR=off \
  PIP_DISABLE_PIP_VERSION_CHECK=on \
  PIP_DEFAULT_TIMEOUT=100 \
  POETRY_VERSION=1.1.10

# System deps:
RUN pip install "poetry==$POETRY_VERSION"

# Copy only requirements to cache them in docker layer
WORKDIR /app
COPY poetry.lock pyproject.toml /app/

# Project initialization:
RUN poetry config virtualenvs.create false \
&& poetry install --no-root --no-dev --no-interaction --no-ansi --extras sql

# Creating folders, and files for a project:
COPY . /app

ENV PYTHONPATH="/app/src:$PYTHONPATH"

ENTRYPOINT [ "python", "/app/src/trawler/agent/main.py" ]
