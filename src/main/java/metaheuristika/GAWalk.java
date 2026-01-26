package metaheuristika;

import java.util.*;
import utils.*;

/**
 * Genetic Algorithm za MCW problem - Walk verzija s Local Search
 * 
 * KLJUČNA RAZLIKA od GA Standard:
 * - Koristi min_distances (Floyd-Warshall) - ISPRAVNO za MCW!
 * - Dodaje local search (2-opt) za intenzifikaciju
 * - Elitizam za očuvanje najboljih rješenja
 * 
 * Time Complexity: O(generations * popSize * n²) zbog local search
 */
public class GAWalk {
    
    private static final Random rand = new Random();
    
    public static Result solve(Graph g, int popSize, int generations, double mutationRate, int printEvery) {
        int n = g.n;
        double[][] minDist = g.min_distances; // KORISTIMO MIN_DISTANCES!
        
        if (n <= 1) return new Result(0, Arrays.asList(0));
        
        // Inicijalizacija populacije (permutacije)
        List<int[]> population = initializePopulation(g, popSize);
        
        int[] bestPerm = null;
        double bestCost = Double.MAX_VALUE;
        
        // Elitizam - sačuvaj top 10%
        int eliteCount = Math.max(1, popSize / 10);
        
        for (int gen = 0; gen < generations; gen++) {
            // Evaluacija i sortiranje
            double[] fitness = new double[popSize];
            Integer[] indices = new Integer[popSize];
            
            for (int i = 0; i < popSize; i++) {
                fitness[i] = evaluatePerm(population.get(i), minDist);
                indices[i] = i;
            }
            
            // Sortiraj po fitness (ascending - manji je bolji)
            Arrays.sort(indices, (a, b) -> Double.compare(fitness[a], fitness[b]));
            
            // Update best
            if (fitness[indices[0]] < bestCost) {
                bestCost = fitness[indices[0]];
                bestPerm = population.get(indices[0]).clone();
            }
            
            if (printEvery > 0 && gen % printEvery == 0) {
                double avgFitness = Arrays.stream(fitness).average().orElse(0.0);
                System.out.printf("Gen %d: Best=%.4f, Avg=%.4f%n", gen, bestCost, avgFitness);
            }
            
            // Nova generacija s elitizmom
            List<int[]> newPopulation = new ArrayList<>();
            
            // Sačuvaj elite
            for (int i = 0; i < eliteCount; i++) {
                newPopulation.add(population.get(indices[i]).clone());
            }
            
            // Generiraj ostatak
            while (newPopulation.size() < popSize) {
                // Tournament selection
                int[] parent1 = tournamentSelect(population, fitness, 3);
                int[] parent2 = tournamentSelect(population, fitness, 3);
                
                // Order Crossover (OX)
                int[] offspring = orderCrossover(parent1, parent2);
                
                // Mutation (swap mutation)
                if (rand.nextDouble() < mutationRate) {
                    swapMutation(offspring);
                }
                
                // Local search na dijelu populacije (intenzifikacija)
                if (rand.nextDouble() < 0.1) { // 10% šanse za local search
                    offspring = twoOptImprove(offspring, minDist);
                }
                
                newPopulation.add(offspring);
            }
            
            population = newPopulation;
        }
        
        // Finalni local search na best solution
        if (bestPerm != null) {
            bestPerm = twoOptImprove(bestPerm, minDist);
            bestCost = evaluatePerm(bestPerm, minDist);
        }
        
        return new Result(bestCost, permToWalk(bestPerm, g));
    }
    
    private static List<int[]> initializePopulation(Graph g, int popSize) {
        int n = g.n;
        List<int[]> population = new ArrayList<>();
        
        // Greedy nearest neighbor
        population.add(greedyPerm(g));
        
        // Random permutacije
        for (int i = 1; i < popSize; i++) {
            int[] perm = new int[n];
            for (int j = 0; j < n; j++) perm[j] = j;
            shuffleArray(perm);
            population.add(perm);
        }
        
        return population;
    }
    
