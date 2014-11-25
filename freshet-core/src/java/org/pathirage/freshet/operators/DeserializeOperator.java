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

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.samza.config.Config;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.*;
import org.pathirage.freshet.Constants;
import org.pathirage.freshet.FreshetException;
import org.pathirage.freshet.data.StreamDefinition;
import org.pathirage.freshet.data.StreamElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeserializeOperator extends FreshetOperator implements StreamTask, InitableTask{
    private static final Logger log = LoggerFactory.getLogger(DeserializeOperator.class);

    private Schema inputStreamAvroSchema;

    private String inputStream;

    @Override
    public void init(Config config, TaskContext taskContext) throws Exception {
        this.config = config;

        initOperator(FreshetOperatorType.DESERIALIZE);

        String schemaStr = config.get(Constants.CONF_STREAM_AVRO_SCHEMA, Constants.CONST_STR_UNDEFINED);

        if(!schemaStr.equals(Constants.CONST_STR_UNDEFINED)){
            this.inputStreamAvroSchema = new Schema.Parser().parse(schemaStr);
        } else {
            String errMsg = buildLogMessage("Cannot find Avro schema for stream elements.");
            log.error(errMsg);
            throw new FreshetException(errMsg);
        }

        String inputStream = config.get(Constants.CONF_INPUT_STREAM, Constants.CONST_STR_UNDEFINED);

        if(!inputStream.equals(Constants.CONST_STR_UNDEFINED)){
            this.inputStream = inputStream;
        } else {
            String errMsg = buildLogMessage("Cannot find input stream in configuration.");
            log.error(errMsg);
            throw new FreshetException(errMsg);
        }
    }

    @Override
    public void process(IncomingMessageEnvelope incomingMessageEnvelope, MessageCollector messageCollector, TaskCoordinator taskCoordinator) throws Exception {
        String incomingStream = incomingMessageEnvelope.getSystemStreamPartition().getStream();

        if(incomingStream.equals(inputStream)){
            GenericRecord message = (GenericRecord)incomingMessageEnvelope.getMessage();

            StreamDefinition streamDefinition = inputStreams.get(incomingStream);
        }
    }
}
