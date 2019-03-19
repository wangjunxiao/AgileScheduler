package cn.dlut.elements.port;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class PhysicalPortSerializer implements JsonSerializer<PhysicalPort> {

    @Override
    public JsonElement serialize(final PhysicalPort port, final Type portType,
            final JsonSerializationContext context) {
        final JsonObject result = new JsonObject();
        result.addProperty("dpid", port.getParentSwitch().getSwitchName());
        result.addProperty("port", String.valueOf(port.getPortNumber()));
        return result;
    }

}
