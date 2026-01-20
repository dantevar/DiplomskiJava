package metaheuristika;

import java.util.*;
import utils.Graph;
import utils.Result;

/**
 * Ant Colony Optimization (ACO) for TSP using min_distances matrix.
 * 
 * Time Complexity Analysis:
 * - Per iteration: O(m * n^2) where m = number of ants, n = number of vertices
 *   - Each ant constructs tour: O(n^2) for probabilistic selection
 *   - m ants: O(m * n^2)
 * - Total: O(iterations * m * n^2)
 * 
 * Space Complexity: O(n^2) for pheromone matrix
 * 
 * GA vs ACO Complexity:
 * - GA: O(generations * popSize * n) for fitness evaluation + crossover
 * - ACO: O(iterations * ants * n^2) due to probabilistic selection at each step
 * - ACO is typically slower per iteration but often finds better solutions for graph problems
 */
public class ACO {

    private static final Random rand = new Random();

    public static Result solve(Graph g, int numAnts, int iterations, 
                                double alpha, double beta, double evaporation, 
                                double Q, int printEvery) {
        int n = g.n;
        double[][] distances = g.min_distances;
        
        // Pheromone matrix
        double[][] pheromone = new double[n][n];
        double initialPheromone = 1.0;
        for (int i = 0; i < n; i++) {
            Arrays.fill(pheromone[i], initialPheromone);
        }
        
        int[] bestTour = null;
        double bestCost = Double.MAX_VALUE;
        
        // Main ACO loop
        for (int iter = 0; iter < iterations; iter++) {
            if (printEvery > 0 && iter % printEvery == 0) {
                System.out.println("Iteration " + iter + ": Best cost = " + bestCost);
            }
            
            // Deploy ants
            List<int[]> tours = new ArrayList<>();
            List<Double> costs = new ArrayList<>();
            
            for (int ant = 0; ant < numAnts; ant++) {
                int[] tour = constructTour(n, distances, pheromone, alpha, beta);
                double cost = calculateCost(tour, distances);
                
                tours.add(tour);
                costs.add(cost);
                
                if (cost < bestCost) {
                    bestCost = cost;
                    bestTour = tour.clone();
                }
            }
            
            // Evaporate pheromone
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    pheromone[i][j] *= (1 - evaporation);
                }
            }
            
            // Deposit pheromone
            for (int ant = 0; ant < numAnts; ant++) {
                int[] tour = tours.get(ant);
                double cost = costs.get(ant);
                double delta = Q / cost;
                
                for (int i = 0; i < tour.length; i++) {
                    int from = tour[i];
                    int to = tour[(i + 1) % tour.length];
                    pheromone[from][to] += delta;
                    pheromone[to][from] += delta; // Symmetric
                }
            }
        }
        
        return new Result(bestCost, tourToList(bestTour));
    }
    
    // Default parameters
    public static Result solve(Graph g) {
        return solve(g, 20, 100, 1.0, 2.0, 0.5, 100.0, 0);
    }
    
    /**
     * Construct tour using probabilistic state transition rule.
     * Complexity: O(n^2) - for each of n cities, compute probability for all unvisited
     */
    private static int[] constructTour(int n, double[][] dist, double[][] pheromone,
                                       double alpha, double beta) {
        int[] tour = new int[n];
        boolean[] visited = new boolean[n];
        
        // Start at city 0
        int current = 0;
        tour[0] = current;
        visited[current] = true;
        
        // Build tour
        for (int step = 1; step < n; step++) {
            int next = selectNextCity(current, visited, dist, pheromone, alpha, beta);
            tour[step] = next;
            visited[next] = true;
            current = next;
        }
        
        return tour;
    }
    
    /**
     * Select next city using pheromone and heuristic information.
     * Probability: p_ij = (tau_ij^alpha * eta_ij^beta) / sum(...)
     * where tau = pheromone, eta = 1/distance (heuristic desirability)
     */
    private static int selectNextCity(int current, boolean[] visited, double[][] dist,
                                      double[][] pheromone, double alpha, double beta) {
        int n = visited.length;
        double[] probabilities = new double[n];
        double sum = 0.0;
        
        // Calculate probabilities for unvisited cities
        for (int i = 0; i < n; i++) {
            if (!visited[i] && dist[current][i] > 0) {
                double tau = Math.pow(pheromone[current][i], alpha);
                double eta = Math.pow(1.0 / dist[current][i], beta);
                probabilities[i] = tau * eta;
                sum += probabilities[i];
            }
        }
        
        // Roulette wheel selection
        if (sum == 0) {
            // Fallback: select random unvisited
            List<Integer> unvisited = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if (!visited[i]) unvisited.add(i);
            }
            return unvisited.get(rand.nextInt(unvisited.size()));
        }
        
        double r = rand.nextDouble() * sum;
        double cumulative = 0.0;
        
        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                cumulative += probabilities[i];
                if (cumulative >= r) {
                    return i;
                }
            }
        }
        
        // Should not reach here, but fallback
        for (int i = 0; i < n; i++) {
            if (!visited[i]) return i;
        }
        return 0;
    }
    
    private static double calculateCost(int[] tour, double[][] dist) {
        double cost = 0;
        for (int i = 0; i < tour.length; i++) {
            int from = tour[i];
            int to = tour[(i + 1) % tour.length];
            cost += dist[from][to];
        }
        return cost;
    }
    
    private static List<Integer> tourToList(int[] tour) {
        List<Integer> list = new ArrayList<>();
        for (int v : tour) list.add(v);
        list.add(tour[0]); // Close the loop
        return list;
    }
}
