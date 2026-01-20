package metaheuristika;

import java.util.*;
import utils.*;

/**
 * Simulated Annealing za MCW problem - Walk verzija
 * 
 * Reprezentacija: Cijeli walk kao lista čvorova [0, 3, 1, 3, 2, 0]
 * - Walk može imati ponavljanja (nije permutacija)
 * - Duljina može varirati
 * - MORA posjećivati sve čvorove!
 * 
 * Fitness: Suma udaljenosti uzastopnih bridova
 * 
 * Susjedstvo operacije:
 * 1. Insert node: Ubaci random čvor na random poziciju
 * 2. Delete node: Obriši čvor (samo duplikate)
 * 3. Swap nodes: Zamijeni dva čvora
 * 4. Replace node: Zamijeni čvor drugim iz walka
 */
public class SimulatedAnnealingWalk {
    
    private static final Random rand = new Random();
    
    public static Result solve(Graph g, double T0, double alpha, int iterationsPerTemp, 
                                double minTemp, int printEvery) {
        int n = g.n;
        double[][] distances = g.distance_matrix;
        
        if (n <= 1) return new Result(0, Arrays.asList(0));
        
        // Inicijalno rješenje: greedy walk
        List<Integer> current = greedyWalk(g);
        double currentCost = evaluateWalk(current, distances);
        
        // Najbolje rješenje
        List<Integer> best = new ArrayList<>(current);
        double bestCost = currentCost;
        
        double T = T0;
        int iteration = 0;
        
        while (T > minTemp) {
            for (int i = 0; i < iterationsPerTemp; i++) {
                iteration++;
                
                // Generiraj susjeda
                List<Integer> neighbor = generateNeighbor(current, n);
                double neighborCost = evaluateWalk(neighbor, distances);
                
                // Metropolis kriterij
                double delta = neighborCost - currentCost;
                
                if (delta < 0 || rand.nextDouble() < Math.exp(-delta / T)) {
                    current = neighbor;
                    currentCost = neighborCost;
                    
                    if (currentCost < bestCost) {
                        best = new ArrayList<>(current);
                        bestCost = currentCost;
                    }
                }
                
                if (printEvery > 0 && iteration % printEvery == 0) {
                    System.out.printf("Iter %d, T=%.2f: Current=%.2f (len=%d), Best=%.2f (len=%d)%n", 
                        iteration, T, currentCost, current.size(), bestCost, best.size());
                }
            }
            
            T *= alpha;
        }
        
        return new Result(bestCost, best);
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
    
    private static List<Integer> generateNeighbor(List<Integer> walk, int n) {
        List<Integer> neighbor = new ArrayList<>(walk);
        
        int operation = rand.nextInt(4);
        
        switch (operation) {
            case 0: // Insert
                insertRandomNode(neighbor, n);
                break;
            case 1: // Delete (samo duplikate)
                if (neighbor.size() > n) {
                    deleteRandomNodeSafe(neighbor, n);
                }
                break;
            case 2: // Swap
                if (neighbor.size() >= 2) {
                    swapNodes(neighbor);
                }
                break;
            case 3: // Replace
                if (neighbor.size() >= 2) {
                    replaceNodeSafe(neighbor);
                }
                break;
        }
        
        return neighbor;
    }
    
    private static void insertRandomNode(List<Integer> walk, int n) {
        int node = rand.nextInt(n);
        int pos = rand.nextInt(walk.size() + 1);
        walk.add(pos, node);
    }
    
    private static void deleteRandomNodeSafe(List<Integer> walk, int n) {
        if (walk.size() <= n) return;
        
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
    
    private static void swapNodes(List<Integer> walk) {
        int i = rand.nextInt(walk.size());
        int j = rand.nextInt(walk.size());
        
        int temp = walk.get(i);
        walk.set(i, walk.get(j));
        walk.set(j, temp);
    }
    
    private static void replaceNodeSafe(List<Integer> walk) {
        int pos = rand.nextInt(walk.size());
        int otherPos = rand.nextInt(walk.size());
        walk.set(pos, walk.get(otherPos));
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
    
    public static Result solve(Graph g) {
        return solve(g, 100.0, 0.95, 100, 0.01, 0);
    }
}
