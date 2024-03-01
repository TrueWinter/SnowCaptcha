package dev.truewinter.snowcaptcha.pebble;

import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Function;

import java.util.HashMap;
import java.util.Map;

public class PebbleExtension extends AbstractExtension {
    @Override
    public Map<String, Function> getFunctions() {
        HashMap<String, Function> functions = new HashMap<>();
        functions.put(JsonStringifyFunction.FUNCTION_NAME, new JsonStringifyFunction());
        functions.put(JsonParseFunction.FUNCTION_NAME, new JsonParseFunction());
        return functions;
    }
}
