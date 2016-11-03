
import java.io.*;
import java.util.Vector;

public class ScapeGoatSymbolTable<K extends Comparable<K>> implements SymbolTable<K> {

	public class Node {
		K key;
		Node left, right, parent;

		Node(K k) {
			key = k;
		}
	}

	private Node root;

	public ScapeGoatSymbolTable() {
		root = null;
	}


	@Override
	public void insert(K key) {
		if (root == null) {
			root = new Node(key);
			return;
		}
		Node x = root;
		while (true) {
			int cmp = key.compareTo(x.key);
			if (cmp < 0) {
				if (x.left == null) {
					x.left = new Node(key);
					x.left.parent = x;
					x = x.left;
					break;
				}
				x = x.left;
			}else {
				if (x.right == null) {
					x.right = new Node(key);
					x.right.parent = x;
					x = x.right;
					break;
				}
				x = x.right;
			}
		}
		balance(x);
	}


	@Override
	public V search(K key) {
		if (root == null) {
			return null;
		}
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
		return null;
	}

	@override
	public V delete(K key){
		//todo
	}

	private int size(Node p){
		if(p == null){
			return 0;
		}else {
			return size(p.left) + size(p.right) + 1;
		}
	}

	private V balance(Node p, double alpha){
		//todo
	}

	private V flatten(){
		//todo
	}

	private void rotateLeft(Node p) {
		Node x = p.right;
		if (p == root) {
			if (x.left != null) {
				p.right = x.left;
				p.right.parent = p;
			} else {
				p.right = null;
			}
			x.left = p;
			p.parent = x;
			x.parent = null;
			root = x;
		} else {
			if (x.left != null) {
				p.right = x.left;
				p.right.parent = p;
			} else {
				p.right = null;
			}
			x.parent = p.parent;
			if (x.parent.left == p) {
				x.parent.left = x;
			} else {
				x.parent.right = x;
			}
			x.left = p;
			p.parent = x;
		}
	}


	private void rotateRight(Node p) {
		Node x = p.left;
		if (p == root) {
			if (x.right != null) {
				p.left = x.right;
				p.left.parent = p;
			} else {
				p.left = null;
			}
			x.right = p;
			p.parent = x;
			x.parent = null;
			root = x;
		} else {
			if (x.right != null) {
				p.left = x.right;
				p.left.parent = p;
			} else {
				p.left = null;
			}
			x.parent = p.parent;
			if (x.parent.left == p) {
				x.parent.left = x;
			} else {
				x.parent.right = x;
			}
			x.right = p;
			p.parent = x;
		}
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
