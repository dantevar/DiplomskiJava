package heuristika;

import fer.*;
import utils.*;
public class Main {

    static double round(double x) {
        return Math.round(x * 1e12) / 1e12;
    }

    public static void main(String[] args) {
        int n = 10; 
        int iter = 10000;
        
        System.out.println("Starting comparison: Greedy vs Parallel Solvers (N=" + n + ", Iterations=" + iter + ")...");

        long totalGreedyTime = 0;
        long totalTspParTime = 0;
        long totalHkParTime = 0;

        double totalGreedyCost = 0;
        double totalTspParCost = 0;
        double totalHkParCost = 0;

        int k= 0;
        for (int i = 0; i < iter; i++) {
            if (i % 100 == 0) System.out.println("Iteration: " + i);

            double[][] w = GraphGenerator.generateRandomGraph(n);
            Graph g = new Graph(w);

            // --- Greedy Heuristic ---
            long greedyStart = System.nanoTime();
            Result greedyResult = Greedy.solve(g);
            long greedyEnd = System.nanoTime();
            totalGreedyTime += (greedyEnd - greedyStart);
            totalGreedyCost += round(greedyResult.cost);

            // --- TSP Solver Parallel ---
            long tspParStart = System.nanoTime();
            TSPSolverParallel.Result tspResult = TSPSolverParallel.solve(w);
            long tspParEnd = System.nanoTime();
            totalTspParTime += (tspParEnd - tspParStart);
            totalTspParCost += round(tspResult.cost);

            // --- Closed Walk Solver Parallel ---
            long hkParStart = System.nanoTime();
            Result hkResult = ClosedWalkSolverParallel.solve(g);
            long hkParEnd = System.nanoTime();
            totalHkParTime += (hkParEnd - hkParStart);
            totalHkParCost += round(hkResult.cost);

            if(greedyResult.cost < tspResult.cost) {
                k++;
            }
        }

        System.out.println("\nNumber of times Greedy was better than TSP Parallel: " + k + " out of " + iter);

        double avgGreedyMs = (totalGreedyTime / 1e6) / iter;
        double avgTspParMs = (totalTspParTime / 1e6) / iter;
        double avgHkParMs = (totalHkParTime / 1e6) / iter;

        System.out.println("\n--- Timing (ms) ---");
        System.out.println("Greedy:                " + avgGreedyMs);
        System.out.println("TSP Parallel:          " + avgTspParMs);
        System.out.println("Closed Walk Parallel:  " + avgHkParMs);

        System.out.println("\n--- Average Costs ---");
        System.out.println("Greedy:                " + (totalGreedyCost / iter));
        System.out.println("TSP Parallel:          " + (totalTspParCost / iter));
        System.out.println("Closed Walk Parallel:  " + (totalHkParCost / iter));
        
        double greedyGap = ((totalGreedyCost - totalHkParCost) / totalHkParCost) * 100;
        System.out.println("\nGreedy is on average " + String.format("%.2f", greedyGap) + "% worse than optimal Closed Walk.");
    }
}
