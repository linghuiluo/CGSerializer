package cgs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.jimple.Jimple;
import soot.toolkits.graph.ExceptionalUnitGraph;

public class UnitEdgeSerializer implements JsonSerializer<ExceptionalUnitGraph> {
  static HashMap<Unit, Integer> unitIDMap;
  static int currentID;

  public JsonElement serialize(
      ExceptionalUnitGraph graph, Type typeOfSrc, JsonSerializationContext context) {
    JsonArray result = new JsonArray();
    unitIDMap = new HashMap<>();
    currentID = 0;
    for (Iterator<Unit> it = graph.iterator(); it.hasNext(); ) {
      Unit u = it.next();
      for (Unit succ : graph.getUnexceptionalSuccsOf(u)) {
        JsonObject e = new JsonObject();
        e.addProperty("from", getString(u));
        e.addProperty("to", getString(succ));
        result.add(e);
      }
      for (Unit succ : graph.getExceptionalPredsOf(u)) {
        JsonObject e = new JsonObject();
        e.addProperty("from", getString(u));
        e.addProperty("to", getString(succ));
        result.add(e);
      }
    }
    return result;
  }

  private String getString(Unit u) {
    String str = u.toString();
    if (u.branches()) {
      if (u instanceof IfStmt) {
        IfStmt ifStmt = (IfStmt) u;
        str = Jimple.IF + " " + ifStmt.getCondition().toString();
      }
    }
    return "[" + getID(u) + "] " + str;
  }

  public static int getID(Unit unit) {
    if (unitIDMap.containsKey(unit)) return unitIDMap.get(unit);
    else {
      currentID++;
      unitIDMap.put(unit, currentID);
      return currentID;
    }
  }
}
