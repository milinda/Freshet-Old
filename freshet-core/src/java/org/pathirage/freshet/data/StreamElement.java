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

package org.pathirage.freshet.data;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/* Represent a element of streams in KappaQL. This is a immutable data structure. */
public class StreamElement {

    /* System wide notion of clock will be required in future. */
    private long globalClock;

    /* Time this element got introduced to the world. */
    private long timestamp;

    /* Unique ID may use in future to identify the element. */
    private String id;

    /* Delete and insert elements are used to simulate relations. delete true means removing
     * element from relation, otherwise its a insert. */
    private boolean delete;

    /* Actual contents of stream element. */
    private final ImmutableMap<String, Object> fields;

    public StreamElement(Map<String, Object> fields, long globalClock, long timestamp, String id){
        this.globalClock = globalClock;
        this.timestamp = timestamp;
        this.id = id;
        this.fields = ImmutableMap.copyOf(fields);
    }

    public Object getField(String fieldName) {
        return fields.get(fieldName);
    }

    public String getStringField(String fieldName){
        return (String)fields.get(fieldName);
    }

    public Integer getIntegerField(String fieldName){
        return (Integer)fields.get(fieldName);
    }

    public Double getDoubleField(String fieldName){
        return (Double)fields.get(fieldName);
    }

    public Float getFloatField(String fieldName){
        return (Float)fields.get(fieldName);
    }

    public Boolean getBoolField(String fieldName){
        return (Boolean)fields.get(fieldName);
    }

    public StreamElement extend(Map<String, Object> newFields){
        for(Map.Entry<String, Object> entry: fields.entrySet()){
            if(!newFields.containsKey(entry.getKey())){
                newFields.put(entry.getKey(), entry.getValue());
            }
        }
        StreamElement newElement = new StreamElement(newFields, this.globalClock, this.timestamp, this.id);

        return newElement;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public long getGlobalClock() {
        return globalClock;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getId() {
        return id;
    }
}
