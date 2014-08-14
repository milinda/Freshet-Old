package org.pathirage.cloudbi.decoder;

import backtype.storm.tuple.Values;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TMemoryInputTransport;
import org.pathirage.cloudbi.thrift.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

public class ThriftEventDecoderFunction extends BaseFunction {
    private static final Logger log = LoggerFactory.getLogger(ThriftEventDecoderFunction.class);

    @Override
    public void execute(TridentTuple tuple, TridentCollector collector) {
        byte[] thriftEncodedEvent = tuple.getBinaryByField("bytes");

        if(thriftEncodedEvent != null){
            Event e = new Event();
            try {
                e.read(new TBinaryProtocol(new TMemoryInputTransport(thriftEncodedEvent)));
                collector.emit(new Values(e.getId(), e.getStream(), e.getEventType(), e.getTimestamp(), e.getData(), e.getVersion()));
            } catch (TException te) {
                log.error("Unable to decode thrift encoded event.", te);
            }
        }
    }
}
