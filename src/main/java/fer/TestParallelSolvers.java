package fer;
import utils.*;
public class TestParallelSolvers {

    static double round(double x) {
        return Math.round(x * 1e12) / 1e12;
    }

    public static void main(String[] args) {
        int n = 23; 
        int iter = 1;
        
        System.out.println("Starting benchmark for Parallel Solvers (N=" + n + ", Iterations=" + iter + ")...");

        long totalTspParTime = 0;
        long totalHeldKarpParTime = 0;

        double totalTspParCost = 0;
        double totalHeldKarpParCost = 0;

        int k = 0;
        for (int i = 0; i < iter; i++) {
            if (i % 10 == 0) {
                System.out.println("Iteration: " + i);
            }

            double[][] w = GraphGenerator.generateRandomGraph(n);
            Graph g = new Graph(w);

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
            totalHeldKarpParTime += (hkParEnd - hkParStart);
            totalHeldKarpParCost += round(hkResult.cost);

            if(round(tspResult.cost) > round(hkResult.cost)) {
                k++;
            }
        }

        double avgTspParMs = (totalTspParTime / 1e6) / iter;
        double avgHkParMs = (totalHeldKarpParTime / 1e6) / iter;

        System.out.println("\nNumber of times Closed Walk Parallel was better than TSP Parallel: " + k + " out of " + iter);

        System.out.println("\n--- Parallel Solvers Timing (N=" + n + ") ---");
        System.out.println("Average TSP Parallel time (ms):        " + avgTspParMs);
        System.out.println("Average Closed Walk Parallel time (ms): " + avgHkParMs);

        System.out.println("\n--- Average Costs ---");
        System.out.println("Average TSP Parallel cost:        " + (totalTspParCost / iter));
        System.out.println("Average Closed Walk Parallel cost: " + (totalHeldKarpParCost / iter));
    }
}
