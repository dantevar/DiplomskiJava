package heuristika;

import java.util.ArrayList;
import java.util.List;

import fer.Graph;

public class Greedy {

    public static class Result {
        public final double cost;
        public final List<Integer> tour;

        public Result(double cost, List<Integer> tour) {
            this.cost = cost;
            this.tour = tour;
        }
    }

    /**
     * Classic Nearest Neighbor TSP heuristic.
     * Starts at node 0, always visits the nearest unvisited neighbor.
     * Finally returns to 0.
     * 
     * The cost is calculated using the shortest paths (min_distances) 
     * between the chosen vertices in the permutation.
     */
    public static Result solve(Graph g) {
        int n = g.n;
        if (n == 0) return new Result(0, new ArrayList<>());
        
        List<Integer> tour = new ArrayList<>();
        boolean[] visited = new boolean[n];
        
        int current = 0;
        tour.add(current);
        visited[current] = true;
        
        // Greedy Nearest Neighbor construction
        for (int i = 0; i < n - 1; i++) {
            int nextNode = -1;
            double minDist = Double.MAX_VALUE;
            
            for (int j = 0; j < n; j++) {
                if (!visited[j]) {
                    // Using direct distance for the "Classic TSP Greedy" decision
                    double d = g.distance_matrix[current][j];
                    if (d < minDist) {
                        minDist = d;
                        nextNode = j;
                    }
                }
            }
            
            if (nextNode != -1) {
                visited[nextNode] = true;
                tour.add(nextNode);
                current = nextNode;
            }
        }
        
        // Return to start
        tour.add(0);
        
        // Calculate cost using min_distances (shortest paths)
        double totalCost = 0;
        for (int i = 0; i < tour.size() - 1; i++) {
            int u = tour.get(i);
            int v = tour.get(i+1);
            totalCost += g.min_distances[u][v];
        }
        
        return new Result(totalCost, tour);
    }
}
