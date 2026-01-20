package heuristika;

import java.util.*;
import utils.*;

/**
 * Adaptive Shortest Path Walker (ASPW)
 * 
 * Heuristika specifično dizajnirana za MCW problem.
 * Razlika od TSP algoritama: eksplicitno koristi shortest paths i dozvoljava
 * ponavljanje čvorova kao "bridges".
 * 
 * Faze:
 * 1. Greedy Coverage - pokrij sve čvorove najkraćim putevima
 * 2. Closing - zatvori walk natrag na 0
 * 3. Local Optimization - skrati nepotrebne petlje
 * 
 * Vremenska složenost: O(n³) (dominantno Floyd-Warshall)
 * 
 * Napomena: Ovo NIJE egzaktni algoritam - daje približno rješenje.
 */
public class ASPW {
    
    /**
     * Solve MCW with default alpha=0.4
     */
    public static Result solve(Graph g) {
        return solve(g, 0.4);
    }
    
    /**
     * Solve MCW with adaptive shortest path walking
     * 
     * @param g Graph instance
     * @param alpha Balance factor: 0=pure greedy, 1=strategic hub selection
     * @return Approximate solution
     */
    public static Result solve(Graph g, double alpha) {
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
            int next = selectNext(current, uncovered, minDist, alpha);
            
            // Reconstruct shortest path current→next and add to walk
            List<Integer> path = reconstructPath(current, next, g);
            
            // Add path (skip first node - already in walk)
            for (int i = 1; i < path.size(); i++) {
                walk.add(path.get(i));
            }
            
            uncovered.remove(next);
        }
        
        // Phase 2: Closing
        int last = walk.get(walk.size() - 1);
        if (last != 0) {
            List<Integer> closingPath = reconstructPath(last, 0, g);
            for (int i = 1; i < closingPath.size(); i++) {
                walk.add(closingPath.get(i));
            }
        }
        
        // Phase 3: Local Optimization
        walk = localOptimization(walk, g);
        
        double cost = evaluateWalk(walk, g.distance_matrix);
        
        return new Result(cost, walk);
    }
    
    /**
     * Select next node to cover based on distance and centrality
     * 
     * @param current Current position
     * @param uncovered Remaining nodes to cover
     * @param minDist Shortest distances matrix
     * @param alpha Balance factor
     * @return Best next node
     */
    private static int selectNext(int current, Set<Integer> uncovered, 
                                   double[][] minDist, double alpha) {
        int best = -1;
        double bestScore = Double.MAX_VALUE;
        
        for (int candidate : uncovered) {
            // Distance to reach candidate
            double dist = minDist[current][candidate];
            
            // Average distance from candidate to remaining nodes (centrality)
            double avgRemaining = avgDistanceToRemaining(candidate, uncovered, minDist);
            
            // Combined score: immediate cost + future cost estimate
            double score = dist + alpha * avgRemaining;
            
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
     * Reconstruct shortest path from start to end
     * 
     * Koristi Floyd-Warshall next-hop matricu za rekonstrukciju.
     * nextHops.get(u) daje array mogućih first hops iz u.
     * Moramo rekurzivno tražiti put do destinacije.
     * 
     * @param start Starting node
     * @param end Target node
     * @param g Graph with min_distances and distance_matrix
     * @return Path as list of nodes
     */
    private static List<Integer> reconstructPath(int start, int end, Graph g) {
        if (start == end) {
            return Arrays.asList(start);
        }
        
        // Koristi BFS za rekonstrukciju puta
        // Traži put gdje je udaljenost = min_distance
        int n = g.n;
        double[][] minDist = g.min_distances;
        double[][] directDist = g.distance_matrix;
        
        // BFS za pronalaženje shortest path
        Queue<List<Integer>> queue = new LinkedList<>();
        queue.add(Arrays.asList(start));
        
        while (!queue.isEmpty()) {
            List<Integer> currentPath = queue.poll();
            int current = currentPath.get(currentPath.size() - 1);
            
            if (current == end) {
                return currentPath;
            }
            
            // Provjeri moguće next hops
            for (int next = 0; next < n; next++) {
                if (next == current || currentPath.contains(next)) continue;
                
                // Provjeri da li next vodi prema end s optimalnom udaljenošću
                double pathSoFar = 0.0;
                for (int i = 0; i < currentPath.size() - 1; i++) {
                    pathSoFar += directDist[currentPath.get(i)][currentPath.get(i + 1)];
                }
                pathSoFar += directDist[current][next];
                
                double remainingOptimal = minDist[next][end];
                double totalExpected = minDist[start][end];
                
                // Ako je put + remaining = total optimal, nastavi
                if (Math.abs(pathSoFar + remainingOptimal - totalExpected) < 1e-9) {
                    List<Integer> newPath = new ArrayList<>(currentPath);
                    newPath.add(next);
                    queue.add(newPath);
                }
            }
        }
        
        // Fallback: direktan edge ako BFS ne uspije
        return Arrays.asList(start, end);
    }
    
    /**
     * Local optimization phase: remove redundant loops and optimize segments
     * 
     * @param walk Current walk
     * @param g Graph
     * @return Optimized walk
     */
    private static List<Integer> localOptimization(List<Integer> walk, Graph g) {
        List<Integer> optimized = new ArrayList<>(walk);
        boolean improved = true;
        int iterations = 0;
        int maxIterations = 10;
        
        while (improved && iterations < maxIterations) {
            improved = false;
            iterations++;
            
            // Operator 1: Remove redundant segments
            // If segment a→...→b can be replaced with direct path a→b
            for (int i = 0; i < optimized.size() - 3; i++) {
                for (int j = i + 3; j < optimized.size(); j++) {
                    int a = optimized.get(i);
                    int b = optimized.get(j);
                    
                    // Check if all nodes in segment are still covered elsewhere
                    Set<Integer> segmentNodes = new HashSet<>();
                    for (int k = i + 1; k < j; k++) {
                        segmentNodes.add(optimized.get(k));
                    }
                    
                    // Check if these nodes appear elsewhere in walk
                    boolean allCoveredElsewhere = true;
                    for (int node : segmentNodes) {
                        boolean foundElsewhere = false;
                        for (int k = 0; k < optimized.size(); k++) {
                            if ((k < i || k > j) && optimized.get(k) == node) {
                                foundElsewhere = true;
                                break;
                            }
                        }
                        if (!foundElsewhere) {
                            allCoveredElsewhere = false;
                            break;
                        }
                    }
                    
                    if (allCoveredElsewhere) {
                        // Try replacing segment with shortest path
                        List<Integer> shortcut = reconstructPath(a, b, g);
                        
                        // Calculate costs
                        double oldCost = segmentCost(optimized, i, j, g.distance_matrix);
                        double newCost = pathCost(shortcut, g.distance_matrix);
                        
                        if (newCost < oldCost) {
                            // Replace segment
                            List<Integer> newWalk = new ArrayList<>();
                            for (int k = 0; k <= i; k++) {
                                newWalk.add(optimized.get(k));
                            }
                            for (int k = 1; k < shortcut.size(); k++) {
                                newWalk.add(shortcut.get(k));
                            }
                            for (int k = j + 1; k < optimized.size(); k++) {
                                newWalk.add(optimized.get(k));
                            }
                            
                            optimized = newWalk;
                            improved = true;
                            break;
                        }
                    }
                }
                if (improved) break;
            }
        }
        
        return optimized;
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
