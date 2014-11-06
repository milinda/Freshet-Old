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

import com.google.common.base.Joiner;
import com.google.common.collect.Ordering;
import org.apache.samza.config.Config;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.*;
import org.pathirage.freshet.Constants;
import org.pathirage.freshet.FreshetException;
import org.pathirage.freshet.data.StreamElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Divide input stream into multiple output streams based on the group by key.
 * <p/>
 * 10/08/2014
 * ----------
 * Main issue with group-by operator is lack of support for dynamic routing. Because we don't know the cardinality
 * of the group-by attribute its hard to do static planning. Current solution is to use Kafka topic's partitioning to
 * parallelize the execution among multiple down stream aggregators.
 */
public class GroupByOperator extends FreshetOperator implements StreamTask, InitableTask {
    private static final Logger log = LoggerFactory.getLogger(GroupByOperator.class);

    /* Order is important. */
    private List<String> groupByFields;

    @Override
    public void init(Config config, TaskContext taskContext) throws Exception {
        initOperator(FreshetOperatorType.GROUP_BY);

        /* Comma separated values of group by fields */
        String groupByFields = config.get(Constants.CONF_GROUPBY_FIELDS, Constants.CONST_STR_UNDEFINED);

        if (groupByFields.equals(Constants.CONST_STR_UNDEFINED)) {
            throw new FreshetException(Constants.ERROR_UNDEFINED_GROUP_BY_FIELDS);
        }


        this.groupByFields = Arrays.asList(groupByFields.split("\\s*,\\s*"));
        Collections.sort(this.groupByFields, Ordering.usingToString());
    }

    @Override
    public void process(IncomingMessageEnvelope incomingMessageEnvelope,
                        MessageCollector messageCollector,
                        TaskCoordinator taskCoordinator) throws Exception {
        StreamElement se = (StreamElement) incomingMessageEnvelope.getMessage();

        /* Based on group by fields we create new key for the message. This key is used to partitioned messages
         * from different group to different partition.
         *
         * 10/08/2014
         * ----------
         * I assume Samza creates one partition for each group dynamically.
         * I assumes this code maintains the order of fields.
         * TODO: Test the order maintenance. */
        List<Object> values = new LinkedList<Object>();
        for (String f : groupByFields) {
            values.add(se.getField(f));
        }

        String partitionKey = Joiner.on("-").skipNulls().join(values);

        messageCollector.send(new OutgoingMessageEnvelope(new SystemStream(system, downStreamTopic),
                partitionKey,
                partitionKey,
                se));
    }
}
