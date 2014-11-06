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
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.*;
import org.pathirage.freshet.Constants;
import org.pathirage.freshet.FreshetException;
import org.pathirage.freshet.data.StreamDefinition;
import org.pathirage.freshet.data.StreamElement;
import org.pathirage.freshet.operators.select.Expression;
import org.pathirage.freshet.operators.select.ExpressionEvaluator;
import org.pathirage.freshet.utils.ExpressionSerde;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectOperator extends FreshetOperator implements StreamTask, InitableTask{
    private static final Logger log = LoggerFactory.getLogger(SelectOperator.class);

    private Expression whereClause;

    private ExpressionEvaluator expressionEvaluator;

    @Override
    public void init(Config config, TaskContext taskContext) throws Exception {
        this.config = config;

        initOperator(FreshetOperatorType.SELECT);

        this.expressionEvaluator = new ExpressionEvaluator();

        // Read where clause from config and build the expression.
        String expression = config.get(Constants.CONF_SELECT_WHERE_EXPRESSION, Constants.CONST_STR_UNDEFINED);
        if(!expression.equals(Constants.CONST_STR_UNDEFINED)){
            Expression expr = ExpressionSerde.deserialize(expression);
            if(!expr.isPredicate()){
                String errMessage = "Unsupported expression type: " + expr.getType() + " expression: " + expression;
                log.error(errMessage);
                throw new FreshetException(errMessage);
            }

            this.whereClause = expr;
        }
    }

    @Override
    public void process(IncomingMessageEnvelope incomingMessageEnvelope, MessageCollector messageCollector, TaskCoordinator taskCoordinator) throws Exception {
        StreamElement se = (StreamElement)incomingMessageEnvelope.getMessage();
        String inputStream = incomingMessageEnvelope.getSystemStreamPartition().getStream();
        StreamDefinition sd = inputStreams.get(inputStream);

        if(sd == null){
            String errMessage = "Unknown stream " + inputStream;
            log.error(errMessage);
            throw new FreshetException(errMessage);
        }

        if(expressionEvaluator.evalPredicate(se, sd, whereClause)){
            messageCollector.send(new OutgoingMessageEnvelope(new SystemStream(system, downStreamTopic), se));
        }

        // TODO: How down stream of select is handled. If it handled as insert/delete stream we need to modify select logic.
    }
}
