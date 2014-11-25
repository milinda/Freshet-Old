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
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.samza.serializers.Serde;
import org.pathirage.freshet.utils.QueueNode;

import java.io.ByteArrayOutputStream;

public class QueueNodeSerde implements Serde<QueueNode> {
    private Kryo kryo;

    public QueueNodeSerde(Kryo kryo){
        this.kryo = kryo;
    }

    @Override
    public QueueNode fromBytes(byte[] bytes) {
        Input input = new Input(bytes);
        return kryo.readObject(input, QueueNode.class);
    }

    @Override
    public byte[] toBytes(QueueNode queueNode) {
        Output output = new Output(new ByteArrayOutputStream(2048));
        kryo.writeObject(output, queueNode);

        return output.toBytes();
    }
}
