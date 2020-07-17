package cgs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class EdgeSerializer implements JsonSerializer<CallGraph> {

  @Override
  public JsonElement serialize(CallGraph cg, Type typeOfSrc, JsonSerializationContext context) {
    JsonArray result = new JsonArray();

    for (Edge edge : cg) {
      JsonObject e = new JsonObject();
      Stmt srcStmt = edge.srcStmt();
      String src = edge.getSrc().toString();
      String tgt = edge.getTgt().toString();
      if (srcStmt != null) {
        e.addProperty("callStmt", srcStmt.toString());
        e.addProperty("lineNo", srcStmt.getJavaSourceStartLineNumber());
      } else {
        e.addProperty("callStmt", "null");
        e.addProperty("lineNo", "-1");
      }
      e.addProperty("caller", src);
      e.addProperty("callee", tgt);
      result.add(e);
    }
    return result;
  }
}
