# Overview

Trawler is a data catalogue and metadata platform based on a [knowledge graph](https://en.wikipedia.org/wiki/Knowledge_graph).

Trawler's primary purpose is storing, searching and visualising *metadata* -
data about data. This is especially useful as organisations scale and their data
landscape becomes more complex. Knowledge about what data lives where, how it is
structured and where it comes from may be scattered around making it difficult
to access and use. Furthermore, static data documentation quickly becomes out of
date and may not represent the current state of the world. Trawler aims to solve
these problems.

## What is metadata?
Metadata is everything meaningful about your data except for the data itself
(although it may be derived from it). Trawler has a flexible ontology-based data
model which allows for storing almost any kind of metadata. However, commonly
this would include:

- What is the name of the dataset?
- How can I access it?
- What fields does it contain and what types do they have?
- When was it last updated?
- Who is responsible for it?
- How many rows or records does it have currently? (And how many did it have last week?)
- What datasets or systems was this data derived from?
- etc.

Once collected, metadata can be put to a variety of uses, including:

- Data discovery
- Data quality monitoring and alerting
- Change control
- Debugging data pipelines
- Data integration

