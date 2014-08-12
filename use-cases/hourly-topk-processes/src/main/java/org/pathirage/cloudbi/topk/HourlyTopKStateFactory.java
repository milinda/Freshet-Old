package org.pathirage.cloudbi.topk;

import backtype.storm.task.IMetricsContext;
import storm.trident.state.State;
import storm.trident.state.StateFactory;

import java.util.Map;

public class HourlyTopKStateFactory implements StateFactory {
    @Override
    public State makeState(Map conf, IMetricsContext metrics, int partitionIndex, int numPartitions) {
        return null;
    }
}
