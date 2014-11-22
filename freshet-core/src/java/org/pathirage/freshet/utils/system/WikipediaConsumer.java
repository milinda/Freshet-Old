/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.pathirage.freshet.utils.system;

import org.apache.samza.Partition;
import org.apache.samza.metrics.MetricsRegistry;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.SystemStreamPartition;
import org.apache.samza.util.BlockingEnvelopeMap;
import org.pathirage.freshet.data.StreamElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class WikipediaConsumer extends BlockingEnvelopeMap implements WikipediaFeed.WikipediaFeedListener {
    private static final Logger log = LoggerFactory.getLogger(WikipediaConsumer.class);
    private final List<String> channels;
    private final String systemName;
    private final WikipediaFeed feed;

    public WikipediaConsumer(String systemName, WikipediaFeed feed, MetricsRegistry registry) {
        this.channels = new ArrayList<String>();
        this.systemName = systemName;
        this.feed = feed;
    }

    public void onEvent(final WikipediaFeed.WikipediaFeedEvent event) {
        SystemStreamPartition systemStreamPartition = new SystemStreamPartition(systemName, event.getChannel(), new Partition(0));

        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("rawEvent", event.getRawEvent());
        fields.put("channel", event.getChannel());
        fields.put("source", event.getChannel());
        fields.put("time", event.getTime());

        Date now = new Date();

        StreamElement se = new StreamElement(fields, now.getTime(), now.getTime(), null);

        try {
            put(systemStreamPartition, new IncomingMessageEnvelope(systemStreamPartition, null, null, se));
        } catch (Exception e) {
            log.error("Error sending messages downstream.", e);
        }
    }

    @Override
    public void register(SystemStreamPartition systemStreamPartition, String startingOffset) {
        super.register(systemStreamPartition, startingOffset);

        channels.add(systemStreamPartition.getStream());
    }

    @Override
    public void start() {
        feed.start();

        for (String channel : channels) {
            feed.listen(channel, this);
        }
    }

    @Override
    public void stop() {
        for (String channel : channels) {
            feed.unlisten(channel, this);
        }

        feed.stop();
    }
}
