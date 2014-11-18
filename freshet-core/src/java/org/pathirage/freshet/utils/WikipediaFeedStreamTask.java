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

import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.StreamTask;
import org.apache.samza.task.TaskCoordinator;
import org.pathirage.freshet.utils.system.WikipediaFeed;

import java.util.Map;

public class WikipediaFeedStreamTask implements StreamTask {
    private static final SystemStream OUTPUT_STREAM = new SystemStream("kafka", "wikipedia-raw");

    @Override
    public void process(IncomingMessageEnvelope incomingMessageEnvelope, MessageCollector messageCollector, TaskCoordinator taskCoordinator) throws Exception {
        Map<String, Object> outgoingMap = WikipediaFeed.WikipediaFeedEvent.toMap((WikipediaFeed.WikipediaFeedEvent) incomingMessageEnvelope.getMessage());
        messageCollector.send(new OutgoingMessageEnvelope(OUTPUT_STREAM, outgoingMap));
    }
}
