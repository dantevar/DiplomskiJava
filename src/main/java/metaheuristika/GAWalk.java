package metaheuristika;

import java.util.*;
import utils.*;

/**
 * Genetic Algorithm za MCW problem - Walk verzija
 * 
 * Reprezentacija: Cijeli walk kao lista čvorova (može imati ponavljanja)
 * - Walk mora posjećivati sve čvorove
 * - Duljina može varirati
 * 
 * Genetski operatori:
 * - Crossover: Segment exchange (uzmi segment iz parent1, ostatak iz parent2)
 * - Mutation: Insert/Delete/Swap/Replace operacije
 * - Selection: Tournament selection
 * 
 * Time Complexity: O(generations * popSize * avgWalkLength)
 */
public class GAWalk {
    
    private static final Random rand = new Random();
    
    public static Result solve(Graph g, int popSize, int generations, double mutationRate, int printEvery) {
        int n = g.n;
        double[][] distances = g.distance_matrix;
        
        if (n <= 1) return new Result(0, Arrays.asList(0));
        
        // Inicijalizacija populacije
        List<List<Integer>> population = initializePopulation(g, popSize);
        
        List<Integer> bestWalk = null;
        double bestCost = Double.MAX_VALUE;
        
        for (int gen = 0; gen < generations; gen++) {
            // Evaluacija
            double[] fitness = new double[popSize];
            for (int i = 0; i < popSize; i++) {
                fitness[i] = evaluateWalk(population.get(i), distances);
                if (fitness[i] < bestCost) {
                    bestCost = fitness[i];
                    bestWalk = new ArrayList<>(population.get(i));
                }
            }
            
            if (printEvery > 0 && gen % printEvery == 0) {
                double avgFitness = Arrays.stream(fitness).average().orElse(0.0);
                System.out.printf("Gen %d: Best=%.2f, Avg=%.2f, BestLen=%d%n", 
                    gen, bestCost, avgFitness, bestWalk.size());
            }
            
            // Nova generacija
            List<List<Integer>> newPopulation = new ArrayList<>();
            
            while (newPopulation.size() < popSize) {
                // Tournament selection
                List<Integer> parent1 = tournamentSelection(population, fitness);
                List<Integer> parent2 = tournamentSelection(population, fitness);
                
                // Crossover
                List<Integer> offspring = crossover(parent1, parent2, n);
                
                // Mutation
                if (rand.nextDouble() < mutationRate) {
                    mutate(offspring, n);
                }
                
                newPopulation.add(offspring);
            }
            
            population = newPopulation;
        }
        
        return new Result(bestCost, bestWalk);
    }
    
    private static List<List<Integer>> initializePopulation(Graph g, int popSize) {
        List<List<Integer>> population = new ArrayList<>();
        int n = g.n;
        
        // Prvo rješenje: greedy
        population.add(greedyWalk(g));
        
        // Ostala rješenja: random walks sa varijabilnom duljinom
        for (int i = 1; i < popSize; i++) {
            List<Integer> walk = randomWalk(n);
            population.add(walk);
        }
        
        return population;
    }
    
    private static List<Integer> randomWalk(int n) {
        List<Integer> walk = new ArrayList<>();
        boolean[] visited = new boolean[n];
        
        // Prvo posjeti sve čvorove (random redoslijed)
        List<Integer> nodes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            nodes.add(i);
        }
        Collections.shuffle(nodes, rand);
        walk.addAll(nodes);
        
        // Dodaj random duplikate (produlji walk)
        int extraNodes = rand.nextInt(n); // Do n dodatnih čvorova
        for (int i = 0; i < extraNodes; i++) {
            walk.add(rand.nextInt(n));
        }
        
        return walk;
    }
    
    private static List<Integer> greedyWalk(Graph g) {
        int n = g.n;
        double[][] distances = g.distance_matrix;
        
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
    
    private static double evaluateWalk(List<Integer> walk, double[][] distances) {
        if (walk.size() < 2) return Double.MAX_VALUE;
        
        int n = distances.length;
        Set<Integer> visited = new HashSet<>(walk);
        
        // Walk MORA pokrivati sve čvorove
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
    
    private static List<Integer> tournamentSelection(List<List<Integer>> population, double[] fitness) {
        int tournamentSize = 3;
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
     * Segment exchange crossover
     * Uzmi segment iz parent1, ostatak iz parent2, dodaj missing čvorove ako treba
     */
    private static List<Integer> crossover(List<Integer> parent1, List<Integer> parent2, int n) {
        if (parent1.isEmpty() || parent2.isEmpty()) {
            return new ArrayList<>(parent1.isEmpty() ? parent2 : parent1);
        }
        
        int len1 = parent1.size();
        int len2 = parent2.size();
        
        // Izaberi random segment iz parent1
        int start = rand.nextInt(len1);
        int end = start + 1 + rand.nextInt(Math.max(1, len1 - start));
        
        List<Integer> offspring = new ArrayList<>();
        
        // Dodaj segment iz parent1
        for (int i = start; i < end && i < len1; i++) {
            offspring.add(parent1.get(i));
        }
        
        // Dodaj čvorove iz parent2
        for (int node : parent2) {
            offspring.add(node);
        }
        
        // Provjeri da su svi čvorovi pokriveni i dodaj missing
        Set<Integer> covered = new HashSet<>(offspring);
        
        for (int i = 0; i < n; i++) {
            if (!covered.contains(i)) {
                offspring.add(i);
            }
        }
        
        if (offspring.isEmpty()) {
            return new ArrayList<>(parent1);
        }
        
        return offspring;
    }
    
    /**
     * Mutation: primijeni random operaciju
     */
    private static void mutate(List<Integer> walk, int n) {
        if (walk.isEmpty()) return;
        
        int operation = rand.nextInt(4);
        
        switch (operation) {
            case 0: // Insert
                int node = rand.nextInt(n);
                int pos = rand.nextInt(walk.size() + 1);
                walk.add(pos, node);
                break;
            case 1: // Delete (samo duplikate)
                if (walk.size() > n) {
                    deleteDuplicate(walk, n);
                }
                break;
            case 2: // Swap
                if (walk.size() >= 2) {
                    int i = rand.nextInt(walk.size());
                    int j = rand.nextInt(walk.size());
                    int temp = walk.get(i);
                    walk.set(i, walk.get(j));
                    walk.set(j, temp);
                }
                break;
            case 3: // Replace
                if (walk.size() >= 1) {
                    int idx = rand.nextInt(walk.size());
                    walk.set(idx, rand.nextInt(n));
                }
                break;
        }
    }
    
    private static void deleteDuplicate(List<Integer> walk, int n) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int node : walk) {
            counts.put(node, counts.getOrDefault(node, 0) + 1);
        }
        
        List<Integer> duplicates = new ArrayList<>();
        for (int i = 0; i < walk.size(); i++) {
            if (counts.get(walk.get(i)) > 1) {
                duplicates.add(i);
            }
        }
        
        if (!duplicates.isEmpty()) {
            int posToDelete = duplicates.get(rand.nextInt(duplicates.size()));
            walk.remove(posToDelete);
        }
    }
    
    public static Result solve(Graph g) {
        return solve(g, 50, 100, 0.2, 0);
    }
}
