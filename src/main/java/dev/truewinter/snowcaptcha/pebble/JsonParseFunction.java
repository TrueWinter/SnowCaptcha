package dev.truewinter.snowcaptcha.pebble;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mitchellbosecke.pebble.extension.Function;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonParseFunction implements Function {
    public static final String FUNCTION_NAME = "jsonp";

    @Override
    public List<String> getArgumentNames() {
        List<String> names = new ArrayList<>();
        names.add(FUNCTION_NAME);
        return names;
    }

    @Override
    public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
        try {
            String json = (String) args.get(FUNCTION_NAME);
            return new ObjectMapper().readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(String.format("Failed to process JSON at line %d", lineNumber), e);
        }
    }
}
