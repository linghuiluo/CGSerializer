package cgs.visualization;

import cgs.CGDeserializer;
import cgs.MyCallGraph;
import cgs.MyEdge;
import cgs.stats.Callee;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.Viewer;

public class CGVisualizer {

  private static int nodeID = 0;
  private static int edgeID = 0;
  private static HashMap<String, String> nodeIDs = new HashMap<>();
  private static HashMap<MyEdge, String> edgeIds = new HashMap<>();
  private static Set<String> included = new HashSet<>();
  private static Set<String> selected = new HashSet<>();
  private static LabelMode nodeLabel = LabelMode.Full;

  private static void addIncluded() {
    included.add("com.example");
  }

  private static void addSelected() {
    selected.add("com.example");
  }

  public static void main(String... args) {
    String file = "E:\\Git\\Github\\callgraph\\CGBench\\chat_hook_case_1_cg_FD_271.json";
    addIncluded();
    addSelected();
    MyCallGraph cg = CGDeserializer.deserialize(file);
    MultiGraph graph = new MultiGraph("Call Graph Visualization");
    for (MyEdge edge : cg.edges) {
      String from = edge.caller;
      String to = edge.callee;
      if (isIncludedNode(from) || isIncludedNode(to)) {
        addNode(from, graph);
        addNode(to, graph);
        String fromID = nodeIDs.get(from);
        String toID = nodeIDs.get(to);
        String id = generateEdgeID();
        edgeIds.put(edge, id);
        graph.addEdge(id, fromID, toID, true);
      }
    }
    highLightEntryPoints(graph);
    Viewer viewer = graph.display();
    viewer.enableAutoLayout();
  }

  private static void highLightEntryPoints(MultiGraph graph) {
    HashMap<Node, Integer> incomingEdges = new HashMap<>();
    for (Edge e : graph.getEdgeSet()) {
      Node source = e.getSourceNode();
      Node target = e.getTargetNode();
      if (!incomingEdges.containsKey(source)) incomingEdges.put(source, 0);
      if (!incomingEdges.containsKey(target)) incomingEdges.put(target, 1);
      else incomingEdges.put(target, incomingEdges.get(target) + 1);
    }
    for (Node node : incomingEdges.keySet())
      if (incomingEdges.get(node) == 0) // entry point
      {
        node.setAttribute(
            "ui.style",
            "size: 25px; fill-color: red; stroke-mode: plain; stroke-color: black;text-size:12;");
      }
  }

  private static boolean isIncludedNode(String node) {
    if (included.isEmpty()) return true;
    Pattern p = Callee.createJimpleMethodPattern();
    Matcher m = p.matcher(node);
    if (m.find()) {
      String className = m.group(1);
      for (String s : included) if (className.startsWith(s)) return true;
    }
    return false;
  }

  private static boolean isSelected(String node) {
    Pattern p = Callee.createJimpleMethodPattern();
    Matcher m = p.matcher(node);
    if (m.find()) {
      String className = m.group(1);
      for (String s : selected) if (className.startsWith(s)) return true;
    }
    return false;
  }

  private static String generateNodeID() {
    nodeID++;
    return "n" + nodeID;
  }

  private static String generateEdgeID() {
    edgeID++;
    return "e" + edgeID;
  }

  private static void addNode(String node, Graph graph) {
    if (!nodeIDs.containsKey(node)) {
      String id = generateNodeID();
      nodeIDs.put(node, id);
      String label = "[" + id.substring(1) + "] " + node.substring(1, node.length() - 1);
      Pattern p = Callee.createJimpleMethodPattern();
      Matcher m = p.matcher(node);
      if (nodeLabel.equals(LabelMode.Simple) && m.find()) {
        StringBuilder b = new StringBuilder("[" + id.substring(1) + "] ");
        String classSignature = m.group(1);
        String[] splits = classSignature.split("\\.");
        if (splits.length > 0) b.append(splits[splits.length - 1]); // class name
        else b.append(classSignature);
        b.append(".");
        b.append(m.group(3)); // method name
        label = b.toString();
      }
      Node n = graph.addNode(id);
      n.setAttribute(
          "ui.style",
          "size: 15px; fill-color: lightblue; stroke-mode: plain; stroke-color: black;text-size:12;");
      if (isSelected(node))
        n.setAttribute(
            "ui.style",
            "size: 20px; fill-color: green; stroke-mode: plain; stroke-color: black;text-size:12;");
      n.setAttribute("ui.label", label);
    }
  }
}
