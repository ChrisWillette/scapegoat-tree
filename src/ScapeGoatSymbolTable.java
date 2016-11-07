

import java.io.*;
import java.util.*;
import java.lang.Math;

public class ScapeGoatSymbolTable<K extends Comparable<K>> implements SymbolTable<K> {

  public class Node implements Comparable<Object>{
    K key;
    Node left, right, parent;

    Node(K k) {
      key = k;
    }

    public K getKey(){
      return key;
    }

    public int compareTo(Object obj){
      Node node = (Node) obj;
      return this.key.compareTo(node.getKey());
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

  public void setAlpha(double a) {
    alpha = a;
  }

  @Override
  public void insert(K key) {
    if(root == null){
      root = new Node(key);
      root.parent = null;
      root.left = null;
      root.right = null;
      return;
    }
    Node x = root;
    int depth = 1;
    while(true){
      int cmp = key.compareTo(x.key);
      if(cmp < 0){
        if(x.left == null){
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
        if(x.right == null){
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
    if(NodeCount > 2 && (depth > (Math.log(NodeCount) / Math.log(1 / alpha) + 1))){
      findScapeGoat(x);
    }
  }


  @Override
  public Node search(K key) {
    Node x = root;
    while(true){
      int cmp = key.compareTo(x.key);
      if(cmp == 0){
        System.out.println("found key " + key);
        break;
      }else if(cmp < 0){
        if(x.left == null){
          System.out.println("key " + key + " not found");
          break;
        }
        x = x.left;
      }else {
        if(x.right == null){
          System.out.println("key " + key + " not found");
          break;
        }
        x = x.right;
      }
    }
    return x;
  }

  @Override
  public void delete(K key) {
    Node x = search(key);
    //replace x key with key of right-most child in left branch
    Node temp = findMaxChild(x);
    x.key = temp.key;
    if(temp.right == null && temp.left == null){//temp is a leaf
      if(temp == temp.parent.left){
        temp.parent.left = null;
      }else {
        temp.parent.right = null;
      }
      temp.parent = null;
      temp.left = null;
      temp.right = null;
      NodeCount--;
      if(NodeCount < alpha * MaxNodeCount){
        //root = rebuild(NodeCount, root);
        root = rebuild(root);
        MaxNodeCount = NodeCount;
      }
      return;
    }else{
      delete(temp.key);  //this shouldn't actually delete a node directly
    }                   //instead replaces it with successor
  }                     //then deletes the successor if it's a leaf,
                        // otherwise repeats on successor



  private Node findMaxChild(Node n) { //finds right-most child in left branch
    if(n.left == null && n.right == null) return n;
    if(n.left == null){
      return n.right;
    }else if(n.left.right == null){
      return n.left;
    }else {
      Node temp = n.left.right;
      while(temp.right != null){
        temp = temp.right;
      }
      return temp;
    }
  }

  private int size(Node p) {
    if(p == null){
      return 0;
    }else {
      return size(p.left) + size(p.right) + 1;
    }
  }

  private void findScapeGoat(Node x) {
    int siblingSize, selfSize, sgSize;
    selfSize = 0;
    sgSize = 0; //not needed
    double alphaSize;
    Node sibling;
    Node scapeGoat = null;
    Node temp = x.parent;
    while(temp != null){ //find scapegoat candidate closest to root
      if(temp == temp.left){
        sibling = temp.right;
      }else {
        sibling = temp.left;
      }
      selfSize = selfSize + 1;
      siblingSize = size(sibling);
      alphaSize = alpha * (siblingSize + selfSize + 1);
      if((size(sibling) > alphaSize) || (selfSize > alphaSize)){
        scapeGoat = temp;
        sgSize = siblingSize + selfSize + 1; //only for broken rebuild code
      }
      temp = temp.parent;
    }
    if(scapeGoat.parent == null){
      //root = rebuild(sgSize, scapeGoat);
      root = rebuild(root);
    }else if(scapeGoat == scapeGoat.parent.right){
      //scapeGoat.parent.right = rebuild(sgSize, scapeGoat);
      scapeGoat.parent.right = rebuild(scapeGoat);
    }else
      //scapeGoat.parent.left = rebuild(sgSize, scapeGoat);
      scapeGoat.parent.left = rebuild(scapeGoat);
  }




  private ArrayList<Node> buildList (Node scapeGoat){
    ArrayList<Node> list = new ArrayList<Node>();
    buildListAux(scapeGoat, list);
    Collections.sort(list);
    return list;
  }

  private void buildListAux(Node tree, ArrayList<Node> list) {
    if(tree == null){
      list.add(null);
    }else {
      list.add(tree);
      buildListAux(tree.left, list);
      buildListAux(tree.right, list);
    }
  }

  private Node rebuild(Node scapeGoat){
    ArrayList<Node> NodeList = buildList(scapeGoat);
    return buildTree(NodeList, 0, NodeList.size() -1);
  }

  private Node buildTree(ArrayList<Node> list, int first, int last){
    if(first > last){
      return null;
    }
    int mid = first + (last - first)/2;
    Node head = list.get(mid);
    head.left = buildTree(list, first, mid -1);
    head.right = buildTree(list, mid+1, last);
    return head;
  }


  //that published algorithm for in-place rebuild is broken
  /*

  private Node flatten(Node x, Node y){
    if(x == null){
      return y;
    }else{
      x.right = flatten(x.right, y);
      return flatten(x.left, x);
    }
  }

  private Node rebuild(int n, Node scapeGoat){
    Node w = null;
    Node z = flatten(scapeGoat, w);
    w = buildTree(n, z);
    return w.left;
  }

  private Node buildTree(int n, Node x){
    if(n == 0){
      x.left = null;
      return x;
    }
    Node r = buildTree((int) Math.ceil((n-1)/2), x);
    Node s = buildTree((int) Math.floor((n-1)/2), r.right);
    r.right = s.left;
    s.left = r;
    return s;
  }
*/

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
    Vector<String> sgt = serialize();
    TreePrinter treePrinter = new TreePrinter(sgt);
    treePrinter.fontSize = 14;
    treePrinter.nodeRadius = 14;
    try {
      FileOutputStream out = new FileOutputStream(fname);
      PrintStream ps = new PrintStream(out);
      treePrinter.printSVG(ps);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    ScapeGoatSymbolTable<Integer> table = new ScapeGoatSymbolTable<>();

    try{

      File f = new File("/home/chris/cs450/sg/src/tree.txt");
      Scanner reader = new Scanner(new FileInputStream(f));
      while(reader.hasNext()){
        String command = reader.next();
        if(command.equals("BuildTree")){
          table.setAlpha(reader.nextDouble());
          table.insert(reader.nextInt());
        }else if(command.equals("Insert")){
          table.insert(reader.nextInt());
        }else if(command.equals("Delete")){
          table.delete(reader.nextInt());
        }else if(command.equals("Search")){
          table.search(reader.nextInt());
        }else if(command.equals("Print")){
          table.printTree("tree.svg");
        }else if(command.equals("Done")){
          return;
        }else{
          System.out.println("invalid command " + command);
          return;
        }
      }
    }catch(FileNotFoundException e){
      e.printStackTrace();
    }


  }
}
