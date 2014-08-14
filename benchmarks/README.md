# Freshet Benchmark

Freshet benchmark measure various aspects of Freshet hybrid monitoring platform including:

* Performance under multi-tenant setup
* Multi-tenant scalability
* Reliability and Behavior under failures
* Usability of programming model

## Interesting Problems

* Multiple Storm clusters vs multiple topologies in same Storm clusters for handling multi-tenancy
* When using single Storm cluster, how to ensure fair scheduling between various topologies from different tenants


## Performance and scalability benchmark design

```
                     Multiple tenants are simulated
                     using multiple topics
                     ==============================
                                  |
+------------------------------+  |  +---------------+     +-------------------------+
| Multi-tenant Event Generator | --> | Kafka Cluster | --> | Apache Storm Topologies |
+------------------------------+     +---------------+     +-------------------------+
                                                                  |
                                                                  |
                                                     ======================================
                                                     Multiple topologies per tenant running
                                                     different analytics tasks
```