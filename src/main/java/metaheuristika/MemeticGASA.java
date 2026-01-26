package metaheuristika;

import java.util.*;
import utils.*;

/**
 * Memetic Algorithm: Genetic Algorithm + Simulated Annealing Hybrid
 * 
 * Kombinira globalni search (GA) s lokalnom optimizacijom (SA).
 * Također poznat kao "Lamarckian Evolution" - poboljšanja se nasljeđuju.
 * 
 * Struktura:
 * 1. GA kreira offspringe (crossover + mutation)
 * 2. SA lokalno optimizira svaki offspring
 * 3. Poboljšani offsprinzi ulaze u sljedeću generaciju
 * 
 * Strategija: Lamarckian (SA trajno mijenja offspringe)
 * Reprezentacija: Walk s ponavljanjima čvorova
 * 
 * Očekivani rezultati:
 * - 2-3x bolji gap od pure GA (ali ~2x sporije)
 * - Najbolji trade-off kvalitete i vremena
 */
public class MemeticGASA {
    
    private static final Random rand = new Random();
    
    /**
     * Solve with default parameters
     */
    public static Result solve(Graph g) {
        return solve(g, 40, 80, 0.2, 0.5, 0);
    }
    
    /**
     * Memetic GA-SA solver
     * 
     * @param g Graph instance
     * @param popSize Population size (preporučeno: 30-50)
     * @param generations Number of GA generations
     * @param mutationRate Mutation probability
     * @param saApplyRate Probability of applying SA to offspring (0.3-0.5)
     * @param printEvery Print progress every N generations (0 = no print)
     * @return Best solution
     */
    public static Result solve(Graph g, int popSize, int generations, 
                               double mutationRate, double saApplyRate, 
                               int printEvery) {
        int n = g.n;
        double[][] distances = g.min_distances;  // MORA biti min_distances za MCW!
        
        if (n <= 1) return new Result(0, Arrays.asList(0));
        
        // === INITIALIZATION ===
        List<List<Integer>> population = initializePopulation(g, popSize);
        
        // Apply initial SA to all individuals (short)
        for (int i = 0; i < popSize; i++) {
            population.set(i, localSearchSA(population.get(i), g, 30.0, 0.95, 20));
        }
        
        List<Integer> globalBest = null;
        double globalBestCost = Double.MAX_VALUE;
        
        // === MAIN GA-SA LOOP ===
        for (int gen = 0; gen < generations; gen++) {
            // Evaluate population
            double[] fitness = new double[popSize];
            for (int i = 0; i < popSize; i++) {
                fitness[i] = evaluateWalk(population.get(i), distances);
                if (fitness[i] < globalBestCost) {
                    globalBestCost = fitness[i];
                    globalBest = new ArrayList<>(population.get(i));
                }
            }
            
            if (printEvery > 0 && gen % printEvery == 0) {
                double avgFitness = Arrays.stream(fitness).average().orElse(0.0);
                System.out.printf("Gen %d: Best=%.4f, Avg=%.4f, BestLen=%d%n", 
                    gen, globalBestCost, avgFitness, globalBest.size());
            }
            
            // Adaptive SA parameters based on generation
            SAParams saParams = getAdaptiveSAParams(gen, generations);
            
            // === CREATE NEW GENERATION ===
            List<List<Integer>> newPopulation = new ArrayList<>();
            
            while (newPopulation.size() < popSize) {
                // Selection
                List<Integer> parent1 = tournamentSelection(population, fitness, 3);
                List<Integer> parent2 = tournamentSelection(population, fitness, 3);
                
                // Crossover
                List<Integer> offspring = crossover(parent1, parent2, n);
                
                // Mutation
                if (rand.nextDouble() < mutationRate) {
                    mutate(offspring, n);
                }
                
                // === MEMETIC: Local Search SA ===
                // Apply SA to improve offspring
                if (rand.nextDouble() < saApplyRate) {
                    offspring = localSearchSA(offspring, g, 
                        saParams.T0, saParams.alpha, saParams.iterations);
                }
                
                newPopulation.add(offspring);
            }
            
            population = newPopulation;
            
            // === ELITE INTENSIVE SEARCH ===
            // Every 10 generations, apply intensive SA to global best
            if (gen > 0 && gen % 10 == 0) {
                globalBest = localSearchSA(globalBest, g, 80.0, 0.90, 150);
                globalBestCost = evaluateWalk(globalBest, distances);
            }
        }
        
        // Final intensive optimization on best solution
        globalBest = localSearchSA(globalBest, g, 100.0, 0.88, 200);
        globalBestCost = evaluateWalk(globalBest, distances);
        
        return new Result(globalBestCost, globalBest);
    }
    
