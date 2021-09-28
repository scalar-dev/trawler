# pandas

```python
import trawler as tr
import pandas

df = pandas.read_csv("myfile.csv")
tr.record(df)
```

Trawler can also wrap the `pandas` API in order to automatically instrument dataframes.

```python
import trawler as tr
import pandas

tr.init(local_file_namespace="myjob")
tr.instrument(pandas)

df = pandas.read_csv("myfile.csv")

df.to_csv("output.csv")

tr.flush()
```

If you prefer less magic, you can also do:

```python
import trawler as tr
import trawler.pandas

tr.init(local_file_namespace="myjob")

df1 = trawler.pandas.read_csv("myfile.csv")

df2 = trawler.pandas.read_sql(...)

tr.flush()
```
