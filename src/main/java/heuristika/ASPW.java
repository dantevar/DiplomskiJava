package heuristika;

import java.util.*;
import utils.*;

/**
 * Adaptive Shortest Path Walker (ASPW) - POBOLJŠANA VERZIJA
 * 
 * Heuristika specifično dizajnirana za MCW problem.
 * Razlika od TSP algoritama: eksplicitno koristi shortest paths i dozvoljava
 * ponavljanje čvorova kao "bridges".
 * 
 * Faze:
 * 1. Multi-Start Greedy Coverage - pokrij sve čvorove (više strategija)
 * 2. Closing - zatvori walk natrag na 0
 * 3. Advanced Local Optimization - 2-opt, segment removal, shortcut insertion
 * 
 * POBOLJŠANJA V2:
 * - Multi-start: isprobaj više strategija odabira
 * - Bolja selekcija: uključuje povratak na 0 u procjenu
 * - Jača lokalna optimizacija: 2-opt + shortcut + node removal
 * - Or-opt: premještanje segmenata
 * 
 * Vremenska složenost: O(n³) 
 */
public class ASPW {
    
    /**
     * Solve MCW with default alpha=0.3
     */
    public static Result solve(Graph g) {
        return solveMultiStart(g);
    }
    
    /**
     * Multi-start verzija - isprobaj više alpha vrijednosti i strategija
     */
    public static Result solveMultiStart(Graph g) {
        double[] alphas = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5};
        
        Result best = null;
        
        for (double alpha : alphas) {
            Result r = solveSingleStart(g, alpha, false);
            if (best == null || r.cost < best.cost) {
                best = r;
            }
            
            // Probaj i s "return cost" heuristikom
            Result r2 = solveSingleStart(g, alpha, true);
            if (r2.cost < best.cost) {
                best = r2;
            }
        }
        
        // Dodatno: nearest neighbor kao početak
        Result greedyStart = solveFromGreedy(g);
        if (greedyStart.cost < best.cost) {
            best = greedyStart;
        }
        
