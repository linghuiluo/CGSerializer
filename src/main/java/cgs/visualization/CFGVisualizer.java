package cgs.visualization;

import cgs.CFGDeserializer;
import cgs.MyCFG;
import cgs.UnitEdge;
import java.util.HashMap;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.view.Viewer;

@SuppressWarnings("unused")
public class CFGVisualizer {
  private static int nodeID = 0;
  private static int edgeID = 0;
  private static HashMap<String, String> nodeIDs = new HashMap<>();
  private static HashMap<UnitEdge, String> edgeIds = new HashMap<>();
  private static boolean onlyShowVirtualMethod = false;

  public static void main(String... args) {
    String name = "ActivityLifecycle4_cfg_DummyMainMethod.json";
    String file = "E:\\Git\\Github\\callgraph\\CGBench_Test\\ActivityLifecycle4\\" + name;
    MyCFG cfg = CFGDeserializer.deserialize(file);
    MultiGraph graph = new MultiGraph("CFG Visualization");
    for (UnitEdge edge : cfg.edges) {
      String from = edge.from;
      String to = edge.to;
      addNode(from, graph);
      addNode(to, graph);
      String fromID = nodeIDs.get(from);
      String toID = nodeIDs.get(to);
      String id = generateEdgeID();
      edgeIds.put(edge, id);
      graph.addEdge(id, fromID, toID, true);
    }
    highLightEntryPoints(graph);
    Viewer viewer = graph.display();
    viewer.enableAutoLayout(new SpringBox());
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
      String label = node;
      Node n = graph.addNode(id);
      n.setAttribute(
          "ui.style",
          "size: 15px; fill-color: lightblue; stroke-mode: plain; stroke-color: black;text-size:12;");
      if (onlyShowVirtualMethod) {
        if (node.contains("virtualinvoke")) {
          n.setAttribute(
              "ui.style",
              "size: 15px; fill-color: green; stroke-mode: plain; stroke-color: black;text-size:12;");
          String[] strs;
          strs = node.split("<");
          strs = strs[1].split(":");
          String className = strs[0];
          String methodName = strs[1].split("\\s")[2].split(">")[0];
          label = node.split("]")[0] + "] " + className + "." + methodName;
        } else label = "";
      }
      n.setAttribute("ui.label", label);

      int i = Integer.parseInt(node.split("]")[0].substring(1));
      float y = (float) (10.0 * i);
      n.setAttribute("y", -y);
    }
  }
}
