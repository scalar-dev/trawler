from datetime import datetime, date
from decimal import Decimal
import logging

LOG = logging.getLogger(__name__)

def sql_query(conn, sql):
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


PRIMITIVE_TYPES = {int, str, bool, float}

def get_column_metrics(engine, field_type, table_name, column_name):
    with engine.connect() as conn:
        out = {
            "metrics__nullRatio": sql_query(
                conn,
                f"""
                SELECT
                CAST(SUM(CASE WHEN {column_name} IS NULL THEN 1 ELSE 0 END) as DOUBLE PRECISION) / COUNT(*)
                FROM {table_name};
                """,
            ),
        }

        is_primitive = False
        try:
            is_primitive = field_type.python_type in PRIMITIVE_TYPES
        except NotImplementedError:
            pass

        if is_primitive:
            out["metrics__uniqueRatio"] = sql_query(
                conn,
                f"""
                SELECT
                CAST(COUNT(DISTINCT({column_name})) as DOUBLE PRECISION) / COUNT(*)
                FROM {table_name};
                """,
            )

            out["metrics__max"] = sql_query(
                conn,
                f"""
                SELECT
                MAX({column_name})
                FROM {table_name};
                """,
            )

            out["metrics__min"] = sql_query(
                conn,
                f"""
                SELECT
                MIN({column_name})
                FROM {table_name};
                """,
            )

        return out


def get_table_metrics(engine, table_name):
    with engine.connect() as conn:
        return {
            "metrics__count": next(
                conn.execute(
                    f"""
                SELECT COUNT(*)
                FROM {table_name};
                """
                )
            )[0]
        }


