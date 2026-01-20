package metaheuristika;

import utils.*;

/**
 * Test program za Simulated Annealing algoritme.
 * 
 * Usporedba:
 * 1. SA-Permutation: Permutacija + min_distances fitnes
 * 2. SA-Walk: Cijeli walk s ponavljanjima
 */
import fer.*;
public class SAMain {
    
    public static void main(String[] args) {
        // Generiraj test graf
        int n = 15;
        double[][] distances = GraphGenerator.generateRandomGraph(n);
        Graph graph = new Graph(distances);
        
        System.out.println("=== Simulated Annealing Test ===");
        System.out.println("Graph: n=" + n);
        System.out.println();
        
        // Parametri SA
        double T0 = 100.0;           // Početna temperatura
        double alpha = 0.95;          // Faktor hlađenja
        int iterationsPerTemp = 100;  // Iteracija po temp
        double minTemp = 0.01;        // Minimalna temp
        int printEvery = 500;         // Print freq
        
        System.out.println("SA Parameters:");
        System.out.println("  T0 = " + T0);
        System.out.println("  alpha = " + alpha);
        System.out.println("  iterationsPerTemp = " + iterationsPerTemp);
        System.out.println("  minTemp = " + minTemp);
        System.out.println();
        
        // 1. SA-Permutation
        System.out.println("--- SA-Permutation (Permutacija + min_distances) ---");
        long start1 = System.currentTimeMillis();
        Result result1 = SimulatedAnnealingPermutation.solve(
            graph, T0, alpha, iterationsPerTemp, minTemp, printEvery
        );
        long duration1 = System.currentTimeMillis() - start1;
        
        System.out.println("\nSA-Permutation Result:");
        System.out.println("  Cost: " + result1.cost);
        System.out.println("  Tour length: " + result1.tour.size());
        System.out.println("  Tour: " + result1.tour);
        System.out.println("  Time: " + duration1 + " ms");
        System.out.println();
        
        // 2. SA-Walk
        System.out.println("--- SA-Walk (Full walk representation) ---");
        long start2 = System.currentTimeMillis();
        Result result2 = SimulatedAnnealingWalk.solve(
            graph, T0, alpha, iterationsPerTemp, minTemp, printEvery
        );
        long duration2 = System.currentTimeMillis() - start2;
        
        System.out.println("\nSA-Walk Result:");
        System.out.println("  Cost: " + result2.cost);
        System.out.println("  Tour length: " + result2.tour.size());
        System.out.println("  Tour: " + result2.tour);
        System.out.println("  Time: " + duration2 + " ms");
        System.out.println();
        
        // Usporedba
        System.out.println("=== Comparison ===");
        System.out.println("SA-Permutation: " + result1.cost + " (" + duration1 + " ms)");
        System.out.println("SA-Walk:        " + result2.cost + " (" + duration2 + " ms)");
        
        if (result1.cost < result2.cost) {
            double gap = (result2.cost - result1.cost) / result1.cost * 100;
            System.out.println("Winner: SA-Permutation (+" + String.format("%.2f", gap) + "% better)");
        } else {
            double gap = (result1.cost - result2.cost) / result2.cost * 100;
            System.out.println("Winner: SA-Walk (+" + String.format("%.2f", gap) + "% better)");
        }
        
        // Optimum (brute force za male grafove)
        if (n <= 10) {
            System.out.println("\n--- Computing Optimal Solution ---");
            long startOpt = System.currentTimeMillis();
            Result optimal = ClosedWalkSolver.solve(graph);
            long durationOpt = System.currentTimeMillis() - startOpt;
            
            System.out.println("Optimal: " + optimal.cost + " (" + durationOpt + " ms)");
            
            double gap1 = (result1.cost - optimal.cost) / optimal.cost * 100;
            double gap2 = (result2.cost - optimal.cost) / optimal.cost * 100;
            
            System.out.println("\nGap to optimal:");
            System.out.println("  SA-Permutation: " + String.format("%.2f", gap1) + "%");
            System.out.println("  SA-Walk:        " + String.format("%.2f", gap2) + "%");
        }
    }
}
