package org.pathirage.cloudbi.decoder;

import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

public class ThriftEventDecoderFunction extends BaseFunction {
    @Override
    public void execute(TridentTuple tuple, TridentCollector collector) {
        byte[] thriftEncodedEvent = tuple.getBinaryByField("bytes");

        if(thriftEncodedEvent != null){
        }
    }
}
