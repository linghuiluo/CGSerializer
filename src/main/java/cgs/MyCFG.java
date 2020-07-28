package cgs;

import java.util.HashSet;
import java.util.Set;

public class MyCFG {
	public Set<UnitEdge> edges;

	public MyCFG() {
		this.edges = new HashSet<>();
	}

	public void addEdge(UnitEdge edge) {
		this.edges.add(edge);
	}

}
