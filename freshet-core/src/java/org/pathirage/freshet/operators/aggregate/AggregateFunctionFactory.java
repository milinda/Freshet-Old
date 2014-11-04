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

package org.pathirage.freshet.operators.aggregate;

import org.pathirage.freshet.Constants;
import org.pathirage.freshet.FreshetException;
import org.pathirage.freshet.data.StreamDefinition;
import org.pathirage.freshet.utils.Utilities;

import java.util.Map;

public class AggregateFunctionFactory {
    public static AggregateFunction buildAggregateFunction(String config, Map<String, StreamDefinition> inputStreamDefs){
        Map<String, String> aggregateConfig = Utilities.parseMap(config);

        AggregateType type = AggregateType.valueOf(aggregateConfig.get(Constants.CONF_AGGREGATE_TYPE));
        String field = aggregateConfig.get(Constants.CONF_AGGREGATE_FIELD);
        String alias = aggregateConfig.get(Constants.CONF_AGGREGATE_ALIAS);

        switch (type) {
            case AVG:
                return new Average(field, alias, inputStreamDefs);
            case SUM:
                return new Sum(field, alias, inputStreamDefs);
            case MAX:
                return new Max(field, alias, inputStreamDefs);
            case MIN:
                return new Min(field, alias, inputStreamDefs);
            case COUNT:
                return new Count(field, alias, inputStreamDefs);
            default:
                throw new FreshetException("Unsupported aggregate type.");
        }
    }
}