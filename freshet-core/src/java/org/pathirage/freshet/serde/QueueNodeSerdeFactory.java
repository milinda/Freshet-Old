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

import com.esotericsoftware.kryo.Kryo;
import org.apache.samza.config.Config;
import org.apache.samza.serializers.Serde;
import org.apache.samza.serializers.SerdeFactory;
import org.pathirage.freshet.data.StreamElement;
import org.pathirage.freshet.utils.QueueNode;

public class QueueNodeSerdeFactory implements SerdeFactory<QueueNode>{
    private static Kryo kryo = new Kryo();

    static {
        kryo.register(QueueNode.class);
        kryo.register(StreamElement.class);
    }

    @Override
    public Serde<QueueNode> getSerde(String s, Config config) {
        return new QueueNodeSerde(kryo);
    }
}
