package org.pathirage.cloudbi.topk;

import com.clearspring.analytics.stream.frequency.CountMinSketch;
import storm.trident.state.OpaqueValue;
import storm.trident.state.map.IBackingMap;
import storm.trident.state.map.OpaqueMap;

public class HourlyTopKState extends OpaqueMap<CountMinSketch> {
    protected HourlyTopKState(IBackingMap<OpaqueValue> backing) {
        super(backing);
    }
}
