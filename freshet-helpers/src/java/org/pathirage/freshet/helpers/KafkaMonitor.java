/*
 * (C) Copyright 2014 Milinda Pathirage.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pathirage.freshet.helpers;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.pathirage.freshet.data.StreamElement;
import org.pathirage.freshet.serde.StreamElementSerde;
import org.pathirage.freshet.serde.StreamElementSerdeFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KafkaMonitor {
    private final ConsumerConnector consumer;
    private ExecutorService executor;


    public KafkaMonitor(String zk){
        this.consumer = Consumer.createJavaConsumerConnector(createConsumerConfig(zk, UUID.randomUUID().toString()));
        this.executor = Executors.newCachedThreadPool();
    }

    public void registerTopic(String topic, int partitions){
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, partitions);

        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);

        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

        int threadNumber = 0;
        for (final KafkaStream stream : streams) {
            executor.submit(new MessageConsumer(stream, new StreamHandler(), threadNumber));
            threadNumber++;
        }
    }

    private static ConsumerConfig createConsumerConfig(String zk, String groupId) {
        Properties props = new Properties();
        props.put("zookeeper.connect", zk);
        props.put("group.id", groupId);
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");

        return new ConsumerConfig(props);
    }

    public static void main(String[] args) {
        KafkaMonitor kafkaMonitor = new KafkaMonitor("localhost:2181");
        kafkaMonitor.registerTopic("wikipedia-window", 1);
    }

    public class MessageConsumer implements Runnable {
        private KafkaStream ks;
        private StreamHandler sh;
        int tid;
        private StreamElementSerde seSerde;

        public MessageConsumer(KafkaStream ks, StreamHandler sh, int threadNumber){
            this.ks = ks;
            this.sh = sh;
            this.tid = threadNumber;
            this.seSerde = (StreamElementSerde)(new StreamElementSerdeFactory().getSerde(null, null));
        }

        @Override
        public void run() {
            ConsumerIterator<byte[], byte[]> itr = ks.iterator();

            while(itr.hasNext()){
                StreamElement se = this.seSerde.fromBytes(itr.next().message());
                sh.handle(se);
            }
        }
    }

    public  class StreamHandler {
        private Map<String, String> elements = new HashMap<>();

        public void handle(StreamElement se){
            if(!se.isDelete()){
                elements.put(se.getId(), se.getStringField("title"));
            } else {
                String s = elements.remove(se.getId());
                if(s != null){
                    System.out.println("Deleting item already seen: " + se.getId());
                }
            }
        }

    }


}
