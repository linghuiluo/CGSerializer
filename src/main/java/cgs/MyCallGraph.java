package cgs;

import java.util.HashSet;
import java.util.Set;

import cgs.stats.Callee;

public class MyCallGraph {

  public Set<MyEdge> edges;
  public Set<String> nodes;

  public MyCallGraph() {
    this.edges = new HashSet<>();
    this.nodes = new HashSet<>();
  }

  public void addEdge(MyEdge edge) {
    this.edges.add(edge);
    this.nodes.add(edge.caller);
    this.nodes.add(edge.callee);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("[\n");
    for (MyEdge edge : edges) builder.append(edge.toString());
    builder.append("]\n");
    return builder.toString();
  }

  public boolean called(String javaMethod) {
    for (MyEdge e : edges) {
      String jimpleMethod = e.callee;
      if (Callee.compareMethod(javaMethod, jimpleMethod)) {
        return true;
      }
    }
    return false;
  }
}
