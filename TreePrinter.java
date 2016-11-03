import java.util.Vector;
import java.io.*;

public class TreePrinter {
        
    private static class Node {
        public String str;
        public Node left, right;
        public int height;
        public float dx;
        public float x, y;
        public String color;
        public Node(String s, String c) {
            str = s;
            color = c;
            left = right = null;
            height = 0;
            dx = x = y = 0;
        }
        public Node(String s) {this(s, "purple");}
    }
        
    private Node root;
    private float[] xrange;
        
    private static class Scanner {
        private int next;
        private Vector<String> vec;
        public Scanner(Vector<String> serializedTree) {
            next = 0;
            vec = serializedTree;
        }
        public boolean hasNext() {
            return next < vec.size();
        }
        public String next() {
            return vec.elementAt(next++);
        }
    }
        
    private static int height(Node tree) {
        return (tree == null) ? -1 : tree.height;
    }
        
    private static Node buildTree(Scanner scanner) {
        if (scanner.hasNext()) {
            String str = scanner.next();
            if (str == null)
                return null;
            String[] a = str.split(":"); // a[1] is color if it exists
            Node n = (a.length < 2) ? new Node(a[0]) : new Node(a[0],a[1]);
            n.left = buildTree(scanner);
            n.right = buildTree(scanner);
            n.height = 1 + Math.max(height(n.left), height(n.right));
            return n;
        }
        return null;
    }
        
    public TreePrinter(Vector<String> serializedTree) {
        Scanner scanner = new Scanner(serializedTree);
        root = buildTree(scanner);
        childOffsets(root);
        xrange = new float[2];
        assignCoordinates(root, 0, 0, xrange);
    }
        
    private static void rightContour(Node n, int y, float x, float contour[]) {
        if (n != null) {
            x += n.dx;
            if (x > contour[y])
                contour[y] = x;
            rightContour(n.left, y+1, x, contour);
            rightContour(n.right, y+1, x, contour);
        }
    }
        
    private static void leftContour(Node n, int y, float x, float contour[]) {
        if (n != null) {
            x += n.dx;
            if (x < contour[y])
                contour[y] = x;
            leftContour(n.left, y+1, x, contour);
            leftContour(n.right, y+1, x, contour);
        }
    }
        
    private static void childOffsets(Node tree) {
        if (tree.left == null) {
            if (tree.right != null) {
                childOffsets(tree.right);
                tree.right.dx = +1;
            }
        } else if (tree.right == null) {
            childOffsets(tree.left);
            tree.left.dx = -1;
        } else {
            childOffsets(tree.left);
            childOffsets(tree.right);

            int lh = height(tree.left);
            float[] rcontour = new float[lh+1];
            for (int i = 0; i <= lh; i++)
                rcontour[i] = -10000;
            rightContour(tree.left, 0, 0, rcontour);

            int rh = height(tree.right);
            float[] lcontour = new float[rh+1];;
            for (int i = 0; i <= rh; i++)
                lcontour[i] = 10000;
            leftContour(tree.right, 0, 0, lcontour);

            int yend = (lh < rh) ? lh : rh;
            float smin = 0;
            for (int y = 1; y <= yend; y++) {
                float s = lcontour[y] - rcontour[y];
                if (s < smin)
                    smin = s;
            }
            float d = 2 - smin;
            tree.left.dx = -d/2;
            tree.right.dx = +d/2;
        }
    }

    private void assignCoordinates(Node n, float x, float y, float[] xrange) {
        if (n != null) {
            n.x = x + n.dx;
            if (n.x < xrange[0])
                xrange[0] = n.x;
            if (n.x > xrange[1])
                xrange[1] = n.x;
            n.y = y;
            assignCoordinates(n.left, n.x, y+1, xrange);
            assignCoordinates(n.right, n.x, y+1, xrange);
        }
    }

    private void circleSVG(PrintStream stream, float x, float y, float radius, String color) {
        stream.println(
                       "<circle cx=\"" + x + "\" cy=\"" + y + "\" r=\"" + radius + 
                       "\" stroke-width=\"3\" stroke=\"" + color + "\" fill=\"white\"/>");
    }

    private void lineSVG(PrintStream stream, float x1, float y1, float x2, float y2, String color) {
        stream.println(
                       "<line x1=\"" + x1 + "\" y1=\"" + y1 + "\" x2=\"" + x2 +"\" y2=\"" + y2 + 
                       "\" style=\"stroke:" + color + ";stroke-width:3\"/>");
    }

    private void textSVG(PrintStream stream, float x, float y, int fontSize, String text) {
        stream.println(
                       "<text x=\"" + x + "\" y=\"" + y + "\" font-size=\"" + fontSize +
                       "\" style=\"text-anchor: middle;dominant-baseline: central;\">" + text + "</text>");
    }
        
    void edgeSVG(PrintStream stream, float x0, float y0, float x1, float y1, 
                 float nodeRadius, String color) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float len =(float)  Math.sqrt(dx*dx + dy*dy);
        float u = dx/len;
        float v = dy/len;
        lineSVG(stream,
                x0 + nodeRadius*u, y0 + nodeRadius*v,
                x1 - nodeRadius*u, y1 - nodeRadius*v, color);
    }
        
    private void printSVG(PrintStream f, Node n, int fontSize, int nodeRadius, 
                          float scalex, float scaley, float dx, float dy) {
        if (n == null)
            return;
        float x = scalex*n.x + dx;
        float y = scaley*n.y + dy;
        // XXX String color = "black";
        circleSVG(f, x, y, nodeRadius, n.color);
        textSVG(f, x, y, fontSize, n.str);
        if (n.left != null) {
            float x1 = scalex*n.left.x + dx;
            float y1 = scaley*n.left.y + dy;
            edgeSVG(f, x, y, x1, y1, nodeRadius, n.left.color);
            printSVG(f, n.left, fontSize, nodeRadius, scalex, scaley, dx, dy);
        }
        if (n.right != null) {
            float x1 = scalex*n.right.x + dx;
            float y1 = scaley*n.right.y + dy;
            edgeSVG(f, x, y, x1, y1, nodeRadius, n.right.color);
            printSVG(f, n.right, fontSize, nodeRadius, scalex, scaley, dx, dy);
        }
    }
        
    public float border = 30;
    public float scalex = 20;
    public float scaley = 50;
    public int fontSize = 20;
    public int nodeRadius = 14;
        
    public void printSVG(PrintStream stream) {

        float yshift = border;
        float xshift = -scalex*xrange[0] + border;
        int W = (int) (scalex*xrange[1] + xshift + border);
        int H = (int) (scaley*height(root) + yshift + border);
        stream.println(
                       "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\"" +
                       " width=\"" + W + "\" height=\"" + H + "\">");
        printSVG(stream, root, fontSize, nodeRadius, scalex, scaley, xshift, yshift);
        stream.println("</svg>\n");
    }

}
