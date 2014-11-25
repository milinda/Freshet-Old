# Freshet 

[CQL](http://dl.acm.org/citation.cfm?id=1146463) based Clojure DSL for Apache Samza. 

Freshet is first step towards a complete implementation of Kappa Architecture based on extension to SQL [1] to sup- port continuous queries. Freshet implements a subset(select, windowing, aggregates) of CQL on top of Apache Samza. Freshet implements RStream and IStream relation-to-stream operators, tuple and time based sliding windows to convert streams to relations and basic relation to relation operators for implementing business logic. Following CQL, Freshet uses insert/delete stream to model instantaneous relations.

[1] Arvind Arasu, Shivnath Babu, and Jennifer Widom. 2006. The CQL continuous query language: semantic foundations and query execution. The VLDB Journal 15, 2 (June 2006), 121-142. DOI=10.1007/s00778-004-0147-z http://dx.doi.org/10.1007/s00778-004-0147-z

