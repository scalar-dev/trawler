# JSON-LD
`JSON-LD` is trawler's native data ingest format. You can add or update data within trawler using a simple `POST` request.

+++ curl (trawler.dev)
```bash
export TRAWLER_TOKEN=<token>
curl -H "Authorization: Bearer $TRAWLER_TOKEN" \
  --data @data.jsonld https://trawler.dev/api/collect
```
+++ curl (self-hosted)
```bash
export TRAWLER_TOKEN=<token>
export TRAWLER_HOST=<hostname>
curl -H "Authorization: Bearer $TRAWLER_TOKEN" \
  --data @data.jsonld "$TRAWLER_HOST/api/collect"
```
+++