package org.pathirage.cloudbi.topk;

import backtype.storm.generated.StormTopology;
import backtype.storm.tuple.Fields;
import org.pathirage.cloudbi.decoder.ThriftEventDecoderFunction;
import org.pathirage.cloudbi.utils.HourAssignmentFunction;
import storm.kafka.ZkHosts;
import storm.kafka.trident.OpaqueTridentKafkaSpout;
import storm.kafka.trident.TridentKafkaConfig;
import storm.trident.Stream;
import storm.trident.TridentTopology;

public class HourlyTopKTopologyBuilder {

    public static StormTopology buildTopology(String zkHostsStr, String topic){
        ZkHosts zkHosts = new ZkHosts(zkHostsStr);

        TridentTopology indexerTopology = new TridentTopology();

        TridentKafkaConfig tridentKafkaConfig = new TridentKafkaConfig(zkHosts, topic);
        OpaqueTridentKafkaSpout kafkaSpout = new OpaqueTridentKafkaSpout(tridentKafkaConfig);

        Stream eventStream = indexerTopology.newStream("events", kafkaSpout);
        eventStream.each(new Fields("bytes"), new ThriftEventDecoderFunction(), new Fields("stream", "type", "event", "id"))
                .each(new Fields("stream", "type", "event", "id"), new HourAssignmentFunction(), new Fields("hourSinceEpoch"))
                .each(new Fields("stream", "type", "event", "id", "hourSinceEpoch"), new InstanceCreatedEventFilter())
                .project(new Fields("hourSinceEpoch", "process", "instance"))
                .groupBy(new Fields("hourSinceEpoch"))
                .persistentAggregate(HourlyTopKBackingMap.FACTORY, new Fields("hourSinceEpoch", "process", "instance"), new HourlyTopKUpdater(), new Fields("hourlyTopK"));

        return indexerTopology.build();
    }
}
