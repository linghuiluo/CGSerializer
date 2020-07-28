package cgs.stats;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import cgs.CGDeserializer;
import cgs.MyCallGraph;

public class IncompleteCGStats {

  private static PrintWriter writer;

  public static void main(String... args) throws FileNotFoundException {
    String allFindingsPath = "E:\\Git\\Github\\taintbench\\GitPod-GITHUB\\AllSuite\\allFindings";
    File allFindingsDir = new File(allFindingsPath);
    String outputPath = "cgIncompleteOutput.csv";
    writer = new PrintWriter(new File(outputPath));
    writer.print("apk;flowID;type;stepID;class;method\n");
    for (File tbFindingsPath : allFindingsDir.listFiles()) {
      String serializedCg =
          "cg-serialized\\"
              + tbFindingsPath.getName().split("_findings.json")[0]
              + "_cg_FD_271.json";
      File cgFile = new File(serializedCg);
      if (!cgFile.exists()) System.err.println(serializedCg + " doesn't exist");
      else {
        MyCallGraph actualCg = CGDeserializer.deserialize(cgFile.getAbsolutePath());
        searchInCallGraph(tbFindingsPath.getAbsolutePath(), actualCg);
      }
    }
    writer.flush();
    writer.close();
  }

  public static void searchInCallGraph(String tbFindingsPath, MyCallGraph actualCg) {
    JsonParser parser = new JsonParser();
    JsonObject obj;
    try {
      obj = parser.parse(new FileReader(tbFindingsPath)).getAsJsonObject();
      JsonArray findings = obj.getAsJsonArray("findings");
      String apk = obj.get("fileName").getAsString();

      for (int i = 0; i < findings.size(); i++) {
        JsonObject finding = findings.get(i).getAsJsonObject();
        int id = finding.get("ID").getAsInt();
        boolean isNegativeFlow = finding.get("isNegative").getAsBoolean();
        if (!isNegativeFlow) {
          if (finding.has("source")) {
            JsonObject source = finding.get("source").getAsJsonObject();
            String methodName = source.get("methodName").getAsString();
            String className = source.get("className").getAsString();
            Callee c = new Callee(apk, className, methodName, Type.SOURCE, id);
            boolean called = actualCg.called(c.toString());
            if (!called) {
              write(c);
            }
          }
          if (finding.has("sink")) {
            JsonObject sink = finding.get("sink").getAsJsonObject();
            String methodName = sink.get("methodName").getAsString();
            String className = sink.get("className").getAsString();
            Callee c = new Callee(apk, className, methodName, Type.SINK, id);
            boolean called = actualCg.called(c.toString());
            if (!called) {
              write(c);
            }
          }
          if (finding.has("intermediateFlows")) {
            JsonArray interSteps = finding.get("intermediateFlows").getAsJsonArray();
            for (int j = 0; j < interSteps.size(); j++) {
              JsonObject interStep = interSteps.get(j).getAsJsonObject();
              int stepID = interStep.get("ID").getAsInt();
              String methodName = interStep.get("methodName").getAsString();
              String className = interStep.get("className").getAsString();
              Callee c = new Callee(apk, className, methodName, Type.INTERMEDIATE, id, stepID);
              boolean called = actualCg.called(c.toString());
              if (!called) {
                write(c);
              }
            }
          }
        }
      }
    } catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static void write(Callee c) {
    writer.print(c.apk);
    writer.print(";");
    writer.print(c.flowID);
    writer.print(";");
    writer.print(c.type);
    writer.print(";");
    if (c.type.equals(Type.INTERMEDIATE)) writer.print(c.stepID);
    writer.print(";");
    writer.print(c.className);
    writer.print(";");
    writer.print(c.methodName);
    writer.print("\n");
  }
}
