package fer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import utils.*;
public class TSPSolver {



    /**
     * Held-Karp bitmask DP for TSP (start at 0).
     * dist: n x n matrix of non-negative doubles (use Double.POSITIVE_INFINITY if no edge)
     */
    public static Result solve(double[][] distances) {
        final int n = distances.length;
        if (n == 0) return new Result(0.0, new ArrayList<>());
        if (n == 1) {
            List<Integer> tour = new ArrayList<>();
            tour.add(0);
            tour.add(0);
            return new Result(0.0, tour);
        }

        final int subsetCount = 1 << n;
        final double INFINITY = Double.MAX_VALUE / 4;

        // dp[mask][j] = minimum cost to start at 0, visit exactly the cities in the mask, and end at city j
        double[][] dp = new double[subsetCount][n];
        for (int i = 0; i < subsetCount; i++) {
            Arrays.fill(dp[i], INFINITY);
        }

        // parents[mask][j] = best predecessor of j in the optimal path from mask to j
        int[][] parents = new int[subsetCount][n];
        for (int i = 0; i < subsetCount; i++) {
            Arrays.fill(parents[i], -1);
        }

        dp[1][0] = 0; // Set the base case which is mask = 1 << 0, at city 0, with cost = 0

        // Build up dp table
        for (int mask = 1; mask < subsetCount; mask++) {
            if ((mask & 1) == 0) {
                continue; // City 0 is always included in the tour
            }

            for (int j = 1; j < n; j++) {
                if ((mask & (1 << j)) == 0) {
                    continue; // City j is not in this subset
                }

                final int previousMask = mask ^ (1 << j);
                // Attempt to travel to city j from city k in the previousMask
                for (int k = 0; k < n; k++) {
                    if ((previousMask & (1 << k)) > 0) {
                        final double cost = dp[previousMask][k] + distances[k][j];
                        if (cost < dp[mask][j]) {
                            dp[mask][j] = cost;
                            parents[mask][j] = k;
                        }
                    }
                }
            }
        }

        // Complete the tour by returning to city 0
        final int fullMask = subsetCount - 1;
        double minimumCost = INFINITY;
        int lastCity = 0;
        for (int j = 1; j < n; j++) {
            final double cost = dp[fullMask][j] + distances[j][0];
            if (cost < minimumCost) {
                minimumCost = cost;
                lastCity = j;
            }
        }

        if (minimumCost >= INFINITY) {
            // no feasible Hamiltonian cycle
            return new Result(Double.POSITIVE_INFINITY, new ArrayList<>());
        }

        // Construct the optimal tour
        List<Integer> tour = new ArrayList<>(n + 1);
        int tourMask = fullMask;
        int currentCity = lastCity;
        // Backtrack until city 0 is reached
        while (currentCity != 0) {
            tour.add(currentCity);
            final int parent = parents[tourMask][currentCity];
            tourMask ^= (1 << currentCity);
            currentCity = parent;
        }

        tour.add(0); // Include the start city 0
        java.util.Collections.reverse(tour);
        tour.add(0); // Return to the start city 0

        return new Result(minimumCost, tour);
    }


}
