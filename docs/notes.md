# Notes (01/27/2015)

## Current Status Of Freshet

- 

# Notes (01/07/2015)

I have window and select operators working. Need to get join and aggregates working. Other than that there are several
issues related to implementation.

 - Execution layer is not properly connected to DSL.
 - Operators are designed as StreamTasks and it prevent us from implementing stream optimizations like **fusion** and **fission**.
 - Stream element definition assumes a DB row like data. This prevent Freshet from supporting JSON/XML streams.
 - There is no proper representation for streaming query.
 - No way of supporting user defined functions, data types and operators.


Yi Pan's API for Samza StreamQL has several nice concepts such as separation of operator layer from execution layer. This
can be used as a base for implementing Freshet operator layer.

If we separate out operator layer from execution layer, we can easily support different back-ends based on different requirements.
Also this will allow us to do multiple levels of streaming optimizations. For example,

 - CQL level optimizations at operator layer and how operators are assigned to StreamTasks.
 - Then at StreamTask level we can do low level optimizations.

# Notes (12/02/2014)

## Stream Processing Language Calculus

Main entities:

- Inputs: These are queues
- Outputs: These are queues
- Operators: Take queues and variables as inputs and output queues and variables
- Variables: Used to maintain state

Execution Configuration:

- Function name to implementation map
- Variable names to variable value map
- Queue name to actual queue map

# TODOS (10/15/2014)

* Write a summary about CQL
* Define minimal set of CQL constructs to support
* Define set of samples which shows the usefulness of above subset
* Design the DSL based on above
* Define the internal representation of CQL
* CQL to Execution Plan
* Understand how IStream, DStream and RStream works and their semantics in CQL 

# KappaQL Query Layer Design Notes

* First problem is what is the serialization format of the events comes in to Kafka from outside world. For the 
  prototype we can use flat JSON objects.
* Then how we are going to define the stream:
    > Given that we choose JSON as the serialization format above, we can just use a mapping of fields to their types 
    > as the stream definition. Then the problem is how we annotate the ID/Primary Key of this stream in the definition.
    > And also which field contains the timestamp. In the first version its mandatory to have a timestamp field.
    > We can use something like follows.
    
    > ```clojure
    > (defstream stream
    >     (fields [:name :string :address :string :age :integer :timestamp :long])
    >     (pk :id)
    >     (ts :timestamp))
    > ```
    
* How the queries looks like
    > ```clojure
    > (select stream
    >     (fields [:name :firstname] :address :age)
    >     (where {:age (less-than 34)}))
    >```

## Queries Supported in v0.1

- Only **stream-to-stream** queries are supported. 
- **select** with **where** clause and **projection** is supported.
- *less-than*, *greater-than*, *equal*, *like*, *(greater-than|less-than)-or-equal* conditions composed with *AND* or *OR* is supported.


## Queries Supported in v0.2

- Aggregates support with **stream-to-relation** queries.
- In addition to v0.1 queries **group-by** and **aggregate** is supported.

    > ```clojure
    > (select stream
    >   (aggregate )
    >   (group-by ))
    > ```