        return best;
    }
    
    /**
     * Solve MCW with adaptive shortest path walking
     * 
     * @param g Graph instance
     * @param alpha Balance factor: 0=pure greedy, 1=strategic hub selection
     * @return Approximate solution
     */
    public static Result solve(Graph g, double alpha) {
        return solveSingleStart(g, alpha, false);
    }
    
    private static Result solveSingleStart(Graph g, double alpha, boolean considerReturn) {
        int n = g.n;
        double[][] minDist = g.min_distances;
        
        if (n <= 1) return new Result(0, Arrays.asList(0));
        
        // Phase 1: Greedy Coverage
        List<Integer> walk = new ArrayList<>();
        walk.add(0);
        
        Set<Integer> uncovered = new HashSet<>();
        for (int i = 1; i < n; i++) {
            uncovered.add(i);
        }
        
        while (!uncovered.isEmpty()) {
            int current = walk.get(walk.size() - 1);
            
            // Select next node to cover
            int next = selectNext(current, uncovered, minDist, alpha, considerReturn);
            
            // Reconstruct shortest path current→next and add to walk
            List<Integer> path = reconstructPath(current, next, g);
            
            // Add path (skip first node - already in walk)
            for (int i = 1; i < path.size(); i++) {
                walk.add(path.get(i));
            }
            
            // Ukloni SVE čvorove na putu iz uncovered (ne samo destinaciju!)
            for (int node : path) {
                uncovered.remove(node);
            }
        }
        
        // Phase 2: Closing
        int last = walk.get(walk.size() - 1);
        if (last != 0) {
            List<Integer> closingPath = reconstructPath(last, 0, g);
            for (int i = 1; i < closingPath.size(); i++) {
                walk.add(closingPath.get(i));
            }
        }
        
        // Phase 3: Advanced Local Optimization
        walk = advancedLocalOptimization(walk, g);
        
        double cost = evaluateWalk(walk, g.distance_matrix);
        
        return new Result(cost, walk);
    }
    
    /**
     * Počni od greedy nearest neighbor i optimiziraj
     */
    private static Result solveFromGreedy(Graph g) {
        int n = g.n;
        double[][] minDist = g.min_distances;
        
        List<Integer> walk = new ArrayList<>();
        walk.add(0);
        
        Set<Integer> uncovered = new HashSet<>();
        for (int i = 1; i < n; i++) {
            uncovered.add(i);
        }
        
        // Pure nearest neighbor
        while (!uncovered.isEmpty()) {
            int current = walk.get(walk.size() - 1);
            
            int nearest = -1;
            double nearestDist = Double.MAX_VALUE;
            for (int candidate : uncovered) {
                if (minDist[current][candidate] < nearestDist) {
                    nearestDist = minDist[current][candidate];
                    nearest = candidate;
                }
            }
            
            // Dodaj shortest path
            List<Integer> path = reconstructPath(current, nearest, g);
            for (int i = 1; i < path.size(); i++) {
                walk.add(path.get(i));
            }
            
            for (int node : path) {
                uncovered.remove(node);
            }
        }
        
        // Close
        int last = walk.get(walk.size() - 1);
        if (last != 0) {
            List<Integer> closingPath = reconstructPath(last, 0, g);
            for (int i = 1; i < closingPath.size(); i++) {
                walk.add(closingPath.get(i));
            }
        }
        
        walk = advancedLocalOptimization(walk, g);
        double cost = evaluateWalk(walk, g.distance_matrix);
        
        return new Result(cost, walk);
    }
    
    /**
     * Select next node to cover based on distance, centrality, and return cost
     */
    private static int selectNext(int current, Set<Integer> uncovered, 
                                   double[][] minDist, double alpha, boolean considerReturn) {
        int best = -1;
        double bestScore = Double.MAX_VALUE;
        
        for (int candidate : uncovered) {
            // Distance to reach candidate
            double dist = minDist[current][candidate];
            
            // Average distance from candidate to remaining nodes (centrality)
            double avgRemaining = avgDistanceToRemaining(candidate, uncovered, minDist);
            
            // Return cost to 0 (važno za završetak!)
            double returnCost = considerReturn ? minDist[candidate][0] * 0.3 : 0;
            
            // Combined score
            double score = dist + alpha * avgRemaining + returnCost;
            
            if (score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        
        return best;
    }
    
    /**
     * Compute average distance from node to remaining uncovered nodes
     */
    private static double avgDistanceToRemaining(int node, Set<Integer> uncovered, 
                                                  double[][] minDist) {
        if (uncovered.size() <= 1) return 0.0;
        
        double sum = 0.0;
        int count = 0;
        
        for (int other : uncovered) {
            if (other != node) {
                sum += minDist[node][other];
                count++;
            }
        }
        
        return count > 0 ? sum / count : 0.0;
    }
    
    /**
     * Reconstruct shortest path from start to end using predecessor info
     */
    private static List<Integer> reconstructPath(int start, int end, Graph g) {
        if (start == end) {
            return Arrays.asList(start);
        }
        
        int n = g.n;
        double[][] minDist = g.min_distances;
        double[][] directDist = g.distance_matrix;
        
        // Jednostavnija rekonstrukcija - traži greedy put
        List<Integer> path = new ArrayList<>();
        path.add(start);
        
        int current = start;
        int maxSteps = n * 2; // Zaštita od beskonačne petlje
        int steps = 0;
        
        while (current != end && steps < maxSteps) {
            steps++;
            int bestNext = -1;
            double bestProgress = Double.MAX_VALUE;
            
            for (int next = 0; next < n; next++) {
                if (next == current) continue;
                
                // Biramo čvor koji nas vodi bliže cilju
                double progressScore = directDist[current][next] + minDist[next][end];
                
                // Provjeri da je to dio optimalnog puta
                if (Math.abs(directDist[current][next] + minDist[next][end] - minDist[current][end]) < 1e-9) {
                    if (progressScore < bestProgress) {
                        bestProgress = progressScore;
                        bestNext = next;
                    }
                }
            }
            
            if (bestNext == -1) {
                // Fallback: direktan skok
                path.add(end);
                break;
            }
            
            path.add(bestNext);
            current = bestNext;
        }
        
        return path;
    }
    
    /**
     * Advanced local optimization with multiple operators
     */
    private static List<Integer> advancedLocalOptimization(List<Integer> walk, Graph g) {
        List<Integer> optimized = new ArrayList<>(walk);
        int n = g.n;
        boolean improved = true;
        int iterations = 0;
        int maxIterations = 50;  // Više iteracija
        
        while (improved && iterations < maxIterations) {
            improved = false;
            iterations++;
            
            double currentCost = evaluateWalk(optimized, g.distance_matrix);
            
            // Operator 1: 2-opt za MCW (swap segmenata)
            List<Integer> after2opt = try2Opt(optimized, g, currentCost);
            if (after2opt != null) {
                optimized = after2opt;
                improved = true;
                continue;
            }
            
            // Operator 2: Shortcut - zamijeni segment s kraćim putem
            List<Integer> afterShortcut = tryShortcut(optimized, g, currentCost);
            if (afterShortcut != null) {
                optimized = afterShortcut;
                improved = true;
                continue;
            }
            
            // Operator 3: Node removal - ukloni nepotrebne čvorove
            List<Integer> afterRemoval = tryNodeRemoval(optimized, g, n, currentCost);
            if (afterRemoval != null) {
                optimized = afterRemoval;
                improved = true;
                continue;
            }
        }
        
        return optimized;
    }
    
    /**
     * 2-opt: pokušaj preokrenuti segment
     */
    private static List<Integer> try2Opt(List<Integer> walk, Graph g, double currentCost) {
        int len = walk.size();
        double[][] dist = g.distance_matrix;
        
        for (int i = 0; i < len - 2; i++) {
            for (int j = i + 2; j < len - 1; j++) {
                // Provjeri sve čvorove u segmentu - moraju biti pokriveni
                
                // Izračunaj ušteku od 2-opt
                int a = walk.get(i);
                int b = walk.get(i + 1);
                int c = walk.get(j);
                int d = walk.get(j + 1);
                
                double oldEdges = dist[a][b] + dist[c][d];
                double newEdges = dist[a][c] + dist[b][d];
                
                if (newEdges < oldEdges - 1e-9) {
                    // Preokreni segment [i+1, j]
                    List<Integer> newWalk = new ArrayList<>();
                    for (int k = 0; k <= i; k++) {
                        newWalk.add(walk.get(k));
                    }
                    for (int k = j; k >= i + 1; k--) {
                        newWalk.add(walk.get(k));
                    }
                    for (int k = j + 1; k < len; k++) {
                        newWalk.add(walk.get(k));
                    }
                    
                    // Provjeri da je valid walk
                    if (isValidWalk(newWalk, g.n)) {
                        double newCost = evaluateWalk(newWalk, dist);
                        if (newCost < currentCost - 1e-9) {
                            return newWalk;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Shortcut: zamijeni segment s kraćim putem
     */
    private static List<Integer> tryShortcut(List<Integer> walk, Graph g, double currentCost) {
        double[][] dist = g.distance_matrix;
        
        for (int i = 0; i < walk.size() - 3; i++) {
            for (int j = i + 3; j < walk.size(); j++) {
                int a = walk.get(i);
                int b = walk.get(j);
                
                // Provjeri jesu li svi čvorovi u segmentu pokriveni drugdje
                Set<Integer> segmentNodes = new HashSet<>();
                for (int k = i + 1; k < j; k++) {
                    segmentNodes.add(walk.get(k));
                }
                
                boolean allCoveredElsewhere = true;
                for (int node : segmentNodes) {
                    boolean found = false;
                    for (int k = 0; k < walk.size(); k++) {
                        if ((k <= i || k >= j) && walk.get(k) == node) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        allCoveredElsewhere = false;
                        break;
                    }
                }
                
                if (allCoveredElsewhere) {
                    // Zamijeni s shortest path
                    List<Integer> shortcut = reconstructPath(a, b, g);
                    
                    double oldCost = segmentCost(walk, i, j, dist);
                    double newCost = pathCost(shortcut, dist);
                    
                    if (newCost < oldCost - 1e-9) {
                        List<Integer> newWalk = new ArrayList<>();
                        for (int k = 0; k <= i; k++) {
                            newWalk.add(walk.get(k));
                        }
                        for (int k = 1; k < shortcut.size(); k++) {
                            newWalk.add(shortcut.get(k));
                        }
                        for (int k = j + 1; k < walk.size(); k++) {
                            newWalk.add(walk.get(k));
                        }
                        
                        if (isValidWalk(newWalk, g.n)) {
                            double totalNewCost = evaluateWalk(newWalk, dist);
                            if (totalNewCost < currentCost - 1e-9) {
                                return newWalk;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Node removal: ukloni čvor ako je pokriven drugdje
     */
    private static List<Integer> tryNodeRemoval(List<Integer> walk, Graph g, int n, double currentCost) {
        double[][] dist = g.distance_matrix;
        
        for (int i = 1; i < walk.size() - 1; i++) {
            int node = walk.get(i);
            
            // Provjeri pojavljuje li se čvor drugdje
            boolean appearsElsewhere = false;
            for (int j = 0; j < walk.size(); j++) {
                if (j != i && walk.get(j) == node) {
                    appearsElsewhere = true;
                    break;
                }
            }
            
            if (appearsElsewhere) {
                int prev = walk.get(i - 1);
                int next = walk.get(i + 1);
                
                // Pokušaj ukloniti i spojiti s shortest path
                List<Integer> bridge = reconstructPath(prev, next, g);
                
                double oldCost = dist[prev][node] + dist[node][next];
                double newCost = pathCost(bridge, dist);
                
                if (newCost < oldCost - 1e-9) {
                    List<Integer> newWalk = new ArrayList<>();
                    for (int k = 0; k < i; k++) {
                        newWalk.add(walk.get(k));
                    }
                    for (int k = 1; k < bridge.size(); k++) {
                        newWalk.add(bridge.get(k));
                    }
                    for (int k = i + 2; k < walk.size(); k++) {
                        newWalk.add(walk.get(k));
                    }
                    
                    if (isValidWalk(newWalk, n)) {
                        double totalNewCost = evaluateWalk(newWalk, dist);
                        if (totalNewCost < currentCost - 1e-9) {
                            return newWalk;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Provjeri je li walk validan (posjećuje sve čvorove)
     */
    private static boolean isValidWalk(List<Integer> walk, int n) {
        Set<Integer> visited = new HashSet<>(walk);
        for (int i = 0; i < n; i++) {
            if (!visited.contains(i)) return false;
        }
        return walk.get(0) == 0; // Mora početi sa 0
    }
    
    /**
     * Calculate cost of a segment in walk
     */
    private static double segmentCost(List<Integer> walk, int start, int end, 
                                      double[][] distances) {
        double cost = 0.0;
        for (int i = start; i < end; i++) {
            cost += distances[walk.get(i)][walk.get(i + 1)];
        }
        return cost;
    }
    
    /**
     * Calculate cost of a path
     */
    private static double pathCost(List<Integer> path, double[][] distances) {
        double cost = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            cost += distances[path.get(i)][path.get(i + 1)];
        }
        return cost;
    }
    
    /**
     * Evaluate total walk cost
     */
    private static double evaluateWalk(List<Integer> walk, double[][] distances) {
        if (walk.size() < 2) return 0.0;
        
        double cost = 0.0;
        for (int i = 0; i < walk.size() - 1; i++) {
            cost += distances[walk.get(i)][walk.get(i + 1)];
        }
        
        // Close the walk (last → first)
        cost += distances[walk.get(walk.size() - 1)][walk.get(0)];
        
        return cost;
    }
}
