package heuristika;

import fer.*;
import utils.*;
import java.util.*;

public class Main {

    static double round(double x) {
        return Math.round(x * 1e12) / 1e12;
    }
    
    private static final Random rand = new Random();

    public static void main(String[] args) {
        int n = 15; 
        int iter = 1000;
        
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║   USPOREDBA HEURISTIKA: Greedy, Random Perm, ASPW              ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println("N=" + n + ", Iterations=" + iter + "\n");

        // Timing
        long totalGreedyTime = 0;
        long totalRandomPermTime = 0;
        long totalAspwTime = 0;
        long totalOptimalTime = 0;

        // Costs
        double totalGreedyCost = 0;
        double totalRandomPermCost = 0;
        double totalAspwCost = 0;
        double totalOptimalCost = 0;
        
        // Gap tracking
        double totalGreedyGap = 0;
        double totalRandomPermGap = 0;
        double totalAspwGap = 0;
        
        // Win counters
        int greedyWins = 0;
        int randomPermWins = 0;
        int aspwWins = 0;

        for (int i = 0; i < iter; i++) {
            if (i % 20 == 0) System.out.println("Iteration: " + i);

            double[][] w = GraphGenerator.generateRandomGraph(n);
            Graph g = new Graph(w);

            // --- Optimal (Held-Karp) ---
            long optStart = System.nanoTime();
            Result optResult = ClosedWalkSolverParallel.solve(g);
            long optEnd = System.nanoTime();
            totalOptimalTime += (optEnd - optStart);
            double optimalCost = round(optResult.cost);
            totalOptimalCost += optimalCost;

            // --- Greedy + Min Walk ---
            long greedyStart = System.nanoTime();
            Result greedyResult = Greedy.solve(g);
            long greedyEnd = System.nanoTime();
            totalGreedyTime += (greedyEnd - greedyStart);
            double greedyCost = round(greedyResult.cost);
            totalGreedyCost += greedyCost;
            
            double greedyGap = (greedyCost - optimalCost) / optimalCost * 100;
            totalGreedyGap += greedyGap;

            // --- Random Permutation + Min Walk ---
            long randStart = System.nanoTime();
            Result randomResult = randomPermutationMinWalk(g);
            long randEnd = System.nanoTime();
            totalRandomPermTime += (randEnd - randStart);
            double randomCost = round(randomResult.cost);
            totalRandomPermCost += randomCost;
            
            double randomGap = (randomCost - optimalCost) / optimalCost * 100;
            totalRandomPermGap += randomGap;

            // --- ASPW ---
            long aspwStart = System.nanoTime();
            Result aspwResult = ASPW.solve(g);
            long aspwEnd = System.nanoTime();
            totalAspwTime += (aspwEnd - aspwStart);
            double aspwCost = round(aspwResult.cost);
            totalAspwCost += aspwCost;
            
            double aspwGap = (aspwCost - optimalCost) / optimalCost * 100;
            totalAspwGap += aspwGap;

            // Determine winner
            double minCost = Math.min(greedyCost, Math.min(randomCost, aspwCost));
            if (greedyCost == minCost) greedyWins++;
            if (randomCost == minCost) randomPermWins++;
            if (aspwCost == minCost) aspwWins++;
        }

        // Calculate averages
        double avgGreedyMs = (totalGreedyTime / 1e6) / iter;
        double avgRandomPermMs = (totalRandomPermTime / 1e6) / iter;
        double avgAspwMs = (totalAspwTime / 1e6) / iter;
        double avgOptimalMs = (totalOptimalTime / 1e6) / iter;

        double avgGreedyGap = totalGreedyGap / iter;
        double avgRandomPermGap = totalRandomPermGap / iter;
        double avgAspwGap = totalAspwGap / iter;

        // Print results
        System.out.println("\n════════════════════════════════════════════════════════════════");
        System.out.println("                         REZULTATI");
        System.out.println("════════════════════════════════════════════════════════════════");
        
        System.out.println("\n📊 PROSJEČNI TROŠKOVI:");
        System.out.printf("   Optimal (Held-Karp):      %.4f%n", totalOptimalCost / iter);
        System.out.printf("   Greedy + Min Walk:        %.4f%n", totalGreedyCost / iter);
        System.out.printf("   Random Perm + Min Walk:   %.4f%n", totalRandomPermCost / iter);
        System.out.printf("   ASPW:                     %.4f%n", totalAspwCost / iter);

        System.out.println("\n📈 PROSJEČNI GAP OD OPTIMUMA:");
        System.out.printf("   Greedy + Min Walk:        %.2f%%%n", avgGreedyGap);
        System.out.printf("   Random Perm + Min Walk:   %.2f%%%n", avgRandomPermGap);
        System.out.printf("   ASPW:                     %.2f%%%n", avgAspwGap);

        System.out.println("\n⏱️  PROSJEČNO VRIJEME (ms):");
        System.out.printf("   Optimal (Held-Karp):      %.3f%n", avgOptimalMs);
        System.out.printf("   Greedy + Min Walk:        %.3f%n", avgGreedyMs);
        System.out.printf("   Random Perm + Min Walk:   %.3f%n", avgRandomPermMs);
        System.out.printf("   ASPW:                     %.3f%n", avgAspwMs);

        System.out.println("\n🏆 POBJEDE (najbolji među heuristikama):");
        System.out.printf("   Greedy + Min Walk:        %d (%.1f%%)%n", greedyWins, greedyWins * 100.0 / iter);
        System.out.printf("   Random Perm + Min Walk:   %d (%.1f%%)%n", randomPermWins, randomPermWins * 100.0 / iter);
        System.out.printf("   ASPW:                     %d (%.1f%%)%n", aspwWins, aspwWins * 100.0 / iter);

        System.out.println("\n════════════════════════════════════════════════════════════════");
        
        // Find best heuristic
        String bestHeuristic;
        double bestGap;
        if (avgGreedyGap <= avgRandomPermGap && avgGreedyGap <= avgAspwGap) {
            bestHeuristic = "Greedy + Min Walk";
            bestGap = avgGreedyGap;
        } else if (avgAspwGap <= avgRandomPermGap) {
            bestHeuristic = "ASPW";
            bestGap = avgAspwGap;
        } else {
            bestHeuristic = "Random Perm + Min Walk";
            bestGap = avgRandomPermGap;
        }
        
        System.out.printf("🥇 NAJBOLJA HEURISTIKA: %s (avg gap: %.2f%%)%n", bestHeuristic, bestGap);
    }
    
    /**
     * Random permutation + min walk heuristic
     * Generira random permutaciju čvorova i računa MCW koristeći min_distances
     */
    private static Result randomPermutationMinWalk(Graph g) {
        int n = g.n;
        double[][] minDist = g.min_distances;
        
        // Generiraj random permutaciju čvorova 1..n-1 (0 je uvijek start)
        List<Integer> nodes = new ArrayList<>();
        for (int i = 1; i < n; i++) {
            nodes.add(i);
        }
        Collections.shuffle(nodes, rand);
        
        // Kreiraj tour: 0 -> shuffled nodes -> 0
        List<Integer> tour = new ArrayList<>();
        tour.add(0);
        tour.addAll(nodes);
        tour.add(0);
        
        // Izračunaj cost koristeći min_distances (shortest paths)
        double cost = 0;
        for (int i = 0; i < tour.size() - 1; i++) {
            cost += minDist[tour.get(i)][tour.get(i + 1)];
        }
        
        return new Result(cost, tour);
    }
}
