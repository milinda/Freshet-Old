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
import org.pathirage.freshet.data.StreamElement;
import org.pathirage.freshet.utils.system.WikipediaFeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikipediaFeedStreamTask implements StreamTask {
    private static Logger log = LoggerFactory.getLogger(WikipediaFeedStreamTask.class);

    private static final SystemStream OUTPUT_STREAM = new SystemStream("kafka", "wikipedia-raw");

    @Override
    public void process(IncomingMessageEnvelope incomingMessageEnvelope, MessageCollector messageCollector, TaskCoordinator taskCoordinator) throws Exception {
        StreamElement wikipediaFeedEvent = (StreamElement) incomingMessageEnvelope.getMessage();

        try {
            Map<String, Object> parsedEvent = parse(wikipediaFeedEvent.getStringField("rawEvent"));

            parsedEvent.put("channel", wikipediaFeedEvent.getStringField("channel"));
            parsedEvent.put("source", wikipediaFeedEvent.getStringField("source"));

            StreamElement se = new StreamElement(parsedEvent, wikipediaFeedEvent.getLongField("time"), wikipediaFeedEvent.getLongField("time"), (String) parsedEvent.get("diff-url"));

            messageCollector.send(new OutgoingMessageEnvelope(OUTPUT_STREAM, se));
        }catch (Exception e){
            log.error("Unable to parse the wikipedia event.", e);
        }
    }

    public static Map<String, Object> parse(String line) {
        Pattern p = Pattern.compile("\\[\\[(.*)\\]\\]\\s(.*)\\s(.*)\\s\\*\\s(.*)\\s\\*\\s\\(\\+?(.\\d*)\\)\\s(.*)");
        Matcher m = p.matcher(line);

        if (m.find() && m.groupCount() == 6) {
            String title = m.group(1);
            String flags = m.group(2);
            String diffUrl = m.group(3);
            String user = m.group(4);
            int byteDiff = Integer.parseInt(m.group(5));
            String summary = m.group(6);
            
            Map<String, Object> root = new HashMap<String, Object>();

            root.put("title", title);
            root.put("user", user);
            root.put("unparsed-flags", flags);
            root.put("diff-bytes", byteDiff);
            root.put("diff-url", diffUrl);
            root.put("summary", summary);

            root.put("is-minor", flags.contains("M"));
            root.put("is-new", flags.contains("N"));
            root.put("is-unpatrolled", flags.contains("!"));
            root.put("is-bot-edit", flags.contains("B"));
            root.put("is-special", title.startsWith("Special:"));
            root.put("is-talk", title.startsWith("Talk:"));


            return root;
        } else {
            throw new IllegalArgumentException("Illegal event " + line);
        }
    }
}
