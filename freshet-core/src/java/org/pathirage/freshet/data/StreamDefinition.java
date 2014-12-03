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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StreamDefinition {

    private Map<String, FieldType> fieldTypeMap;

    public StreamDefinition(){
        fieldTypeMap = new HashMap<String, FieldType>();
    }

    public StreamDefinition(Map<String, FieldType> fieldTypeMap){
        this.fieldTypeMap = fieldTypeMap;
    }

    public Set<String> getFields(){
        return this.fieldTypeMap.keySet();
    }

    public FieldType getType(String field){
        return fieldTypeMap.get(field);
    }

    public boolean isValidField(String field){
        return fieldTypeMap.containsKey(field);
    }

    public void setFieldTypeMap(Map<String, FieldType> fieldTypeMap) {
        this.fieldTypeMap.putAll(fieldTypeMap);
    }

    public enum FieldType {
        INTEGER,
        STRING,
        BOOLEAN,
        FLOAT,
        TIME,
        LONG
    }
}
