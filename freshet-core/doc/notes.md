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
