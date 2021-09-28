---
Order: 1000
---
# Concepts
## Knowledge Graph
Trawler is based on the concept of a *Knowledge Graph*. A Knowledge Graph stores
facts about the world in a graph structure. Nodes within the graph represent
things - primarily data-related things like databases, tables, software jobs but
also real-world things like people and teams. Edges represent relationships -
e.g. dataset A is derived from dataset B or dataset C is owned by Alice.
Knowledge graphs have long been studied for use within the Semantic Web and
trawler makes use of a lot of these existing ideas and technologies.

## Ontology
Trawler's knowledge graph is based upon two components: entities (nodes) and
facets (attributes on a node or edges between two nodes). The allowed types of
entities and facets are (fairly loosely) restricted by an *Ontology*. Trawler
comes with a core ontology which can describe most important facts about
datasets, their schemas, their provenance etc. In addition, you can extend the
ontology with your own entity and facet types.

