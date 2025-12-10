package fer;

import fer.TSPSolver.*;
public class Main {
	
	static double round(double x) {
	    return Math.round(x * 1e12) / 1e12;
	}

	public static void main(String[] args) {

        int n = 5;
	    int iter = 1000;
	    int k = 0;

	    long totalTspTime = 0;
	    long totalWalkTime = 0;
	    long totalBruteForceTime = 0;
	    long totalHeldKarpTime = 0;
		
	    double totalPathCost = 0;
	    double totalWalkCost = 0;
		double totalBruteForceCost = 0;
		double totalHeldKarpCost = 0;

		int j = 0;
		int h = 0;
	    for (int i = 0; i < iter; i++) {
	    	
	    	if(i % 50 == 0)
	    		System.out.println(i);
	        double[][] w = GraphGenerator.generateRandomGraph(n);

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
	        
			// --- Brute force walk ---
			long bruteForceStart = System.nanoTime();
	        double bruteForceCost = BruteForce.permutations(g);
	        long bruteForceEnd = System.nanoTime();
	        long bruteForceTime = bruteForceEnd - bruteForceStart;

	        totalBruteForceTime += bruteForceTime;
	        totalBruteForceCost += round(bruteForceCost);
	        
	        // --- Held-Karp DP walk ---
	        long heldKarpStart = System.nanoTime();
	        ClosedWalkSolver.Result heldKarpResult = ClosedWalkSolver.solve(g);
	        long heldKarpEnd = System.nanoTime();
	        long heldKarpTime = heldKarpEnd - heldKarpStart;
	        
	        totalHeldKarpTime += heldKarpTime;
	        totalHeldKarpCost += round(heldKarpResult.cost);

	        totalPathCost += optimalCost;
	        totalWalkCost += finalWalkCost;

			if(round(bruteForceCost) < optimalCost)
				j++;
			if(round(heldKarpResult.cost) < optimalCost)
				h++;
	        if (finalWalkCost < optimalCost) {
	            k++;
	        }

	        if (finalWalkCost > optimalCost) {
	            System.out.println("ehhhhhhhh " + finalWalkCost + " " + optimalCost);
	        }
	    }

	    System.out.println("Bolje od tsp puta (BFS): " + (double)k / iter);
		System.out.println("Bolje od tsp puta (Branch&Bound): " + (double)j / iter);
		System.out.println("Bolje od tsp puta (Held-Karp DP): " + (double)h / iter);

	    double avgTspMs = (totalTspTime / 1e6) / iter;
	    double avgWalkMs = (totalWalkTime / 1e6) / iter;
	    double avgBruteForceMs = (totalBruteForceTime / 1e6) / iter;
	    double avgHeldKarpMs = (totalHeldKarpTime / 1e6) / iter;
	    
		System.out.println("\n--- Timing ---");
		System.out.println("Average TSP time (ms):          " + avgTspMs);
		System.out.println("Average BFS Walk time (ms):     " + avgWalkMs);
		System.out.println("Average Branch&Bound time (ms): " + avgBruteForceMs);
		System.out.println("Average Held-Karp DP time (ms): " + avgHeldKarpMs);

	    System.out.println("\n--- Costs ---");
	    System.out.println("Average TSP cost:          " + (totalPathCost) / iter);
	    System.out.println("Average BFS Walk cost:     " + (totalWalkCost) / iter);
	    System.out.println("Average Branch&Bound cost: " + (totalBruteForceCost) / iter);
	    System.out.println("Average Held-Karp DP cost: " + (totalHeldKarpCost) / iter);
	}
}
