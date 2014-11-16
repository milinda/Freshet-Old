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

package org.pathirage.freshet;

public class Constants {
    public static final String CONST_STR_UNDEFINED = "kappaql.undefined";
    public static final String CONST_STR_DEFAULT_SYSTEM = "kafka";

    public static final String CONF_QUERY_ID = "org.pathirage.kappaql.query.id";
    public static final String CONF_SYSTEM = "org.pathirage.kappaql.system";
    public static final String CONF_DOWN_STREAM_TOPIC = "org.pathirage.kappaql.downstream.topic";

    public static final String CONF_SAMZA_TASK_INPUTS = "task.inputs";
    public static final String CONF_SAMZA_TASK_CLASS = "task.class";
    public static final String CONF_SAMZA_TASK_CHECKPOINT_FACTORY = "task.checkpoint.factory";
    public static final String CONF_SAMZA_TASK_CHECKPOINT_SYSTEM = "task.checkpoint.system";
    public static final String CONF_SAMZA_TASK_CHECKPOINT_REPLICATION_FACTOR = "task.checkpoint.replication.factor";
    public static final String CONF_SAMZA_JOB_NAME = "job.name";

    public static final String CONF_OPERATOR_INPUT_STREAMS = "org.pathirage.kappaql.input.streams.";
    public static final String CONF_OPERATOR_OUTPUT_STREAMS = "org.pathirage.kappaql.output.streams.";

    public static final String CONF_WINDOW_RANGE = "org.pathirage.kappaql.window.range";
    public static final String CONF_WINDOW_RANGE_SLOT_SIZE = "org.pathirage.kappaql.window.range.slot.size";
    public static final String CONF_WINDOW_ROWS = "org.pathirage.kappaql.window.rows";

    public static final String CONF_GROUPBY_FIELDS = "org.pathirage.kappaql.groupby.fields";

    public static final String CONF_AGGREGATE_AGGREGATES = "org.pathirage.kappaql.aggregate.aggregrates.";
    public static final String CONF_AGGREGATE_TYPE = "type";
    public static final String CONF_AGGREGATE_FIELD = "field";
    public static final String CONF_AGGREGATE_ALIAS = "alias";

    public static final String CONF_INPUT_STREAM = "org.pathirage.freshet.input.stream";

    public static final String CONF_STREAM_AVRO_SCHEMA = "org.pathirage.freshet.stream.avro.schema";

    public static final String CONF_SELECT_WHERE_EXPRESSION = "org.pathirage.freshet.select.where.expression";

    public static final String ERROR_UNDEFINED_OUTPUT_STREAM = "Undefined output stream.";
    public static final String ERROR_UNABLE_TO_FIND_CONFIGURATION = "Unable to find the configuration.";
    public static final String ERROR_UNDEFINED_OPERATOR_TYPE = "Undefined operator type.";
    public static final String ERROR_UNDEFINED_GROUP_BY_FIELDS = "Undefined group by fields.";

    public static final String WARN_BOTH_ROWS_AND_RANGE_DEFINED = "Both time based and tuple based windows are defined. Priority goes to time based windows.";
}
