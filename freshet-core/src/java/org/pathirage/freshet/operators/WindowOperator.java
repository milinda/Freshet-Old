/*
 * (C) Copyright 2014 Milinda Pathirage.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pathirage.freshet.operators;

import org.apache.samza.config.Config;
import org.apache.samza.metrics.Gauge;
import org.apache.samza.storage.kv.KeyValueStore;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.*;
import org.pathirage.freshet.Constants;
import org.pathirage.freshet.data.StreamElement;
import org.pathirage.freshet.utils.KVStorageBackedEvictingQueue;
import org.pathirage.freshet.utils.QueueNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Sliding window operator reads the input stream's tuples from input queue, update the sliding-window
 * synopsis, and outputs the insertion and deletions to this window to the output queues.
 * <p/>
 * 10/08/2014
 * ----------
 * - Only handles the tuple based sliding windows.
 * - Recovery is handled by persistent synopsis storage and Kafka queue. If the operator goes down, last successful
 * synopsis update will be there in local storage and last read tuple will be tracked internally by Samza. Upon
 * restart input stream read will last from the last read tuple and can recover the synopsis from local storage
 * assuming operator get restarted in same node.
 */
public class WindowOperator extends FreshetOperator implements StreamTask, InitableTask {
    private static Logger log = LoggerFactory.getLogger(WindowOperator.class);

    /* True if this a time-based sliding window. */
    private boolean timeBased;

    /* Range of time based sliding window in seconds. */
    private long range;

    /* sliding window can be divided into slots. */
    private long slotSize;

    /* True if this a tuple-based sliding window. */
    private boolean tupleBased;

    /* Max tuples in tuple based sliding window */
    private int rows;

    /* CQL uses concept called synopses to implement windowing. This stores
     * synopsis as key/value pairs. This assumes every stream element has unique id. */
    private KeyValueStore<String, QueueNode> store;

    private KeyValueStore<String, String> metadataStore;

    /* Window size gauge metric for reporting */
    private Gauge windowSizeGauge;

    /* Current size of the window to handle handle/drop events to/from window as needed. */
    private AtomicLong currentWindowSize = new AtomicLong(0);

    /* Window handler. */
    private WindowHandler windowHandler;

    @Override
    public void init(Config config, TaskContext taskContext) throws Exception {
        this.config = config;

        initOperator(FreshetOperatorType.WINDOW);

        String range = config.get(Constants.CONF_WINDOW_RANGE, Constants.CONST_STR_UNDEFINED);
        if (!range.equals(Constants.CONST_STR_UNDEFINED)) {
            this.range = Long.valueOf(range);

            String slotSize = config.get(Constants.CONF_WINDOW_RANGE_SLOT_SIZE, Constants.CONST_STR_UNDEFINED);
            if (!slotSize.equals(Constants.CONST_STR_UNDEFINED)) {
                this.slotSize = Long.valueOf(slotSize);
            } else {
                this.slotSize = this.range;
            }

            timeBasedWindow(true);
        }

        String rows = config.get(Constants.CONF_WINDOW_ROWS, Constants.CONST_STR_UNDEFINED);
        if (!rows.equals(Constants.CONST_STR_UNDEFINED) && range.equals(Constants.CONST_STR_UNDEFINED)) {
            this.rows = Integer.valueOf(rows);
            timeBasedWindow(false);
        } else {
            timeBasedWindow(true);
            log.warn(Constants.WARN_BOTH_ROWS_AND_RANGE_DEFINED);
        }

        this.store = (KeyValueStore<String, QueueNode>) taskContext.getStore("windowing-synopses");
        this.metadataStore = (KeyValueStore<String, String>) taskContext.getStore("windowing-metadata");

        // TODO: Implement time based sliding window handler.
        if(this.tupleBased && !this.timeBased){
            this.windowHandler = new TupleBasedSlidingWindowHandler(this.rows, store, metadataStore, this.system);
        }

        this.windowSizeGauge = taskContext.getMetricsRegistry().newGauge(getClass().getName(), "window-size", 0);
    }

    @Override
    public void process(IncomingMessageEnvelope incomingMessageEnvelope,
                        MessageCollector messageCollector, TaskCoordinator taskCoordinator) throws Exception {
        windowHandler.handle((StreamElement) incomingMessageEnvelope.getMessage(), messageCollector);
    }

    private void timeBasedWindow(boolean b) {
        if (b) {
            this.timeBased = true;
            this.tupleBased = false;
        } else {
            this.timeBased = false;
            this.tupleBased = true;
        }
    }

    public interface WindowHandler {
        public void handle(StreamElement streamElement, MessageCollector messageCollector);
    }

    public class TupleBasedSlidingWindowHandler implements WindowHandler {
        private int maxSize;
        private KeyValueStore<String, String> metadataStore;
        private KeyValueStore<String, QueueNode> store;
        private KVStorageBackedEvictingQueue evictingQueue;
        private String system;

        public TupleBasedSlidingWindowHandler(int maxSize,
                                              KeyValueStore<String, QueueNode> store,
                                              KeyValueStore<String, String> metadataStore,
                                              String system) {
            this.maxSize = maxSize;
            this.metadataStore = metadataStore;
            this.store = store;
            this.evictingQueue = new KVStorageBackedEvictingQueue(maxSize, this.store, this.metadataStore);
            this.system = system;
        }

        public void handle(StreamElement streamElement, MessageCollector messageCollector) {
            log.info("Incoming stream element id: " + streamElement.getId());
            log.info("Incoming stream element titles: " + streamElement.getStringField("title"));
            StreamElement evicted = evictingQueue.add(streamElement.getId(), streamElement);
            if (evicted != null) {
                /* Sending element deleted from window to down stream for processing.
                 * Need to set delete property to of StreamElement true. */
                evicted.setDelete(true);
                messageCollector.send(new OutgoingMessageEnvelope(new SystemStream(system, downStreamTopic),
                        evicted.getId(), evicted));
            }

            /* Sending insert to window element to down stream for processing. */
            streamElement.setDelete(false);
            messageCollector.send(new OutgoingMessageEnvelope(new SystemStream(system, downStreamTopic),
                    streamElement));
        }
    }
}
