package cgs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.PrintWriter;
import soot.jimple.toolkits.callgraph.CallGraph;

public class CGSerializer {

  public static void serialize(CallGraph cg, String outputPath) {
    Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(CallGraph.class, new EdgeSerializer())
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();
    PrintWriter pw = null;
    try {
      pw = new PrintWriter(new File(outputPath));
      pw.println(gson.toJson(cg));
      pw.close();
    } catch (Exception e) {
      if (pw != null) pw.close();
      e.printStackTrace();
    }
  }
}
