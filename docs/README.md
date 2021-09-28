# Overview
## What is it?
Trawler is a tool for collecting, curating and cataloguing metadata - data about data.  This allows teams (or individuals) to assemble knowledge about where different pieces of data live, what they look like (schemas and metrics) and how they are processed to produce derived data products, machine learning models and dashboards.

## Why use it?
Modern organisations, small or large, depend on a growing pile of data for their core operations. These datasets often live across a variety of systems: relational databases, cloud storage, queues, filesystems etc. Larger organisations often tame this complexity by pulling data into centralised repositories - data warehouses or data lakes - for analysis. Data about these data (i.e. metadata) are then curated in data catalogues.

These approaches work well but bring other problems. They are abstractions, quite high-level ones at that, and thus they intentionally drop messy details about the real underlying data. This can, and often does, lead to issues. For instance, you discover that a table in your data lake has stopped growing. This could be a problem in the upstream data source or in the way you are integrating with it. To investigate further, you may need to look at the logs of your ingestion process or connect to the source database. 

Trawler takes a different, but complementary approach. Trawler embraces all of the messy details of all of your data systems. It integrates with your relational databases, kafka streams, dashboards, data frames or data lakes and pulls out a firehose of metadata. This gives a much richer and more timely picture of what's going on with your data.

## Concepts
### Knowledge Graph
Trawler is based on the concept of a *Knowledge Graph*. A Knowledge Graph stores facts about the world in a graph structure. Nodes within the graph represent things - primarily data-related things like databases, tables, software jobs but also real-world things like people and teams. Edges represent relationships - e.g. dataset A is derived from dataset B or dataset C is owned by Alice. Knowledge graphs have long been studied for use within the Semantic Web and trawler makes use of a lot of these existing ideas and technologies.

### Ontology
Trawler's knowledge graph is based upon two components: entities (nodes) and facets (attributes on a node or edges between two nodes). The allowed types of entities and facets are (fairly loosely) restricted by an *Ontology*. Trawler comes with a core ontology which can describe most important facts about datasets, their schemas, their provenance etc. In addition, you can extend the ontology with your own entity and facet types.

## Principles
### Embrace messiness
Trawler aims to organize and visualize but not to drop information. By embracing the messiness the real world, you can get setup quickly and also have confidence that trawler won't shield you from the truth. If you have an existing data lake, trawler can complement it by hooking into your underlying data assets and connecting the dots.

### Deploy simply
Fancy data systems are no use unless they are deployed and working. Our basic setup just needs a single running service and a postgres database.

### Scale as you scale
Organisations cover many orders of magnitude in terms of the complexity of their data systems. Trawler aims to be useful and manageable for a team of 1 within 20 mins. From there, we should be able to scale as your organisation grows.

### Customise to fit your needs
No two organisations are the same. Our core ontology should be more than enough to get started. If and when you decide it no longer fits your needs, it can be simply extended to cover additional entity types.