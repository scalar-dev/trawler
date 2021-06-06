import pandas as pd
from trawler import record, configure

configure("2759180d-ecf2-4e1b-aaa5-63bf57e8a54c", "5196f037-d825-4efc-89f2-9bd20ff855e0")

def test_record():
    data = {'first':  [1, 2, 3],
        'second': ['Hello', 'Goodbye', None]
        }

    df = pd.DataFrame (data, columns = ['first','second'])
    record(df, "/test/dataset1")
