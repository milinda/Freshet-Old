package org.pathirage.cloudbi.topk;

import backtype.storm.Config;
import org.pathirage.cloudbi.utils.StormRunner;

public class HourlyTopK {
    public static void main(String[] args) throws InterruptedException {
        String zkStr = "localhost:2181";
        String topic = "tenant1";
        StormRunner.runTopologyLocally(HourlyTopKTopologyBuilder.buildTopology(zkStr, topic), "hourly-topk-processes", new Config(), 10000);
    }
}
