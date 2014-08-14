package org.pathirage.cloudbi.topk;

import backtype.storm.tuple.Values;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pathirage.cloudbi.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

import java.io.IOException;
import java.util.Map;

public class EnrichTupleWithProcessInfoFunction extends BaseFunction {
    private static final Logger logger = LoggerFactory.getLogger(EnrichTupleWithProcessInfoFunction.class);

    @Override
    public void execute(TridentTuple tuple, TridentCollector collector) {
        String eventData = tuple.getStringByField(Constants.FIELD_EVENT_DATA);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Map<String, Object> eventJson = objectMapper.readValue(eventData, Map.class);

            if(eventJson.get("event-type") != null && ((String)eventJson.get("event-type")).equals("INSTANCE_CREATED")){
                String process = (String)eventJson.get("workflow-path");
                String instance = (String)eventJson.get("instance-id");

                if(process != null && instance != null){
                    collector.emit(new Values(process, instance));
                }
            }
        } catch (IOException e) {
            logger.error("Unable to parse event data: " + eventData, e);
        }
    }
}
