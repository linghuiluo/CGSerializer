import static org.junit.Assert.assertTrue;

import cgs.CGDeserializer;
import cgs.CGSerializer;
import cgs.MyCallGraph;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRefImpl;
import soot.VoidType;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class CGSerializerTest {

  @Test
  public void test() {
    CallGraph cg = new CallGraph();
    SootClass c1 = new SootClass("Foo");
    SootClass c2 = new SootClass("Bar");
    SootClass c3 = new SootClass("Tea");
    SootMethod m1 = new SootMethod("foo", new ArrayList<>(), VoidType.v());
    m1.setDeclaringClass(c1);
    m1.setDeclared(true);
    SootMethod m2 = new SootMethod("bar", new ArrayList<>(), VoidType.v());
    m2.setDeclaringClass(c2);
    m2.setDeclared(true);
    SootMethod m3 = new SootMethod("tea", new ArrayList<>(), VoidType.v());
    m3.setDeclaringClass(c3);
    m3.setDeclared(true);
    Stmt call1 =
        Jimple.v()
            .newInvokeStmt(
                Jimple.v()
                    .newVirtualInvokeExpr(
                        new JimpleLocal("u1", RefType.v("Bar")),
                        new SootMethodRefImpl(c2, "bar", new ArrayList<>(), VoidType.v(), false)));
    Stmt call2 =
        Jimple.v()
            .newInvokeStmt(
                Jimple.v()
                    .newVirtualInvokeExpr(
                        new JimpleLocal("u2", RefType.v("Tea")),
                        new SootMethodRefImpl(c3, "tea", new ArrayList<>(), VoidType.v(), false)));
    Edge e1 = new Edge(m1, call1, m2);
    Edge e2 = new Edge(m2, call2, m3);
    cg.addEdge(e1);
    cg.addEdge(e2);
    String path = "cgtest.json";
    CGSerializer.serialize(cg, path);
    MyCallGraph callgraph1 = CGDeserializer.deserialize(path);

    MyCallGraph callgraph2 =
        CGDeserializer.deserialize(
            Paths.get("./src/test/resources/cgtest.json").toAbsolutePath().toString());
    Set<String> edges = new HashSet<>();
    callgraph1.edges.forEach(e -> edges.add(e.toString()));
    callgraph2.edges.forEach(e -> assertTrue(edges.contains(e.toString())));
  }
}
