package cgs;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class EdgeDeserializer implements JsonDeserializer<MyCallGraph> {

  @Override
  public MyCallGraph deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    JsonArray array = json.getAsJsonArray();
    MyCallGraph cg = new MyCallGraph();
    for (JsonElement e : array) {
      JsonObject edge = e.getAsJsonObject();
      String callStmt = edge.get("callStmt").getAsString();
      int lineNo = edge.get("lineNo").getAsInt();
      String caller = edge.get("caller").getAsString();
      String callee = edge.get("callee").getAsString();
      cg.addEdge(new MyEdge(callStmt, lineNo, caller, callee));
    }
    return cg;
  }
}
