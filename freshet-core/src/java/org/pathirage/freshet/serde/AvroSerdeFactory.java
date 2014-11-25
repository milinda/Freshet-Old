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

package org.pathirage.freshet.serde;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.samza.config.Config;
import org.apache.samza.serializers.Serde;
import org.apache.samza.serializers.SerdeFactory;
import org.pathirage.freshet.Constants;
import org.pathirage.freshet.FreshetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AvroSerdeFactory implements SerdeFactory<GenericRecord> {
    private static final Logger log = LoggerFactory.getLogger(AvroSerdeFactory.class);

    private Schema inputStreamAvroSchema;

    @Override
    public Serde<GenericRecord> getSerde(String s, Config config) {
        String schemaStr = config.get(Constants.CONF_STREAM_AVRO_SCHEMA, Constants.CONST_STR_UNDEFINED);

        if(!schemaStr.equals(Constants.CONST_STR_UNDEFINED)){
            this.inputStreamAvroSchema = new Schema.Parser().parse(schemaStr);
        } else {
            String errMsg = "Cannot find Avro schema for stream elements.";
            log.error(errMsg);
            throw new FreshetException(errMsg);
        }

        return new AvroSerde(inputStreamAvroSchema);
    }
}
