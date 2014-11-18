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
import org.pathirage.freshet.Constants;
import org.pathirage.freshet.FreshetException;
import org.pathirage.freshet.data.StreamDefinition;
import org.pathirage.freshet.utils.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/* In KappaQL, query is transformed in to execution plan which consists of DAG of operators(Samza jobs) connected via
 * Kakfa queues. */
public abstract class FreshetOperator {
    private static final Logger log = LoggerFactory.getLogger(FreshetOperator.class);

    /* Type of the query operator */
    private FreshetOperatorType type;

    /* Identify the Samza job specific to a query */
    private String id;

    /* Query this job belongs to */
    private String queryId;

    /* Topic to push the downstream. */
    protected String downStreamTopic;

    protected Config config;

    /* Samza System */
    protected String system;

    /* Definitions of input streams for this operator */
    protected Map<String, StreamDefinition> inputStreams;

    /* Definitions of output streams of this operator */
    protected Map<String, StreamDefinition> outputStreams;

    protected void initOperator(FreshetOperatorType type){
        if(config == null){
            log.error(Constants.ERROR_UNABLE_TO_FIND_CONFIGURATION);
            throw new FreshetException(Constants.ERROR_UNABLE_TO_FIND_CONFIGURATION);
        }

        this.type = type;
        this.queryId = config.get(Constants.CONF_QUERY_ID, Constants.CONST_STR_UNDEFINED);

        if(type != null){
            this.id = type + "-" + this.queryId + "-" + UUID.randomUUID();
        } else {
            log.error(Constants.ERROR_UNDEFINED_OPERATOR_TYPE);
            throw new FreshetException(Constants.ERROR_UNDEFINED_OPERATOR_TYPE);
        }

        String downStreamTopic = config.get(Constants.CONF_DOWN_STREAM_TOPIC, Constants.CONST_STR_UNDEFINED);
        if (downStreamTopic.equals(Constants.CONST_STR_UNDEFINED)) {
            log.warn("Down stream topic undefined.");
        }

        this.downStreamTopic = downStreamTopic;

        this.system = config.get(Constants.CONF_SYSTEM, Constants.CONST_STR_DEFAULT_SYSTEM);

        Config inputStreams = config.subset(Constants.CONF_OPERATOR_INPUT_STREAMS);
        for(String inputStream : inputStreams.keySet()){
            // TODO: How to handle undefined
            Map<String, String> fields = Utilities.parseMap(inputStreams.get(inputStream));
            Map<String, StreamDefinition.FieldType> fieldTypes = new HashMap<String, StreamDefinition.FieldType>();
            for(Map.Entry<String, String> e : fields.entrySet()){
                fieldTypes.put(e.getKey(), StreamDefinition.FieldType.valueOf(e.getValue()));
            }

            this.inputStreams.put(inputStream, new StreamDefinition(fieldTypes));
        }

        Config outputStreams = config.subset(Constants.CONF_OPERATOR_OUTPUT_STREAMS);
        for(String outputStream : outputStreams.keySet()){
            Map<String, String> fields = Utilities.parseMap(inputStreams.get(outputStream));
            Map<String, StreamDefinition.FieldType> fieldTypes = new HashMap<String, StreamDefinition.FieldType>();
            for(Map.Entry<String, String> e : fields.entrySet()){
                fieldTypes.put(e.getKey(), StreamDefinition.FieldType.valueOf(e.getValue()));
            }

            this.outputStreams.put(outputStream, new StreamDefinition(fieldTypes));
        }
    }


    public FreshetOperatorType getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getQueryId() {
        return queryId;
    }

    public String buildLogMessage(String error){
        return String.format("Query: %s, Operator Type: %s, Operator ID: %s, System: %s, Error: %s", queryId, type, id, system, error);
    }
}