    /**
     * Initialize population: 1 greedy + rest random walks
     */
    private static List<List<Integer>> initializePopulation(Graph g, int popSize) {
        List<List<Integer>> population = new ArrayList<>();
        int n = g.n;
        
        // First: greedy walk
        population.add(greedyWalk(g));
        
        // Rest: random walks
        for (int i = 1; i < popSize; i++) {
            population.add(randomWalk(n));
        }
        
        return population;
    }
    
    private static List<Integer> randomWalk(int n) {
        List<Integer> walk = new ArrayList<>();
        
        // Start with all nodes in random order
        List<Integer> nodes = new ArrayList<>();
        for (int i = 0; i < n; i++) nodes.add(i);
        Collections.shuffle(nodes, rand);
        walk.addAll(nodes);
        
        // Add some random duplicates
        int extraNodes = rand.nextInt(n);
        for (int i = 0; i < extraNodes; i++) {
            walk.add(rand.nextInt(n));
        }
        
        return walk;
    }
    
    private static List<Integer> greedyWalk(Graph g) {
        int n = g.n;
        double[][] distances = g.min_distances;
        
        List<Integer> walk = new ArrayList<>();
        boolean[] visited = new boolean[n];
        
        int current = 0;
        walk.add(current);
        visited[current] = true;
        
        for (int i = 1; i < n; i++) {
            int next = -1;
            double minDist = Double.MAX_VALUE;
            
            for (int j = 0; j < n; j++) {
                if (!visited[j] && distances[current][j] < minDist) {
                    minDist = distances[current][j];
                    next = j;
                }
            }
            
            walk.add(next);
            visited[next] = true;
            current = next;
        }
        
        return walk;
    }
    
    /**
     * LOCAL SEARCH: Simulated Annealing
     * 
     * Poboljšava walk kroz lokalni search s prihvaćanjem lošijih rješenja.
     */
    private static List<Integer> localSearchSA(List<Integer> walk, Graph g,
                                                double T0, double alpha, int iterations) {
        List<Integer> current = new ArrayList<>(walk);
        double currentCost = evaluateWalk(current, g.min_distances);
        
        List<Integer> best = new ArrayList<>(current);
        double bestCost = currentCost;
        
        double T = T0;
        int n = g.n;
        
        for (int iter = 0; iter < iterations; iter++) {
            // Generate neighbor
            List<Integer> neighbor = generateNeighbor(current, n);
            double neighborCost = evaluateWalk(neighbor, g.min_distances);
            
            // Accept or reject
            double delta = neighborCost - currentCost;
            
            if (delta < 0 || rand.nextDouble() < Math.exp(-delta / T)) {
                current = neighbor;
                currentCost = neighborCost;
                
                // Update best
                if (currentCost < bestCost) {
                    best = new ArrayList<>(current);
                    bestCost = currentCost;
                }
            }
            
            // Cooling
            T *= alpha;
        }
        
        return best;
    }
    
    /**
     * Generate neighbor solution using various operators
     */
    private static List<Integer> generateNeighbor(List<Integer> walk, int n) {
        List<Integer> neighbor = new ArrayList<>(walk);
        
        if (neighbor.isEmpty()) return neighbor;
        
        int operation = rand.nextInt(5);
        
        switch (operation) {
            case 0: // 2-opt (reverse segment)
                if (neighbor.size() >= 4) {
                    int i = rand.nextInt(neighbor.size() - 2);
                    int j = i + 2 + rand.nextInt(neighbor.size() - i - 2);
                    Collections.reverse(neighbor.subList(i + 1, j));
                }
                break;
                
            case 1: // Insert random node
                int pos = rand.nextInt(neighbor.size() + 1);
                neighbor.add(pos, rand.nextInt(n));
                break;
                
            case 2: // Delete duplicate (safe)
                if (neighbor.size() > n) {
                    deleteDuplicate(neighbor, n);
                }
                break;
                
            case 3: // Swap two positions
                if (neighbor.size() >= 2) {
                    int a = rand.nextInt(neighbor.size());
                    int b = rand.nextInt(neighbor.size());
                    Collections.swap(neighbor, a, b);
                }
                break;
                
            case 4: // Replace node
                if (!neighbor.isEmpty()) {
                    int idx = rand.nextInt(neighbor.size());
                    neighbor.set(idx, rand.nextInt(n));
                }
                break;
        }
        
        return neighbor;
    }
    
