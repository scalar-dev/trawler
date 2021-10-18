from sqlalchemy.sql.sqltypes import TypeEngine
from sqlalchemy.dialects.postgresql import JSON, JSONB
from datetime import datetime, date
from decimal import Decimal
import logging
import sqlalchemy
import itertools

LOG = logging.getLogger(__name__)

def sql_query_scalar(conn, sql):
    LOG.debug(f"running SQL: {sql}")
    try:
        val = next(conn.execute(sql))[0]
        if isinstance(val, datetime):
            return str(val)
        elif isinstance(val, date):
            return str(val)
        if isinstance(val, Decimal):
            return str(val)
        else:
            return val
    except:
        return None

def histogram_value(conn, table_name, field_name, buckets=20):
    min_max = conn.execute(f"""
        SELECT MIN({field_name}) as min_val, MAX({field_name}) AS max_val
        FROM {table_name}
    """)
    row = next(min_max)
    min_val = row[0]
    max_val = row[1]

    if min_val == max_val:
        return None

    res = conn.execute(
    f"""SELECT 
        WIDTH_BUCKET({field_name}, {min_val}, {max_val}, {buckets}) AS buckets, COUNT(*)
        FROM {table_name}
        GROUP BY buckets
        ORDER BY buckets
    """)

    bucket_width = (max_val - min_val) / buckets

    counts = {row[0]: row[1] for row in res}

    return {
        "min": min_val,
        "max": max_val,
        "buckets": buckets,
        "counts": [
            counts.get(idx, 0)
            for idx in itertools.chain([None], range(buckets + 1))
        ]
    }

def sql_query_pairs(conn, sql):
    return [
        {"key": row[0], "value": row[1]}
        for row in conn.execute(sql)
    ]

def is_primitive_type(field_type: TypeEngine):
    try:
        return field_type.python_type in {int, str, bool, float}
    except NotImplementedError:
        pass

def is_numeric(field_type: TypeEngine):
    try:
        return field_type.python_type in {int, float}
    except NotImplementedError:
        pass

def is_date(field_type: TypeEngine):
    try:
        return field_type.python_type in {datetime, date}
    except NotImplementedError:
        pass

def get_column_metrics(engine, field_type, table_name, column_name):
    with engine.connect() as conn:
        out = {
            "metrics__nullRatio": sql_query_scalar(
                conn,
                f"""
                SELECT
                CAST(SUM(CASE WHEN {column_name} IS NULL THEN 1 ELSE 0 END) as DOUBLE PRECISION) / COUNT(*)
                FROM {table_name};
                """,
            ),
        }

        if is_primitive_type(field_type):
            out["metrics__uniqueRatio"] = sql_query_scalar(
                conn,
                f"""
                SELECT
                CAST(COUNT(DISTINCT({column_name})) as DOUBLE PRECISION) / COUNT(*)
                FROM {table_name};
                """,
            )

        if is_numeric(field_type) or is_date(field_type):
            out["metrics__max"] = sql_query_scalar(
                conn,
                f"""
                SELECT
                MAX({column_name})
                FROM {table_name};
                """,
            )

            out["metrics__min"] = sql_query_scalar(
                conn,
                f"""
                SELECT
                MIN({column_name})
                FROM {table_name};
                """,
            )

        if is_numeric(field_type):
            hist = histogram_value(
                conn,
                table_name,
                column_name
            )

            if hist:
                out["metrics__histogram"] = hist

        if field_type == JSON:
            out["metrics__countByType"] = sql_query_pairs(
                f"""
                SELECT json_typeof({column_name}), COUNT(*)
                FROM {table_name}
                GROUP BY jsonb_typeof({column_name})
                """
            )
        elif field_type == JSONB:
            out["metrics__countByType"] = sql_query_pairs(
                f"""
                SELECT jsonb_typeof({column_name}), COUNT(*)
                FROM {table_name}
                GROUP BY jsonb_typeof({column_name})
                """
            )

        return out


def get_table_metrics(engine, table_name):
    with engine.connect() as conn:
        return {
            "metrics__count": sql_query_scalar(
                conn,
                f"""
                SELECT COUNT(*)
                FROM {table_name};
                """
            )
        }
