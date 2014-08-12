package org.pathirage.cloudbi.topk;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pathirage.cloudbi.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.trident.operation.BaseFilter;
import storm.trident.tuple.TridentTuple;

import java.io.IOException;
import java.util.Map;

public class InstanceCreatedEventFilter extends BaseFilter {
    private static final Logger logger = LoggerFactory.getLogger(InstanceCreatedEventFilter.class);

    @Override
    public boolean isKeep(TridentTuple tuple) {
        String eventData = tuple.getStringByField(Constants.FIELD_EVENT_DATA);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Map<String, Object> eventJson = objectMapper.readValue(eventData, Map.class);

            if(eventJson.get("event-type") != null && ((String)eventJson.get("event-type")).equals("INSTANCE_CREATED")){
                return true;
            }
        } catch (IOException e) {
            logger.error("Unable to parse event data JSON.", e);
        }

        return false;
    }
}
