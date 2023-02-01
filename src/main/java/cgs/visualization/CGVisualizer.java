package cgs.visualization;

import cgs.CGDeserializer;
import cgs.MyCallGraph;
import cgs.MyEdge;
import cgs.stats.Callee;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.Viewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CGVisualizer {
  public static Logger logger = LoggerFactory.getLogger(CGVisualizer.class);
  private static int nodeID = 0;
  private static int edgeID = 0;
  private static HashMap<String, String> nodeIDs = new HashMap<>();
  private static HashMap<MyEdge, String> edgeIds = new HashMap<>();

  private static Set<String> included = new HashSet<>();
  private static Set<String> selected = new HashSet<>();

  private static HashSet<String> sources = new HashSet<>();
  private static HashSet<String> targets = new HashSet<>();

  private static HashSet<Node> sourceNodes = new HashSet<>();
  private static HashSet<Node> targetNodes = new HashSet<>();

  private static LabelMode nodeLabel = LabelMode.Simple;

  private static void addIncluded() {
    included.add("averroes");
    included.add("springbench.multiplerequests");
  }

  private static void addSelected() {
    // selected.add("onlineshop");
    selected.add("averroes");
  }

  private static void setSourceAndSinkForShortestPaths() {
    // sources.add("averroes.DummyMainClass");
    // targets.add("onlineshop.api.ShopController");
  }

  public static void main(String... args) {
    // String file =
    // "E:\\Git\\Github\\callgraph\\CGBench_Test\\chat_hook_case_1\\chat_hook_case_1_cg_FD_271.json";
    String file =
        "/Users/llinghui/Projects/GitHub/SpringDemo/multipleRequests/gencg-output/instrumented-app_cg_FD_271.json";
    addIncluded();
    addSelected();
    setSourceAndSinkForShortestPaths();
    MyCallGraph cg = CGDeserializer.deserialize(file);
    logger.info("Call graph has " + cg.edges.size() + " edges and " + cg.nodes.size() + " nodes.");
    graphAllNodes(cg);
    // graphWithIncludedNodes(cg);
    // graphWithShortestPaths(cg);

    // highLightEntryPointsInRed(graph);
    // Viewer viewer = graph.display();
    // viewer.enableAutoLayout();
  }

  private static void cleanUp() {
    sourceNodes = new HashSet<>();
    targetNodes = new HashSet<>();
    nodeID = 0;
    edgeID = 0;
    nodeIDs = new HashMap<>();
    edgeIds = new HashMap<>();
  }

  private static void graphAllNodes(MyCallGraph cg) {
    cleanUp();
    MultiGraph graph = new MultiGraph("Call graph");
    for (MyEdge edge : cg.edges) {
      String from = edge.caller;
      String to = edge.callee;
      addNode(from, graph);
      addNode(to, graph);
      String fromID = nodeIDs.get(from);
      String toID = nodeIDs.get(to);
      String id = generateEdgeID();
      edgeIds.put(edge, id);
      graph.addEdge(id, fromID, toID, true);
    }
    highLightEntryPointsInRed(graph);
    Viewer viewer = graph.display();
    viewer.enableAutoLayout();
  }

  private static void graphWithIncludedNodes(MyCallGraph cg) {
    cleanUp();
    MultiGraph graph = new MultiGraph("Call graph");
    int outgoingEdge = 0;
    for (MyEdge edge : cg.edges) {
      String from = edge.caller;
      String to = edge.callee;
      if (from.contains("doItAll")) {
        outgoingEdge++;
      }
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
    logger.info("Library.doItAll has " + outgoingEdge + " outgoing edges.");
    highLightEntryPointsInRed(graph);
    Viewer viewer = graph.display();
    viewer.enableAutoLayout();
  }

  private static void graphWithShortestPaths(MyCallGraph cg) {
    cleanUp();
    MultiGraph graph = new MultiGraph("Call graph");
    for (MyEdge edge : cg.edges) {
      String from = edge.caller;
      String to = edge.callee;
      addNode(from, graph);
      addNode(to, graph);
      String fromID = nodeIDs.get(from);
      String toID = nodeIDs.get(to);
      String id = generateEdgeID();
      edgeIds.put(edge, id);
      graph.addEdge(id, fromID, toID, true);
    }

    MultiGraph subgraph = new MultiGraph("Shortest paths");
    edgeID = 0;
    for (Node source : sourceNodes) {
      for (Node target : targetNodes) {
        drawSubgraphWithShortestPath(graph, subgraph, source, target);
      }
    }
    for (Node source : sourceNodes) {
      setSelectedNodeInRed(subgraph.getNode(source.getId()));
    }
    for (Node target : targetNodes) {
      setSelectedNodeInGreen(subgraph.getNode(target.getId()));
    }
    Viewer viewer = subgraph.display();
    viewer.enableAutoLayout();
  }

  private static void drawSubgraphWithShortestPath(
      MultiGraph graph, MultiGraph subgraph, Node source, Node target) {
    logger.info(
        "compute shortest path from source: "
            + source.getAttribute("ui.label").toString()
            + " to "
            + target.getAttribute("ui.label").toString());
    Dijkstra dijkstra = new Dijkstra();
    dijkstra.init(graph);
    dijkstra.setSource(source);
    dijkstra.compute();
    Iterable<Path> paths = dijkstra.getAllPaths(target);
    for (Path p : paths) {
      logger.info("Found a path with " + p.getEdgeCount() + " edges.");
      List<Edge> edges = p.getEdgePath();
      for (Edge e : edges) {
        String sID = e.getSourceNode().getId();
        String tID = e.getTargetNode().getId();
        if (subgraph.getNode(sID) == null) {
          Node n = subgraph.addNode(sID);
          setNormalNodeInBlue(n);
          n.setAttribute("ui.label", e.getSourceNode().getAttribute("ui.label").toString());
        }
        if (subgraph.getNode(tID) == null) {
          Node n = subgraph.addNode(tID);
          setNormalNodeInBlue(n);
          n.setAttribute("ui.label", e.getTargetNode().getAttribute("ui.label").toString());
        }
        String eId = generateEdgeID();
        subgraph.addEdge(eId, sID, tID, true);
      }
    }
  }

  private static void highLightEntryPointsInRed(MultiGraph graph) {
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
            "size: 20px; fill-color: red; stroke-mode: plain; stroke-color: black;text-size:12;");
      }
  }

  private static boolean isIncludedNode(String node) {
    return commonPrefixInList(node, included);
  }

  private static boolean isSelected(String node) {
    return commonPrefixInList(node, selected);
  }

  private static boolean commonPrefixInList(String node, Set<String> list) {
    if (list.isEmpty()) return true;
    Pattern p = Callee.createJimpleMethodPattern();
    Matcher m = p.matcher(node);
    if (m.find()) {
      String className = m.group(1);
      for (String s : list) if (className.startsWith(s)) return true;
    }
    return false;
  }

  private static boolean commonPrefix(String node, String c) {
    Pattern p = Callee.createJimpleMethodPattern();
    Matcher m = p.matcher(node);
    if (m.find()) {
      String className = m.group(1);
      if (className.startsWith(c)) return true;
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

  private static void setSelectedNodeInGreen(Node n) {
    n.setAttribute(
        "ui.style",
        "size: 20px; fill-color: green; stroke-mode: plain; stroke-color: black;text-size:12;");
  }

  private static void setSelectedNodeInRed(Node n) {
    n.setAttribute(
        "ui.style",
        "size: 20px; fill-color: red; stroke-mode: plain; stroke-color: black;text-size:12;");
  }

  private static void setNormalNodeInBlue(Node n) {
    n.setAttribute(
        "ui.style",
        "size: 15px; fill-color: lightblue; stroke-mode: plain; stroke-color: black;text-size:12;");
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
      setNormalNodeInBlue(n);
      if (isSelected(node)) setSelectedNodeInGreen(n);
      n.setAttribute("ui.label", label);
      for (String source : sources) {
        if (commonPrefix(node, source)) {
          sourceNodes.add(n);
          logger.debug("Add source " + node);
        }
      }
      for (String target : targets) {
        if (commonPrefix(node, target)) {
          targetNodes.add(n);
          logger.debug("Add target " + node);
        }
      }
    }
  }
}
