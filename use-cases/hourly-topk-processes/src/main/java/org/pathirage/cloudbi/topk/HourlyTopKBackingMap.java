package org.pathirage.cloudbi.topk;

import com.clearspring.analytics.stream.frequency.CountMinSketch;
import storm.trident.state.map.IBackingMap;

import java.util.List;

public class HourlyTopKBackingMap implements IBackingMap<CountMinSketch> {
    @Override
    public List<CountMinSketch> multiGet(List<List<Object>> keys) {
        return null;
    }

    @Override
    public void multiPut(List<List<Object>> keys, List<CountMinSketch> vals) {

    }
}