    private static int[] greedyPerm(Graph g) {
        int n = g.n;
        double[][] minDist = g.min_distances;
        int[] perm = new int[n];
        boolean[] visited = new boolean[n];
        
        perm[0] = 0;
        visited[0] = true;
        
        for (int i = 1; i < n; i++) {
            int best = -1;
            double bestDist = Double.MAX_VALUE;
            for (int j = 0; j < n; j++) {
                if (!visited[j] && minDist[perm[i-1]][j] < bestDist) {
                    bestDist = minDist[perm[i-1]][j];
                    best = j;
                }
            }
            perm[i] = best;
            visited[best] = true;
        }
        
        return perm;
    }
    
    private static double evaluatePerm(int[] perm, double[][] minDist) {
        double cost = 0;
        int n = perm.length;
        for (int i = 0; i < n - 1; i++) {
            cost += minDist[perm[i]][perm[i + 1]];
        }
        cost += minDist[perm[n - 1]][perm[0]];
        return cost;
    }
    
    private static int[] tournamentSelect(List<int[]> population, double[] fitness, int k) {
        int best = rand.nextInt(population.size());
        for (int i = 1; i < k; i++) {
            int competitor = rand.nextInt(population.size());
            if (fitness[competitor] < fitness[best]) {
                best = competitor;
            }
        }
        return population.get(best);
    }
    
    /**
     * Order Crossover (OX) - standardni crossover za permutacije
     */
    private static int[] orderCrossover(int[] parent1, int[] parent2) {
        int n = parent1.length;
        int[] offspring = new int[n];
        Arrays.fill(offspring, -1);
        
        // Izaberi segment iz parent1
        int start = rand.nextInt(n);
        int end = rand.nextInt(n);
        if (start > end) { int tmp = start; start = end; end = tmp; }
        
        // Kopiraj segment
        Set<Integer> used = new HashSet<>();
        for (int i = start; i <= end; i++) {
            offspring[i] = parent1[i];
            used.add(parent1[i]);
        }
        
        // Popuni ostatak iz parent2
        int pos = (end + 1) % n;
        for (int i = 0; i < n; i++) {
            int idx = (end + 1 + i) % n;
            int gene = parent2[idx];
            if (!used.contains(gene)) {
                offspring[pos] = gene;
                pos = (pos + 1) % n;
            }
        }
        
        return offspring;
    }
    
    private static void swapMutation(int[] perm) {
        int n = perm.length;
        int i = rand.nextInt(n);
        int j = rand.nextInt(n);
        int tmp = perm[i];
        perm[i] = perm[j];
        perm[j] = tmp;
    }
    
    /**
     * 2-opt local search za poboljšanje permutacije
     */
    private static int[] twoOptImprove(int[] perm, double[][] minDist) {
        int n = perm.length;
        int[] best = perm.clone();
        double bestCost = evaluatePerm(best, minDist);
        
        boolean improved = true;
        int maxIter = 100; // Ograniči iteracije
        int iter = 0;
        
        while (improved && iter++ < maxIter) {
            improved = false;
            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 2; j < n; j++) {
                    // Probaj 2-opt swap
                    double delta = calculate2OptDelta(best, i, j, minDist);
                    if (delta < -1e-10) {
                        reverse(best, i + 1, j);
                        bestCost += delta;
                        improved = true;
                    }
                }
            }
        }
        
        return best;
    }
    
    private static double calculate2OptDelta(int[] perm, int i, int j, double[][] d) {
        int n = perm.length;
        int a = perm[i];
        int b = perm[i + 1];
        int c = perm[j];
        int d_node = perm[(j + 1) % n];
        
        double oldDist = d[a][b] + d[c][d_node];
        double newDist = d[a][c] + d[b][d_node];
        
        return newDist - oldDist;
    }
    
    private static void reverse(int[] arr, int start, int end) {
        while (start < end) {
            int tmp = arr[start];
            arr[start] = arr[end];
            arr[end] = tmp;
            start++;
            end--;
        }
    }
    
    private static void shuffleArray(int[] arr) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }
    
    /**
     * Pretvori permutaciju u walk - jednostavno vraća permutaciju kao listu
     * (za MCW, cost se računa iz min_distances, walk je samo redoslijed čvorova)
     */
    private static List<Integer> permToWalk(int[] perm, Graph g) {
        List<Integer> walk = new ArrayList<>();
        if (perm == null || perm.length == 0) return walk;
        
        for (int node : perm) {
            walk.add(node);
        }
        // Zatvori walk
        walk.add(perm[0]);
        
        return walk;
    }
    
    public static Result solve(Graph g) {
        return solve(g, 100, 100, 0.3, 0);
    }
}
