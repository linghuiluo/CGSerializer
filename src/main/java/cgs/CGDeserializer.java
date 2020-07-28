package cgs;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CGDeserializer {

  public static MyCallGraph deserialize(String path) {
    try {
      String content = new String(Files.readAllBytes(Paths.get(path)));
      Gson gson =
          new GsonBuilder().registerTypeAdapter(MyCallGraph.class, new EdgeDeserializer()).create();
      MyCallGraph callgraph = gson.fromJson(content, MyCallGraph.class);
      return callgraph;
    } catch (Exception e) {
      e.printStackTrace();
    }
    throw new RuntimeException("Couldn't deserialize " + path);
  }
}
