package analiza;

import fer.ClosedWalkSolver;
import fer.ClosedWalkSolverParallel;
import utils.*;

public class ExactSolutions {

    public static void main(String[] args) {
        System.out.println("Exact Solutions Analysis\n");

        int n = 12;
        int iter = 10;
        
        long totalSeqTime = 0;
        long totalParTime = 0;
        
        double totalSeqCost = 0;
        double totalParCost = 0;

        for (int i = 0; i < iter; i++) {
            if (i % 10 == 0) {
                System.out.println("Iteration: " + i);
            }
            
            double[][] w = GraphGenerator.generateRandomGraph(n);
            Graph g = new Graph(w);
            
            // Sequential Held-Karp
            long seqStart = System.nanoTime();
            Result sol = ClosedWalkSolver.solve(g);
            long seqEnd = System.nanoTime();
            totalSeqTime += (seqEnd - seqStart);
            totalSeqCost += sol.cost;
            
            // Parallel Held-Karp
            long parStart = System.nanoTime();
            Result solParr = ClosedWalkSolverParallel.solve(g);
            long parEnd = System.nanoTime();
            totalParTime += (parEnd - parStart);
            totalParCost += solParr.cost;
        }
        
        double avgSeqMs = (totalSeqTime / 1e6) / iter;
        double avgParMs = (totalParTime / 1e6) / iter;
        
        System.out.println("\n--- Timing Results (N=" + n + ", Iterations=" + iter + ") ---");
        System.out.println("Average Sequential Held-Karp time (ms): " + avgSeqMs);
        System.out.println("Average Parallel Held-Karp time (ms):   " + avgParMs);
        System.out.println("Speedup: " + (avgSeqMs / avgParMs) + "x");
        
        System.out.println("\n--- Cost Verification ---");
        System.out.println("Average Sequential cost: " + (totalSeqCost / iter));
        System.out.println("Average Parallel cost:   " + (totalParCost / iter));
    }

}
