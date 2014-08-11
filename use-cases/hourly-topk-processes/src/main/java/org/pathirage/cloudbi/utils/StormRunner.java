package org.pathirage.cloudbi.utils;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.StormTopology;

public class StormRunner {

    public static void runTopologyLocally(StormTopology stormTopology, String topologyName, Config stormConfig, long runtimeInMills) throws InterruptedException {
        LocalCluster stormCluster = new LocalCluster();

        stormCluster.submitTopology(topologyName, stormConfig, stormTopology);

        Thread.sleep(runtimeInMills);

        stormCluster.killTopology(topologyName);
        stormCluster.shutdown();
    }
}
