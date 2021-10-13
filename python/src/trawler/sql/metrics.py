from datetime import datetime, date
from decimal import Decimal
import logging

LOG = logging.getLogger(__name__)

def sql_query(conn, sql):
    LOG.info(f"running SQL: {sql}")
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


def get_column_metrics(engine, table_name, column_name):
    with engine.connect() as conn:
        return {
            "metrics__nullRatio": sql_query(
                conn,
                f"""
                SELECT
                CAST(SUM(CASE WHEN {column_name} IS NULL THEN 1 ELSE 0 END) as DOUBLE PRECISION) / COUNT(*)
                FROM {table_name};
                """,
            ),
            "metrics__uniqueRatio": sql_query(
                conn,
                f"""
                SELECT
                CAST(COUNT(DISTINCT({column_name})) as DOUBLE PRECISION) / COUNT(*)
                FROM {table_name};
                """,
            ),
            "metrics__max": sql_query(
                conn,
                f"""
                SELECT
                MAX({column_name})
                FROM {table_name};
                """,
            ),
            "metrics__min": sql_query(
                conn,
                f"""
                SELECT
                MIN({column_name})
                FROM {table_name};
                """,
            ),
        }


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


