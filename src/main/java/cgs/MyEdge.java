package cgs;

public class MyEdge {
  public String callStmt;
  public int lineNo;
  public String caller;
  public String callee;

  public MyEdge(String callStmt, int lineNo, String caller, String callee) {
    this.callStmt = callStmt;
    this.lineNo = lineNo;
    this.caller = caller;
    this.callee = callee;
  }

  @Override
  public String toString() {
    return "{\n callStmt: "
        + callStmt
        + ", \n lineNo: "
        + lineNo
        + ", \n caller: "
        + caller
        + ", \n callee: "
        + callee
        + "\n}\n";
  }
}
