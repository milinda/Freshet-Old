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

package org.pathirage.freshet.utils;

import org.apache.samza.storage.kv.KeyValueStore;
import org.pathirage.freshet.data.StreamElement;

import java.util.concurrent.atomic.AtomicInteger;

public class KVStorageBackedEvictingQueue {

    private static final String CONST_HEAD = "kvstoragebacked-eq-head";
    private static final String CONST_TAIL = "kvstoragebacked-eq-tail";
    private static final String CONST_SIZE = "kvstoragebacked-eq-size";
    private static final String CONST_MAX_SIZE = "kvstoragebacked-eq-max-size";
    private static final String CONST_UNDEFINED = "kvstoragebacked-undefined";

    private KeyValueStore<String, String> metadataStore;
    private KeyValueStore<String, QueueNode> store;
    private int maxSize;
    private AtomicInteger size;

    public KVStorageBackedEvictingQueue(int maxSize,
                                        KeyValueStore<String, QueueNode> store,
                                        KeyValueStore<String, String> metadataStore) {
        this.metadataStore = metadataStore;
        this.store = store;

        String persistedMaxSize = this.metadataStore.get(CONST_MAX_SIZE);
        if (persistedMaxSize != null) {
            this.maxSize = Integer.valueOf(persistedMaxSize);
        } else {
            this.maxSize = maxSize;
            this.metadataStore.put(CONST_MAX_SIZE, Integer.toString(maxSize));
        }

        String size = this.metadataStore.get(CONST_SIZE);
        if (size != null) {
            this.size = new AtomicInteger(Integer.valueOf(size));
        } else {
            this.size = new AtomicInteger(0);
            this.metadataStore.put(CONST_SIZE, Integer.toString(0));
        }

        if (this.size.equals(new AtomicInteger(0))) {
            this.metadataStore.put(CONST_HEAD, CONST_UNDEFINED);
            this.metadataStore.put(CONST_TAIL, CONST_UNDEFINED);
        }
    }

    public StreamElement add(String key, StreamElement value) {
        int newSize;
        QueueNode head;
        QueueNode tail;

        String headKey = metadataStore.get(CONST_HEAD);
        String tailKey = metadataStore.get(CONST_TAIL);

        QueueNode newElement = new QueueNode();
        newElement.setValue(value);
        newElement.setNext(headKey);
        newElement.setPrev(CONST_UNDEFINED);

        // Update the old head prev ot point new head
        head = store.get(headKey);
        head.setPrev(key);

        // Put the old head with updated info
        store.put(headKey, head);

        // Put the new head.
        store.put(key, newElement);

        // Update metadata
        metadataStore.put(CONST_HEAD, key);

        if(size.get() < maxSize){
            newSize = size.incrementAndGet();
            metadataStore.put(CONST_SIZE, Integer.toString(newSize));

            return null;
        } else {
            tail = store.get(tailKey);

            // Old tail's prev becomes new tail
            metadataStore.put(CONST_TAIL, tail.getPrev());

            // Delete the old tail
            store.delete(tailKey);

            return tail.getValue();
        }
    }
}
