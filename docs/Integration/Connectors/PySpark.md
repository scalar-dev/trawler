# PySpark
Trawler can instrument the `PySpark` APIs in order to automatically extract metadata and ingest it via the collector.

```python
import trawler as tr

df = spark.read.load("examples/src/main/resources/users.parquet")
tr.record(df)
```