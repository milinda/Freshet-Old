package org.pathirage.cloudbi.topk;

import backtype.storm.task.IMetricsContext;
import com.clearspring.analytics.stream.frequency.CountMinSketch;
import com.google.common.collect.AbstractIterator;
import org.apache.commons.lang3.tuple.Pair;
import redis.clients.jedis.Jedis;
import storm.trident.state.OpaqueValue;
import storm.trident.state.State;
import storm.trident.state.StateFactory;
import storm.trident.state.map.IBackingMap;
import storm.trident.state.map.OpaqueMap;

import java.nio.ByteBuffer;
import java.util.*;

public class HourlyTopKBackingMap implements IBackingMap<OpaqueValue> {
    private Jedis redisClient;

    public HourlyTopKBackingMap(Map conf){
    }

    public static StateFactory FACTORY = new StateFactory() {
        @Override
        public State makeState(Map conf, IMetricsContext metrics, int partitionIndex, int numPartitions) {
            return OpaqueMap.build(new HourlyTopKBackingMap(conf));
        }
    };

    @Override
    public List<OpaqueValue> multiGet(List<List<Object>> keys) {
        List<String> singleKeys = toSingleKeys(keys);
        List<OpaqueValue> values = new ArrayList<OpaqueValue>();

        for(String key : singleKeys){
            Map<byte[], byte[]> fields = redisClient.hgetAll(key.getBytes());

            values.add(new OpaqueValue(
                    ByteBuffer.wrap(fields.get("txid".getBytes())).getLong(),
                    CountMinSketch.deserialize(fields.get("curr".getBytes())),
                    CountMinSketch.deserialize(fields.get("prev".getBytes()))));
        }

        return values;
    }

    @Override
    public void multiPut(List<List<Object>> keys, List<OpaqueValue> vals) {
        List<String> singleKeys = toSingleKeys(keys);

        for(Pair<String, OpaqueValue> pair : zip(singleKeys, vals)){
            byte[] prev = CountMinSketch.serialize((CountMinSketch) pair.getValue().getPrev());
            byte[] cur = CountMinSketch.serialize((CountMinSketch) pair.getValue().getCurr());
            long currTxid = pair.getValue().getCurrTxid();

            Map<byte[],byte[]> fields = new HashMap<byte[], byte[]>();

            fields.put("txid".getBytes(), ByteBuffer.allocate(8).putLong(currTxid).array());
            fields.put("prev".getBytes(), prev);
            fields.put("curr".getBytes(), cur);

            redisClient.hmset(pair.getKey().getBytes(), fields);
        }
    }

    public static <L,R> Iterable<Pair<L,R>> zip(final Iterable<L> leftCol, final Iterable<R> rightCol) {
        return new Iterable<Pair<L,R>>() {
            @Override
            public Iterator<Pair<L, R>> iterator() {
                final Iterator<L> leftItr = leftCol.iterator();
                final Iterator<R> rightItr = rightCol.iterator();
                return new AbstractIterator<Pair<L,R>>() {
                    @Override
                    protected Pair<L, R> computeNext() {
                        if (leftItr.hasNext() && rightItr.hasNext()) {
                            return Pair.of(leftItr.next(), rightItr.next());
                        } else {
                            return endOfData();
                        }
                    }};
            }};
    }
    private List<String> toSingleKeys(List<List<Object>> keys) {
        List<String> singleKeys = new ArrayList<String>();

        for(List<Object> key : keys){
            // Single key because groupBy by hourSinceEpoch
            singleKeys.add((String)key.get(0));
        }

        return singleKeys;
    }
}
