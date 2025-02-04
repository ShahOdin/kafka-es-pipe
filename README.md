# Kafka to Elastic Search Pipe

As described in the specs, we want to publish messages from a kafka topic to elastic search such that:

- all the messages from a partition retain their processing order
- The messages are written to an index which is `${topic_name}_${yyyy-MM-dd-HH:mm}`
  and the rotation is every 15 minutes. Use the Kafka message timestamp for that.

Worth noting that there were some typos in the specs and the interpretations presented are my own efforts to align with the intended purpose:

- `{$topic_name}_${yyyy-MM-dd-HH:mm}` -> `${topic_name}_${yyyy-MM-dd-HH:mm}`
- `yyyy-MM-dd-HH:mm` -> `yyyy-MM-dd-HH-mm` since ES doesn't like `:` in index names.

Also, an avro schema was provided which the implementation ignores and uses some simple string messages. This was because:

- The business logic is oblivious to the message contents.
- I had some difficulty in populating sample schema compatible data using the rest api. 

As such, in the interest of time, I opted for an schema-free solution.

## Tests

The program is split in three major modules to aid testing:

- Kafka message consumption
- Business domain
- Elastic search indexing

Since the exercise is primarily an integration test and lite on business logic, no attempt were made at writing unit or integration tests, even though they are modelled in a way that would allow us to stub them, ie [FetchRecords](src/main/scala/FetchRecords.scala) can have alternative stub impls not necessarily the kafka one.  

End to end integration can be tested by running the service locally, as described below. 

## Local Run

from the root folder, simply run:

```shell
docker compose -f ./docker-compose.yaml down -v && docker compose -f ./docker-compose.yaml up
```

This will:

- set up the kafka container
- create a topic
- populate sample data into it
- run the app


### Check Elastic Search indexes

To check the resulting indices, perform the following curl:

```shell
curl -X GET "http://localhost:9200/_cat/indices?format=json" \
| jq 'map(.index)'
```

to see a list of all indices. And then in order to look at a given index, say:

```shell
indexName=clicks_2025-02-03-19-15
curl -X GET "http://localhost:9200/$indexName/_search?pretty" \
| jq '.hits.hits | map (._source)'
```


