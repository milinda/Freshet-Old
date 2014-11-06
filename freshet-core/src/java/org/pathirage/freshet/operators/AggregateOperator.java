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
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.task.*;
import org.pathirage.freshet.Constants;
import org.pathirage.freshet.operators.aggregate.AggregateFunction;
import org.pathirage.freshet.operators.aggregate.AggregateFunctionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AggregateOperator extends FreshetOperator implements StreamTask, InitableTask {
    private static final Logger log = LoggerFactory.getLogger(AggregateOperator.class);

    /* Aggregates map. Single query can have multiple aggregates. */
    private List<AggregateFunction> aggregates;

    @Override
    public void init(Config config, TaskContext taskContext) throws Exception {
        /* To specify the aggregates, let assume we use prefixed property with 1, 2, 3, .. to specify the order. */
        Config aggregatesConfig = config.subset(Constants.CONF_AGGREGATE_AGGREGATES);

        for(int i = 0; i < aggregatesConfig.size(); i++){
            aggregates.add(i, AggregateFunctionFactory.buildAggregateFunction(
                    aggregatesConfig.get(Integer.toString(i), Constants.CONST_STR_UNDEFINED),
                    this.inputStreams));
        }
    }

    @Override
    public void process(IncomingMessageEnvelope incomingMessageEnvelope,
                        MessageCollector messageCollector,
                        TaskCoordinator taskCoordinator) throws Exception {
        String incoming = incomingMessageEnvelope.getSystemStreamPartition().getStream();
    }

}
