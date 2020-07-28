package cgs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import soot.SootMethod;
import soot.toolkits.graph.ExceptionalUnitGraph;

public class CFGSerializer{

	public static void serialize(SootMethod method, String outputPath) {
		if (method.hasActiveBody()) {
			ExceptionalUnitGraph cfg = new ExceptionalUnitGraph(method.getActiveBody());
			Gson gson = new GsonBuilder().registerTypeAdapter(ExceptionalUnitGraph.class, new UnitEdgeSerializer())
					.disableHtmlEscaping().setPrettyPrinting().create();
			PrintWriter pw = null;
			try {
				pw = new PrintWriter(new File(outputPath));
				pw.println(gson.toJson(cfg));
				pw.close();
			} catch (Exception e) {
				if (pw != null)
					pw.close();
				e.printStackTrace();
			}
		}
	}
	
	
	public static void serializeWithJimple(SootMethod method, String outputPath) {
		CFGSerializer.serialize(method, outputPath+File.separator+method.getName() + ".json");
		File file = new File(outputPath+File.separator+method.getName() + ".jimple");
		PrintWriter writer;
		try {
			writer = new PrintWriter(file);
			soot.Printer.v().printTo(method.getActiveBody(), writer);

			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
