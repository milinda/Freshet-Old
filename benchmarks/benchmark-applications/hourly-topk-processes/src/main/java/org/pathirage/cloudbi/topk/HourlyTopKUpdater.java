package org.pathirage.cloudbi.topk;

import com.clearspring.analytics.stream.frequency.CountMinSketch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.trident.operation.ReducerAggregator;
import storm.trident.tuple.TridentTuple;

public class HourlyTopKUpdater implements ReducerAggregator<CountMinSketch>{
    private static final Logger logger = LoggerFactory.getLogger(HourlyTopKUpdater.class);
    @Override
    public CountMinSketch init() {
        return null;
    }

    @Override
    public CountMinSketch reduce(CountMinSketch curr, TridentTuple tuple) {
        CountMinSketch updated = null;
        String process = tuple.getStringByField("process");

        if(curr == null){
            updated = new CountMinSketch(5, 5, 129345322);
        } else {
            updated = new CountMinSketch(5, 5, 129345322);
            try {
                CountMinSketch.merge(updated, curr);
            } catch (Exception e) {
                logger.warn("Unable to merge CountMinSketch objects.", e);
            }
        }

        updated.add(process, 1);

        return updated;
    }
}
