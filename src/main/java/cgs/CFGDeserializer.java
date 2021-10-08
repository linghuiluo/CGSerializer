package cgs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CFGDeserializer {
  public static MyCFG deserialize(String path) {
    try {
      String content = new String(Files.readAllBytes(Paths.get(path)));
      Gson gson =
          new GsonBuilder().registerTypeAdapter(MyCFG.class, new UnitEdgeDeserializer()).create();
      MyCFG cfg = gson.fromJson(content, MyCFG.class);
      return cfg;
    } catch (Exception e) {
      e.printStackTrace();
    }
    throw new RuntimeException("Couldn't deserialize " + path);
  }
}
