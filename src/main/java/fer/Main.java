package fer;

import fer.TSPSolver.*;
public class Main {
	
	static double round(double x) {
	    return Math.round(x * 1e12) / 1e12;
	}

	public static void main(String[] args) {

        int n = 15;
	    int iter = 100;
	    int k = 0;

	    long totalTspTime = 0;
	    long totalWalkTime = 0;
	    
	    double totalPathCost = 0;
	    double totalWalkCost = 0;

	    for (int i = 0; i < iter; i++) {
	    	
	    	if(i % 10 == 0)
	    		System.out.println(i);
	        double[][] w = GraphGenerator.generateRandomGraphExp(n);

	        Graph g = new Graph(w);
	        


	        // --- TSP solver timing ---
	        long tspStart = System.nanoTime();
	        Result path = TSPSolver.solve(w);
	        long tspEnd = System.nanoTime();
	        totalTspTime += (tspEnd - tspStart);
	        double optimalCost = round(path.cost);

	        // --- BFS walk timing ---
	        long walkStart = System.nanoTime();
	        Walk walk = MyAlg.bfsWalk(g);
	        long walkEnd = System.nanoTime();
	        totalWalkTime += (walkEnd - walkStart);
	        double finalWalkCost = round(walk.cost + g.min_distances[walk.getHead()][0]);
	        
	        totalPathCost += optimalCost;
	        totalWalkCost += finalWalkCost;

	        if (finalWalkCost < optimalCost) {
	            k++;
	        }

	        if (finalWalkCost > optimalCost) {
	            System.out.println("ehhhhhhhh " + finalWalkCost + " " + optimalCost);
	        }
	    }

	    System.out.println("Bolje od tsp puta: " + (double)k / iter);

	    double avgTspMs = (totalTspTime / 1e6) / iter;
	    double avgWalkMs = (totalWalkTime / 1e6) / iter;

	    System.out.println("Average TSP time (ms):   " + avgTspMs);
	    System.out.println("Average Walk time (ms):  " + avgWalkMs);
	    
	    System.out.println("Average TSP cost :   " + (totalPathCost ) / iter);
	    System.out.println("Average Walk cost :  " + (totalWalkCost ) / iter);
	}
}
