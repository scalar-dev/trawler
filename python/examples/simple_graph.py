import trawler as tr

g = tr.Graph()
n = g.Dataset("urn:tr:dataset:/my-cool-dataset", name="My cool dataset")
g += n

g += g.Dataset(
    "urn:tr:dashboard:/my-cool-dashboard",
    name="My cool dashboard",
    prov__wasDerivedFrom=n.id,
)

g.store()
