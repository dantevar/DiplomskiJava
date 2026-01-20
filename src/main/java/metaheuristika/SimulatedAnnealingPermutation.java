package metaheuristika;

import java.util.*;
import utils.*;

/**
 * Simulated Annealing za MCW problem - Permutacija verzija
 * 
 * Reprezentacija: Permutacija čvorova [0,1,2,...,n-1]
 * Fitness: Duljina minimum closed walk-a preko permutacije
 * 
 * Susjedstvo: 2-opt swap (reverse podziza)
 * 
 * Annealing Schedule:
 * - Početna temperatura: T0 (visoka - prihvaća gotovo sve)
 * - Hlađenje: T = T * alpha (geometric cooling)
 * - Iteracija po temperaturi: iterationsPerTemp
 * 
 * Metropolis kriterij:
 * - Ako je novo bolje: prihvati
 * - Ako je lošije: prihvati s P = exp(-delta/T)
 * 
 * Time Complexity: O(iterations * n) za fitness evaluacije
 */
public class SimulatedAnnealingPermutation {
    
    private static final Random rand = new Random();
    
    /**
     * @param g Graf
     * @param T0 Početna temperatura
     * @param alpha Faktor hlađenja (0 < alpha < 1, npr. 0.95)
     * @param iterationsPerTemp Broj iteracija po temperaturi
     * @param minTemp Minimalna temperatura (stop uvjet)
     * @param printEvery Koliko često ispisivati (0 = ne ispisuj)
     */
    public static Result solve(Graph g, double T0, double alpha, int iterationsPerTemp, 
                                double minTemp, int printEvery) {
        int n = g.n;
        double[][] distances = g.min_distances;
        
        if (n <= 1) return new Result(0, Arrays.asList(0));
        
        // Inicijalno rješenje: random permutacija
        int[] current = randomPermutation(n);
        double currentCost = evaluatePermutation(current, distances);
        
        // Najbolje rješenje
        int[] best = current.clone();
        double bestCost = currentCost;
        
        double T = T0;
        int iteration = 0;
        
        while (T > minTemp) {
            for (int i = 0; i < iterationsPerTemp; i++) {
                iteration++;
                
                // Generiraj susjeda: 2-opt swap
                int[] neighbor = generate2OptNeighbor(current);
                double neighborCost = evaluatePermutation(neighbor, distances);
                
                // Delta energije
                double delta = neighborCost - currentCost;
                
                // Metropolis kriterij
                if (delta < 0 || rand.nextDouble() < Math.exp(-delta / T)) {
                    current = neighbor;
                    currentCost = neighborCost;
                    
                    // Update best
                    if (currentCost < bestCost) {
                        best = current.clone();
                        bestCost = currentCost;
                    }
                }
                
                if (printEvery > 0 && iteration % printEvery == 0) {
                    System.out.printf("Iter %d, T=%.2f: Current=%.2f, Best=%.2f%n", 
                        iteration, T, currentCost, bestCost);
                }
            }
            
            // Hlađenje
            T *= alpha;
        }
        
        // Konverzija u walk
        List<Integer> tour = new ArrayList<>();
        for (int node : best) {
            tour.add(node);
        }
        
        return new Result(bestCost, tour);
    }
    
    /**
     * Evaluira permutaciju kao minimum closed walk.
     * Walk se gradi idući po permutaciji i koristeći min_distances.
     */
    private static double evaluatePermutation(int[] perm, double[][] distances) {
        int n = perm.length;
        double cost = 0.0;
        
        for (int i = 0; i < n; i++) {
            int from = perm[i];
            int to = perm[(i + 1) % n];
            cost += distances[from][to];
        }
        
        return cost;
    }
    
    /**
     * 2-opt swap: Reverse segment [i+1, j]
     */
    private static int[] generate2OptNeighbor(int[] perm) {
        int n = perm.length;
        int[] neighbor = perm.clone();
        
        int i = rand.nextInt(n);
        int j = rand.nextInt(n);
        
        if (i > j) {
            int temp = i;
            i = j;
            j = temp;
        }
        
        // Reverse segment [i+1, j]
        if (i < j) {
            reverse(neighbor, i + 1, j);
        }
        
        return neighbor;
    }
    
    private static void reverse(int[] arr, int start, int end) {
        while (start < end) {
            int temp = arr[start];
            arr[start] = arr[end];
            arr[end] = temp;
            start++;
            end--;
        }
    }
    
    private static int[] randomPermutation(int n) {
        int[] perm = new int[n];
        for (int i = 0; i < n; i++) {
            perm[i] = i;
        }
        
        // Fisher-Yates shuffle
        for (int i = n - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = perm[i];
            perm[i] = perm[j];
            perm[j] = temp;
        }
        
        return perm;
    }
    
    /**
     * Default parametri za brzo testiranje
     */
    public static Result solve(Graph g) {
        return solve(g, 100.0, 0.95, 100, 0.01, 0);
    }
}
