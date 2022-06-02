// Benjamin Xu, Karthik Chavan
// BXZ180000, KAC180002

package dsa;

import dsa.Graph.Vertex;
import dsa.Graph.Edge;
import dsa.Graph.GraphAlgorithm;
import dsa.Graph.Factory;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.Stack;

public class PERT extends GraphAlgorithm<PERT.PERTVertex> {
	//create the linked list for criticalpath,reverse,and the stack
    LinkedList<Vertex> finishList = new LinkedList<>();
    Stack<Vertex> finishStack = new Stack<>();
    LinkedList<Vertex> reverseFinishList = new LinkedList<>();
    LinkedList<Vertex> critPath = new LinkedList<>();

    public static class PERTVertex implements Factory {
        // Add fields to represent attributes of vertices here
        public int dur;
        public boolean seen;
        public boolean active;
        //public Vertex parent;
        public int ef;
        public int lf;
        public int es;
        public int ls;
        public int slack;
        //linkedlist for tracking parents
        public LinkedList<Vertex> parents;

        public PERTVertex(Vertex u) {
        	//set the variables of the PERTVertex class
            dur = 0;
            seen = false;
            parents = new LinkedList<>();
            active = false;
            ef = 0;
            lf = 0;
            es = 0;
            ls = 0;
            slack = 0;
        }

        public PERTVertex make(Vertex u) {
        	//create vertex u
            return new PERTVertex(u);
        }

        public int getDuration(Vertex u) {
        	//show durration of edges
            return dur;
        }
    }

    // Constructor for PERT is private. Create PERT instances with static method pert().
    private PERT(Graph g) {
    	//create the PERT graph
        super(g, new PERTVertex(null));
    }

    public void setDuration(Vertex u, int d) {
    	//set the duration to d
        get(u).dur = d;
    }

    // Implement the PERT algorithm. Returns false if the graph g is not a DAG.
    public boolean pert() {
    	//set array filled with just vertexes
        Vertex[] first = g.getVertexArray();
        Vertex v = first[0];
        
        //check if isDAG returns true, 
        if (isDAG(v)) {
        	//call ec.lc.slack,topOrder,and parents
            addParents();
            topologicalOrder();
            getEC();
            getLC();
            getSlack();
            int completeTime = 0;
            //run through Vertex array 
            for (Vertex x : first) {
            	//if ef from array is less than comptime, then set comp time to ef
                if (get(x).ef > completeTime) {
                    completeTime = get(x).ef;
                }
            }
            //run thorough vertex array 
            for (Vertex x : finishList) {
            	//if the slack on the edges is 0, then add that to the critial path list
                if (get(x).slack == 0) {
                    critPath.add(x);
                }
            }
            return true;
        }
        return false;
    }
    
    //fucntion for check if its DAG
    public boolean isDAG(Vertex v) {
        get(v).active = true; //set active to true
        Iterable<Edge> y = g.outEdges(v); //create iterable g and set that to the outedges from the graph
        //run through the edges in x
        for (Edge x : y) 
        {
        	//if the active vertex is set to true, then DAG is not true
            if (get(x.toVertex()).active == true) {
                return false;
            } 
            else 
            {
            	//is no vertex, then DAG is also not true
                if (!isDAG(x.toVertex())) {
                    return false;
                }
            }
        }
        //active is false
        get(v).active = false;
        return true;
    }

    // Find a topological order of g using DFS
    LinkedList<Vertex> topologicalOrder() {
    	//create a list first, and set that to the g list
        Vertex[] first = g.getVertexArray();
        Vertex sink = null;
        //run through Vertex and check if the DEgree is 0. then set sink to x
        for (Vertex x : first) {
            if (x.outDegree() == 0) {
                sink = x;

            }
        }
        //run initalize funct
        initialize();
        //run through Vertex list
        for (Vertex x : first) {
        	//check if seen in x is false, then call dfsVisit
            if (get(x).seen == false) {
                dfsVisit(x);
            }
        }

        //dfsVisit(sink);
        //while the finishstack is not empty...
        while (!finishStack.isEmpty()) {
        	//set popped to the first item pop from stack
            Vertex popped = finishStack.pop();
            finishList.add(popped); //add that to the finishlist
            reverseFinishList.addFirst(popped);//add that to the reverse finish list as well
        }
        return finishList;
    }
    //initialize fucntion
    void initialize() {
    	//create a list first, and set that to the g list
        Vertex[] first = g.getVertexArray();
        //run through Vertex list
        for (Vertex x : first) {
        	//set each one to false
            get(x).seen = false;
            get(x).active = false;
        }
    }
    //parent function called in bool pert funct
    void addParents(){
    	//run though edge in list and add x to parents if possible
        for(Edge x : g.getEdgeArray()){
            get(x.to).parents.add(x.from);
        }
    }
    //dsfVisit function 
    void dfsVisit(Vertex v) {
    	//set V seen to true
        get(v).seen = true;
        //get(v).active = true;
        //create iterable g and set that to the outedges from the graph
        Iterable<Edge> y = g.outEdges(v);
        //run through edge x
        for (Edge x : y) {
            if (get(x.toVertex()).seen == false) {
            	//call dfsVisit with toVertex passed through
                dfsVisit(x.toVertex());
            } 
        }
        //push v to finishhstack 
        finishStack.push(v);
    }

