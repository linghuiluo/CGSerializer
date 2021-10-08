package cgs;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

public class UnitEdgeDeserializer implements JsonDeserializer<MyCFG> {

  @Override
  public MyCFG deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    JsonArray array = json.getAsJsonArray();
    MyCFG cfg = new MyCFG();
    for (JsonElement e : array) {
      JsonObject edge = e.getAsJsonObject();
      String from = edge.get("from").getAsString();
      String to = edge.get("to").getAsString();
      cfg.addEdge(new UnitEdge(from, to));
    }
    return cfg;
  }
}
