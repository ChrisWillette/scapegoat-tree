
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Vector;
import java.lang.Math;

public class ScapeGoatSymbolTable<K extends Comparable<K>> implements SymbolTable<K> {

  public class Node {
    K key;
    Node left, right, parent;

    Node(K k) {
      key = k;
    }
  }

  private Node root;
  private double alpha;
  private int MaxNodeCount, NodeCount;

  private ScapeGoatSymbolTable() {
    root = null;
    MaxNodeCount = 0;
    NodeCount = 0;
  }

  public void setAlpha(double a){
    alpha = a;
  }

  @Override
  public void insert(K key) {
    if (root == null) {
      root = new Node(key);
      root.parent = null;
      root.left = null;
      root.right = null;
      return;
    }
    Node x = root;
    int depth = 1;
    while (true) {
      int cmp = key.compareTo(x.key);
      if (cmp < 0) {
        if (x.left == null) {
          x.left = new Node(key);
          x.left.parent = x;
          x.left.left = null;
          x.left.right = null;
          x = x.left; //to check balance
          break;
        }
        x = x.left;
        depth = depth + 1;
      }else {
        if (x.right == null) {
          x.right = new Node(key);
          x.right.parent = x;
          x.right.left = null;
          x.right.right = null;
          x = x.right; //to check balance
          break;
        }
        x = x.right;
        depth = depth + 1;
      }
    }
    NodeCount = NodeCount + 1;
    MaxNodeCount = Math.max(NodeCount, MaxNodeCount);
    if(NodeCount > 2 && (depth > (Math.log(NodeCount) / Math.log(1/alpha) + 1)) ){
      rebuild(findScapeGoat(x));
    }
  }


  @Override
  public Node search(K key) {
    Node x = root;
    while (true) {
      int cmp = key.compareTo(x.key);
      if (cmp == 0) {
        System.out.println("found key " + key);
        break;
      } else if (cmp < 0) {
        if (x.left == null) {
          System.out.println("key " + key + " not found");
          break;
        }
        x = x.left;
      } else {
        if (x.right == null) {
          System.out.println("key " + key + " not found");
          break;
        }
        x = x.right;
      }
    }
    return x;
  }

  @Override
  public void delete(K key){
    Node x = search(key);
    if(x.left == null && x.right == null){ //x is a leaf
      if(x == x.parent.left){
        x.parent.left = null;
        x = null;
      }else{
        x.parent.right = null;
        x = null;
      }
      NodeCount--;
      if(NodeCount < alpha * MaxNodeCount){
        rebuild(root);
        MaxNodeCount = NodeCount;
      }
    }else{
      //replace x key with key of right-most child in left branch
      K tempKey = findMaxChild(x).key;
      delete(tempKey);
      x.key = tempKey;
    }
  }

  private Node findMaxChild(Node n){
    if(n.left == null){
      return n.right;
    }else if(n.left.right == null){
      return n.left;
    }else{
      Node temp = n.left.right;
      while(temp.right != null){
        temp = temp.right;
      }
      return temp;
    }
  }

  private int size(Node p){
    if(p == null){
      return 0;
    }else {
      return size(p.left) + size(p.right) + 1;
    }
  }

  private Node findScapeGoat(Node x){
    double alphaSize, siblingSize, selfSize = 0;
    Node sibling;
    Node scapeGoat = null;
    Node temp = x.parent;
    while(temp != null){ //find scapegoat candidate closest to root
      if(temp == temp.left){
        sibling = temp.right;
      }else{
        sibling = temp.left;
      }
      selfSize = selfSize + 1;
      siblingSize = size(sibling);
      alphaSize = alpha * (siblingSize + selfSize + 1);
      if( (size(sibling) > alphaSize ) || (selfSize > alphaSize) ){
        scapeGoat = temp;
      }
      temp = temp.parent;
    }
    return scapeGoat;
  }

  private void flatten(){

  }

  private void rebuild(Node x){
    //todo
  }

  public Vector<String> serialize() {
    Vector<String> vec = new Vector<String>();
    serializeAux(root, vec);
    return vec;
  }

  private void serializeAux(Node tree, Vector<String> vec) {
    if (tree == null) {
      vec.addElement(null);
    } else {
      vec.addElement(tree.key.toString() + ":black");
      serializeAux(tree.left, vec);
      serializeAux(tree.right, vec);
    }
  }



  void printTree(String fname) {
    Vector<String> st = serialize();
    TreePrinter treePrinter = new TreePrinter(st);
    treePrinter.fontSize = 14;
    treePrinter.nodeRadius = 14;
    try {
      FileOutputStream out = new FileOutputStream(fname);
      PrintStream ps = new PrintStream(out);
      treePrinter.printSVG(ps);
    } catch (FileNotFoundException e) {
    }
  }

  public static void main(String[] args) {
    ScapeGoatSymbolTable<Integer> table = new ScapeGoatSymbolTable<>();

		/*
		handle input file

		 */


    Vector<String> serializedTable = table.serialize();
    TreePrinter treePrinter = new TreePrinter(serializedTable);
    FileOutputStream out = null;
    try {
      out = new FileOutputStream("tree.svg");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    PrintStream ps = new PrintStream(out);
    treePrinter.printSVG(ps);
  }

}
