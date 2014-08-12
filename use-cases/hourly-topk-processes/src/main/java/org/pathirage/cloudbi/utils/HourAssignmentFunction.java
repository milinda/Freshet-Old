package org.pathirage.cloudbi.utils;

import backtype.storm.tuple.Values;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

public class HourAssignmentFunction extends BaseFunction{

    @Override
    public void execute(TridentTuple tuple, TridentCollector collector) {
        long timestamp = tuple.getLongByField(Constants.FIELD_TIMESTAMP);
        long hourSinceEpoch = ((timestamp / 1000) / 60) / 60;

        collector.emit(new Values(hourSinceEpoch));
    }
}
