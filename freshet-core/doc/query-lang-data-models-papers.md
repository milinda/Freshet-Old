# Query Languages and Data Models for Database Sequences and Data Streams

Studies limitations of **relation algebra** and **SQL** in supporting queries over data streams
and present alternative query language and data model.

Notion of **Nonblocking Queries**, only continuous queries that can be supported on data streams.
 They are equivalent to monotonic queries.

## NB-Completeness

RA's ability to express all mono- tonic queries expressible in RA using only the monotonic
operators of RA.

**RA is not NB-Complete and SQL is not more powerful than RA**

## Solutions

- User defined aggregates natively coded in SQL
- A generalisation of union operator to support the merging of multiple streams according to their timestamps

## Blocking Operators Are Not Allowed in Streaming Context

- NOT IN
- NOT EXISTS
- ALL
- EXCEPT

**all monotonic queries, and only those, can be expressed using nonblocking computations**

Cursor based programming model cannot be supported in data stream management systems.

**Precense of timestamp is required for query completeness.**

## Related Work

- Tapestry: Append-only databases supporting continuous queries


## Definitions

- Sequence consists of ordered tuples, where as the order is immaterial in relational tables.
- Streams are sequences of unbounded length, where the tuples are ordered by, and possibly time-stamped with, their arrival time.
- Blocking query operator is a query operator that is unable to produce the first tuples of the output until it has seen the entire input.
- A nonblocking query operator is one that produces all the tuples of output before it has detected the end of the input.

**Query *Q* on a stream *S* can be implemented by a nonblocking operator iff *Q(S)* is monotonic with respect to *presequence*.**

- *physically ordered relations*: those where only the relative positions of tuples in sequence are of significance.
- *unordered relations*: traditional db relations, cal Codd's relaitons.
- *logically ordered relations*: sequences where tuples are ordered by their timestamps or other logical keys.


*Physical order* model is conductive to great expressive power, but cannot support binary operators as naturally as it does for unary ones. For example
SQL *union* of two tabless T1 and T2 is nromally implemented by first returning all the tuples in T1 and then all the tuples in T2. The resulting operator
is not suitable for continuous queries, since it is partially blocking with respect to T1. These issues can be resolved by using Codd's relations or logically
order relations.


## Open Problem

**What generalization of the relation data model, algebra, and query languages are needed to deal with sequences and streams.**
