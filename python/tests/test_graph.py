import trawler as tr
import json
import requests

def test_simple_graph():
  g = tr.Graph()

  db = g.SqlDatabase(
    "urn:tr:::postgres/example.com/postgres",
    name = "foo",
    tr__has = [
      g.SqlTable(
        "urn:tr:::postgres/example.com/postgres/foo",
        name = "foo"
      )
    ]
  )

  g.add(db)

  j = g.json()
  print(j)

  r = requests.post("http://localhost:9090/api/collect/63255f7a-e383-457a-9c30-4c7f95308749", json=j,
  headers={"Authorization": "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzZTQwY2U5MC1kN2M4LTQ5YzQtOTI3ZS05OWU3MGNhNmY3YmMiLCJpYXQiOjE2MjI5OTYxNjl9.DODGqPd8pj3OiTTm7VV2XhxGC5gyV7qV97HRVEUiOEY"})
  r.raise_for_status()

  print(r.json())


