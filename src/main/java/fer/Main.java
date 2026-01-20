package fer;

import utils.*;
public class Main {
	
	static double round(double x) {
	    return Math.round(x * 1e12) / 1e12;
	}

	public static void main(String[] args) {

        int n = 20; 
	    int iter = 100;
	    
	    long totalTspTime = 0;
	    long totalTspParTime = 0; // Parallel TSP
	    long totalMyAlgTime = 0;      
	    long totalMyAlgOptTime = 0;   
	    long totalHeldKarpTime = 0;
	    long totalHeldKarpParTime = 0; 
		
	    double totalPathCost = 0;
	    double totalTspParCost = 0;
	    double totalMyAlgCost = 0;
	    double totalMyAlgOptCost = 0;
		double totalHeldKarpCost = 0;
		double totalHeldKarpParCost = 0;

		int myAlgBetter = 0;
		int myAlgOptBetter = 0;
		int heldKarpBetter = 0;
		
	    for (int i = 0; i < iter; i++) {
	    	
	    	if(i % 10 == 0)
	    		System.out.println("Iteracija: " + i);
	        
	    	double[][] w = GraphGenerator.generateMetricGraph(n);
	        Graph g = new Graph(w);
	        
	        // --- TSP solver timing ---
	        long tspStart = System.nanoTime();
	        Result path = TSPSolver.solve(w);
	        long tspEnd = System.nanoTime();
	        totalTspTime += (tspEnd - tspStart);
	        double optimalCost = round(path.cost);
	        
	        // --- TSP solver Parallel timing ---
	        long tspParStart = System.nanoTime();
	        TSPSolverParallel.Result pathPar = TSPSolverParallel.solve(w);
	        long tspParEnd = System.nanoTime();
	        totalTspParTime += (tspParEnd - tspParStart);
	        double optimalParCost = round(pathPar.cost);

	        // --- MyAlg (Original BFS) timing ---
	        long myAlgStart = System.nanoTime();
	        //Walk walk = MyAlg.bfsWalk(g);
	        long myAlgEnd = System.nanoTime();
	        totalMyAlgTime += (myAlgEnd - myAlgStart);
	        double myAlgCost = 1;//round(walk.cost + g.min_distances[walk.getHead()][0]);
	        
	        // --- MyAlgOptimized (Parallel BFS) timing ---
	        long myAlgOptStart = System.nanoTime();
	        //Walk walkOpt = MyAlgOptimized.bfsWalkParallel(g);
	        long myAlgOptEnd = System.nanoTime();
	        totalMyAlgOptTime += (myAlgOptEnd - myAlgOptStart);
	        double myAlgOptCost = 1;//round(walkOpt.cost + g.min_distances[walkOpt.getHead()][0]);
	        
	        // --- Held-Karp DP walk ---
	        long heldKarpStart = System.nanoTime();
	        ClosedWalkSolver.Result heldKarpResult = ClosedWalkSolver.solve(g);
	        long heldKarpEnd = System.nanoTime();
	        long heldKarpTime = heldKarpEnd - heldKarpStart;
	        
	        // --- Held-Karp DP Parallel walk ---
	        long heldKarpParStart = System.nanoTime();
	        Result heldKarpParResult = ClosedWalkSolverParallel.solve(g);
	        long heldKarpParEnd = System.nanoTime();
	        long heldKarpParTime = heldKarpParEnd - heldKarpParStart;
	        
	        totalHeldKarpTime += heldKarpTime;
	        totalHeldKarpCost += round(heldKarpResult.cost);
	        
	        totalHeldKarpParTime += heldKarpParTime;
	        totalHeldKarpParCost += round(heldKarpParResult.cost);

	        totalPathCost += optimalCost;
	        totalTspParCost += optimalParCost;
	        totalMyAlgCost += myAlgCost;
	        totalMyAlgOptCost += myAlgOptCost;

			if(myAlgCost < optimalCost) myAlgBetter++;
			if(myAlgOptCost < optimalCost) myAlgOptBetter++;
			if(round(heldKarpResult.cost) < optimalCost) heldKarpBetter++;
	    }

	    System.out.println("Bolje od TSP puta (MyAlg):           " + (double)myAlgBetter / iter);
	    System.out.println("Bolje od TSP puta (MyAlg Optimized): " + (double)myAlgOptBetter / iter);
		System.out.println("Bolje od TSP puta (Held-Karp DP):    " + (double)heldKarpBetter / iter);

	    double avgTspMs = (totalTspTime / 1e6) / iter;
	    double avgTspParMs = (totalTspParTime / 1e6) / iter;
	    double avgMyAlgMs = (totalMyAlgTime / 1e6) / iter;
	    double avgMyAlgOptMs = (totalMyAlgOptTime / 1e6) / iter;
	    double avgHeldKarpMs = (totalHeldKarpTime / 1e6) / iter;
	    double avgHeldKarpParMs = (totalHeldKarpParTime / 1e6) / iter;
	    
		System.out.println("\n--- Timing ---");
		System.out.println("Average TSP time (ms):            " + avgTspMs);
		System.out.println("Average TSP Parallel time (ms):   " + avgTspParMs);
		System.out.println("Average MyAlg time (ms):          " + avgMyAlgMs);
		System.out.println("Average MyAlg Optimized time (ms):" + avgMyAlgOptMs);
		System.out.println("Average Held-Karp DP time (ms):   " + avgHeldKarpMs);
		System.out.println("Average Held-Karp Parallel (ms):  " + avgHeldKarpParMs);

	    System.out.println("\n--- Costs ---");
	    System.out.println("Average TSP cost:            " + (totalPathCost) / iter);
	    System.out.println("Average TSP Parallel cost:   " + (totalTspParCost) / iter);
	    System.out.println("Average MyAlg cost:          " + (totalMyAlgCost) / iter);
	    System.out.println("Average MyAlg Optimized cost:" + (totalMyAlgOptCost) / iter);
	    System.out.println("Average Held-Karp DP cost:   " + (totalHeldKarpCost) / iter);
	    System.out.println("Average Held-Karp Parallel cost: " + (totalHeldKarpParCost) / iter);
	}
}