    public int ec(Vertex u) {
    	//return ealiest time
        return get(u).ef;
    }

    public void getEC() {
    	//run through vertex x list and check if the aprents are empty
        for (Vertex x : finishList) {
        	//if parents are empty the set ef to the duration of the cureent list
            if (get(x).parents.isEmpty()) {
               get(x).ef = get(x).dur;
            } else {
            	//max to 0
                int max = 0;
                //run through Vertex list 
                for (Vertex y : get(x).parents) {
                    //Vertex y = z.fromVertex();
                	//if max is less that y list's ef, set ef to max+duration of list
                    if (max <= get(y).ef) {
                        max = get(y).ef;
                        get(x).ef = max + get(x).dur;
                    }
                }
            }
        }
    }

    // Latest completion time of u
    public int lc(Vertex u) {
        return get(u).lf;
    }
    //similar to lc
    public void getLC() {
    	//set length to critical path
        int length = criticalPath();
        //srun through vertex list and  check against reverse finish list
        for (Vertex x : reverseFinishList) {
        	//if outdegree is set to 0, the lf equals length
            if (x.outDegree() == 0) {
                get(x).lf = length;
            } 
            else 
            {
            	//set min to the max vale to integer and run through list z
                int min = Integer.MAX_VALUE;//get(finishList.getFirst()).ef;
                //run through edge list z and if min is greater than lf of y set lf to min
                for (Edge z : g.outEdges(x)) {
                    Vertex y = z.toVertex();
                    if (min > (get(y).lf - get(y).dur)) {
                        min = get(y).lf - get(y).dur;
                        get(x).lf = min;
                    }
                }
            }
        }
    }

    // Slack of u
    public int slack(Vertex u) {
        return get(u).slack;
    }
    
    public void getSlack(){
        for(Vertex x: g){
            get(x).slack = get(x).lf - get(x).ef;
        }
    }

    // Length of a critical path (time taken to complete project)
    public int criticalPath() {
        int last = 0;
        for(Vertex x : g){
            if(get(x).ef > last){
                last = get(x).ef;
            }
        }
        return last;
    }

    // Is u a critical vertex?
    public boolean critical(Vertex u) {
    	//run through Vertex list and check if u = x, then return true
        for (Vertex x : critPath) {
            if (u == x) {
                return true;
            }
        }
        return false;
    }

    // Number of critical vertices of g
    public int numCritical() {
    	//return critpath size
        return critPath.size();
    }

    /* Create a PERT instance on g, runs the algorithm.
     * Returns PERT instance if successful. Returns null if G is not a DAG.
     */
    public static PERT pert(Graph g, int[] duration) {
    	//set pert p to list g
        PERT p = new PERT(g);
        //run throguh vertex list and set the uration to the index of each item in vertex
        for (Vertex u : g) {
            p.setDuration(u, duration[u.getIndex()]);
        }
        // Run PERT algorithm.  Returns false if g is not a DAG
        if (p.pert()) {
            return p;
        } else {
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        String graph = "10 13   1 2 1   2 4 1   2 5 1   3 5 1   3 6 1   4 7 1   5 7 1   5 8 1   6 8 1   6 9 1   7 10 1   8 10 1   9 10 1      0 3 2 3 2 1 3 2 4 1";
        Scanner in;
        // If there is a command line argument, use it as file from which
        // input is read, otherwise use input from string.
        in = args.length > 0 ? new Scanner(new File(args[0])) : new Scanner(graph);
        Graph g = Graph.readDirectedGraph(in);
        g.printGraph(false);

        int[] duration = new int[g.size()];
        for (int i = 0; i < g.size(); i++) {
            duration[i] = in.nextInt();
        }
        PERT p = pert(g, duration);
        if (p == null) {
            System.out.println("Invalid graph: not a DAG");
        } else {
            System.out.println("Number of critical vertices: " + p.numCritical());
            System.out.println("u\tEC\tLC\tSlack\tCritical");
            for (Vertex u : g) {
                System.out.println(u + "\t" + p.ec(u) + "\t" + p.lc(u) + "\t" + p.slack(u) + "\t" + p.critical(u));
            }
        }
    }
}
