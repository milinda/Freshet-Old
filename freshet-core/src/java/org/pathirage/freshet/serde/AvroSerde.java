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
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.samza.serializers.Serde;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AvroSerde implements Serde<GenericRecord> {
    private static final Logger log = LoggerFactory.getLogger(AvroSerde.class);

    private Schema avroSchema;

    public AvroSerde(Schema avroSchema) {
        this.avroSchema = avroSchema;
    }

    @Override
    public GenericRecord fromBytes(byte[] bytes) {
        GenericDatumReader<GenericRecord> serveReader = new GenericDatumReader<GenericRecord>(avroSchema);
        try {
            return serveReader.read(null, DecoderFactory.get().binaryDecoder(bytes, null));
        } catch (IOException e) {
            log.error("Cannot deserialize byte array to GenericRecord.");
            return null;
        }
    }

    @Override
    public byte[] toBytes(GenericRecord genericRecord) {
        GenericDatumWriter<GenericRecord> serveWriter = new GenericDatumWriter<GenericRecord>(avroSchema);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            serveWriter.write(genericRecord, EncoderFactory.get().binaryEncoder(out, null));
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Cannot serialize GenericRecord.");
        }
        return new byte[0];
    }
}