    private static void deleteDuplicate(List<Integer> walk, int n) {
        // Count occurrences
        Map<Integer, Integer> counts = new HashMap<>();
        for (int node : walk) {
            counts.put(node, counts.getOrDefault(node, 0) + 1);
        }
        
        // Find positions of duplicates
        List<Integer> duplicatePositions = new ArrayList<>();
        for (int i = 0; i < walk.size(); i++) {
            if (counts.get(walk.get(i)) > 1) {
                duplicatePositions.add(i);
            }
        }
        
        // Remove random duplicate
        if (!duplicatePositions.isEmpty()) {
            int posToRemove = duplicatePositions.get(rand.nextInt(duplicatePositions.size()));
            walk.remove(posToRemove);
        }
    }
    
    /**
     * Tournament selection
     */
    private static List<Integer> tournamentSelection(List<List<Integer>> population, 
                                                      double[] fitness, 
                                                      int tournamentSize) {
        int best = rand.nextInt(population.size());
        
        for (int i = 1; i < tournamentSize; i++) {
            int competitor = rand.nextInt(population.size());
            if (fitness[competitor] < fitness[best]) {
                best = competitor;
            }
        }
        
        return new ArrayList<>(population.get(best));
    }
    
    /**
     * Crossover: segment exchange
     */
    private static List<Integer> crossover(List<Integer> parent1, 
                                           List<Integer> parent2, int n) {
        if (parent1.isEmpty() || parent2.isEmpty()) {
            return new ArrayList<>(parent1.isEmpty() ? parent2 : parent1);
        }
        
        int len1 = parent1.size();
        int len2 = parent2.size();
        
        // Take segment from parent1
        int start = rand.nextInt(len1);
        int end = start + 1 + rand.nextInt(Math.max(1, len1 - start));
        
        List<Integer> offspring = new ArrayList<>();
        
        // Add segment from parent1
        for (int i = start; i < end && i < len1; i++) {
            offspring.add(parent1.get(i));
        }
        
        // Add nodes from parent2
        for (int node : parent2) {
            offspring.add(node);
        }
        
        // Ensure all nodes are covered
        Set<Integer> covered = new HashSet<>(offspring);
        for (int i = 0; i < n; i++) {
            if (!covered.contains(i)) {
                offspring.add(i);
            }
        }
        
        return offspring;
    }
    
    /**
     * Mutation: apply random modification
     */
    private static void mutate(List<Integer> walk, int n) {
        if (walk.isEmpty()) return;
        
        int operation = rand.nextInt(4);
        
        switch (operation) {
            case 0: // Insert
                walk.add(rand.nextInt(walk.size() + 1), rand.nextInt(n));
                break;
            case 1: // Delete duplicate
                if (walk.size() > n) {
                    deleteDuplicate(walk, n);
                }
                break;
            case 2: // Swap
                if (walk.size() >= 2) {
                    int i = rand.nextInt(walk.size());
                    int j = rand.nextInt(walk.size());
                    Collections.swap(walk, i, j);
                }
                break;
            case 3: // Replace
                walk.set(rand.nextInt(walk.size()), rand.nextInt(n));
                break;
        }
    }
    
    /**
     * Evaluate walk fitness
     */
    private static double evaluateWalk(List<Integer> walk, double[][] distances) {
        if (walk.size() < 2) return Double.MAX_VALUE;
        
        int n = distances.length;
        Set<Integer> visited = new HashSet<>(walk);
        
        // Must cover all nodes
        if (visited.size() < n) {
            return Double.MAX_VALUE;
        }
        
        double cost = 0.0;
        for (int i = 0; i < walk.size() - 1; i++) {
            cost += distances[walk.get(i)][walk.get(i + 1)];
        }
        cost += distances[walk.get(walk.size() - 1)][walk.get(0)];
        
        return cost;
    }
    
    /**
     * Adaptive SA parameters based on generation progress
     */
    private static SAParams getAdaptiveSAParams(int gen, int maxGen) {
        // Early generations: Exploration (high T, more iterations)
        if (gen < maxGen / 3) {
            return new SAParams(50.0, 0.95, 40);
        } 
        // Middle: Balance
        else if (gen < 2 * maxGen / 3) {
            return new SAParams(30.0, 0.92, 60);
        } 
        // Late: Exploitation (low T, many iterations)
        else {
            return new SAParams(15.0, 0.88, 80);
        }
    }
    
    /**
     * SA parameters container
     */
    private static class SAParams {
        double T0;
        double alpha;
        int iterations;
        
        SAParams(double T0, double alpha, int iterations) {
            this.T0 = T0;
            this.alpha = alpha;
            this.iterations = iterations;
        }
    }
}
