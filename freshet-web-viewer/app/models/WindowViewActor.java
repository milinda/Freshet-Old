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

package models;

import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;
import org.pathirage.freshet.data.StreamElement;
import play.Logger;
import play.libs.Json;
import play.mvc.WebSocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WindowViewActor extends UntypedActor implements StreamHandler {
    WebSocket.In<JsonNode> in;
    WebSocket.Out<JsonNode> out;
    private Map<String, StreamElement> windowData;
    private KafkaTopicMonitor topicMonitor;
    private String topic;
    private Integer partitions;

    public WindowViewActor(WebSocket.In<JsonNode> in,
                           WebSocket.Out<JsonNode> out,
                           KafkaTopicMonitor kafkaTopicMonitor,
                           String topic,
                           Integer partitions) {
        this.in = in;
        this.out = out;
        this.windowData = new HashMap<>();
        this.topicMonitor = kafkaTopicMonitor;
        this.topic = topic;
        this.partitions = partitions;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        topicMonitor.registerTopic(topic, partitions, this);
    }

    @Override
    public void handle(StreamElement se) {
        Logger.info("Handling element: " + se.getId() + " isDelete: " + se.isDelete());
        if (se.isDelete()) {
            windowData.remove(se.getId());
        } else {
            windowData.put(se.getId(), se);
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message.equals("Tick")) {
            List<Map<String, Object>> window = new ArrayList<>();
            for (StreamElement se : windowData.values()) {
                Map<String, Object> fields = se.getFields();
                window.add(fields);
            }
            JsonNode windowJson = Json.toJson(window);
            out.write(windowJson);
        } else {
            unhandled(message);
        }
    }
}
