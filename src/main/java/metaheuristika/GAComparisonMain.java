package metaheuristika;

import utils.*;
import fer.*;
/**
 * Usporedba GA sa dvije reprezentacije:
 * 1. GA (standardni): Permutacija + min_distances
 * 2. GAWalk: Cijeli walk sa ponavljanjima
 */
public class GAComparisonMain {
    
    public static void main(String[] args) {
        // Test graf
        int n = 25;
        double[][] distances = GraphGenerator.generateRandomGraphSeed(n, 1234);
        Graph graph = new Graph(distances);
        
        System.out.println("=== GA Representation Comparison ===");
        System.out.println("Graph: n=" + n);
        System.out.println();
        
        int popSize = 50;
        int generations = 100;
        double mutationRate = 0.2;
        int printEvery = 10;
        
        System.out.println("Parameters:");
        System.out.println("  Population: " + popSize);
        System.out.println("  Generations: " + generations);
        System.out.println("  Mutation rate: " + mutationRate);
        System.out.println();
        
        // 1. Standardni GA (permutacija + min_distances)
        System.out.println("--- GA Standard (Permutation + min_distances) ---");
        long start1 = System.currentTimeMillis();
        Result result1 = GA.solve(graph, popSize, generations, mutationRate, printEvery);
        long duration1 = System.currentTimeMillis() - start1;
        
        System.out.println("\nGA Standard Result:");
        System.out.println("  Cost: " + result1.cost);
        System.out.println("  Tour length: " + result1.tour.size());
        System.out.println("  Tour: " + result1.tour);
        System.out.println("  Time: " + duration1 + " ms");
        System.out.println();
        
        // 2. GA-Walk (cijeli walk sa ponavljanjima)
        System.out.println("--- GA-Walk (Full walk with repetitions) ---");
        long start2 = System.currentTimeMillis();
        Result result2 = GAWalk.solve(graph, popSize, generations, mutationRate, printEvery);
        long duration2 = System.currentTimeMillis() - start2;
        
        System.out.println("\nGA-Walk Result:");
        System.out.println("  Cost: " + result2.cost);
        System.out.println("  Tour length: " + result2.tour.size());
        System.out.println("  Tour: " + result2.tour);
        System.out.println("  Time: " + duration2 + " ms");
        System.out.println();
        
        // Optimum
        System.out.println("--- Computing Optimal Solution ---");
        long startOpt = System.currentTimeMillis();
        Result optimal = ClosedWalkSolver.solve(graph);
        long durationOpt = System.currentTimeMillis() - startOpt;
        
        System.out.println("Optimal: " + optimal.cost + " (" + durationOpt + " ms)");
        System.out.println();
        
        // Usporedba
        System.out.println("=== Comparison ===");
        System.out.printf("GA Standard:  cost=%.2f, len=%d, time=%d ms%n", 
            result1.cost, result1.tour.size(), duration1);
        System.out.printf("GA Walk:      cost=%.2f, len=%d, time=%d ms%n", 
            result2.cost, result2.tour.size(), duration2);
        System.out.printf("Optimal:      cost=%.2f%n", optimal.cost);
        System.out.println();
        
        double gap1 = (result1.cost - optimal.cost) / optimal.cost * 100;
        double gap2 = (result2.cost - optimal.cost) / optimal.cost * 100;
        
        System.out.println("Gap to optimal:");
        System.out.printf("  GA Standard: %.2f%%%n", gap1);
        System.out.printf("  GA Walk:     %.2f%%%n", gap2);
        System.out.println();
        
        if (result1.cost < result2.cost) {
            double improvement = (result2.cost - result1.cost) / result1.cost * 100;
            System.out.printf("Winner: GA Standard (%.2f%% better, %.1fx faster)%n", 
                improvement, (double)duration2 / duration1);
        } else if (result2.cost < result1.cost) {
            double improvement = (result1.cost - result2.cost) / result2.cost * 100;
            System.out.printf("Winner: GA Walk (%.2f%% better, %.1fx faster)%n", 
                improvement, (double)duration1 / duration2);
        } else {
            System.out.printf("Tie! (Time difference: %.1fx)%n", 
                (double)Math.max(duration1, duration2) / Math.min(duration1, duration2));
        }
        
        System.out.println();
        System.out.println("Key difference:");
        System.out.println("  GA Standard: Fixed length (n=" + n + "), uses min_distances matrix");
        System.out.println("  GA Walk:     Variable length (" + result2.tour.size() + "), uses direct distances, allows repetitions");
    }
}
