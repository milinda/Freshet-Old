# Freshet Benchmark

Freshet benchmark measure various aspects of Freshet hybrid monitoring platform including:

* Performance under multi-tenant setup
* Multi-tenant scalability
* Reliability and Behavior under failures
* Usability of programming model


## Performance and scalability benchmark design

```
+------------------------------+     +---------------+
| Multi-tenant Event Generator | --> | Kafka Cluster |
+------------------------------+     +---------------+
```