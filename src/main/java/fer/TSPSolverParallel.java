package fer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TSPSolverParallel {

    public static class Result {
        public final double cost;
        public final List<Integer> tour; // uključuje povratak na 0
        public Result(double cost, List<Integer> tour) {
            this.cost = cost;
            this.tour = tour;
        }
    }

    /**
     * Parallel Held-Karp bitmask DP for TSP (start at 0).
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

        // dp[mask][j]
        double[][] dp = new double[subsetCount][n];
        // parents[mask][j]
        int[][] parents = new int[subsetCount][n];

        // Inicijalizacija
        for (int i = 0; i < subsetCount; i++) {
            Arrays.fill(dp[i], INFINITY);
            Arrays.fill(parents[i], -1);
        }

        dp[1][0] = 0;

        // 1. Grupiranje maski po broju bitova (slojevi)
        List<Integer>[] layers = new List[n + 1];
        for (int i = 0; i <= n; i++) {
            layers[i] = new ArrayList<>();
        }

        for (int mask = 1; mask < subsetCount; mask++) {
            if ((mask & 1) != 0) { // Samo maske koje sadrže početni vrh 0
                layers[Integer.bitCount(mask)].add(mask);
            }
        }

        // 2. Procesiranje po slojevima
        for (int r = 2; r <= n; r++) {
            final List<Integer> currentLayer = layers[r];

            // PARALELIZACIJA
            currentLayer.parallelStream().forEach(mask -> {
                
                for (int j = 1; j < n; j++) {
                    if ((mask & (1 << j)) == 0) continue;

                    int previousMask = mask ^ (1 << j);
                    
                    for (int k = 0; k < n; k++) {
                        if ((previousMask & (1 << k)) > 0) {
                            double cost = dp[previousMask][k] + distances[k][j];
                            
                            if (cost < dp[mask][j]) {
                                dp[mask][j] = cost;
                                parents[mask][j] = k;
                            }
                        }
                    }
                }
            });
        }

        // Završni korak: povratak na 0
        final int fullMask = subsetCount - 1;
        double minimumCost = INFINITY;
        int lastCity = 0;
        
        for (int j = 1; j < n; j++) {
            double cost = dp[fullMask][j] + distances[j][0];
            if (cost < minimumCost) {
                minimumCost = cost;
                lastCity = j;
            }
        }

        if (minimumCost >= INFINITY) {
            return new Result(Double.POSITIVE_INFINITY, new ArrayList<>());
        }

        // Rekonstrukcija puta
        List<Integer> tour = new ArrayList<>(n + 1);
        int tourMask = fullMask;
        int currentCity = lastCity;
        
        while (currentCity != 0) {
            tour.add(currentCity);
            int parent = parents[tourMask][currentCity];
            tourMask ^= (1 << currentCity);
            currentCity = parent;
        }

        tour.add(0);
        java.util.Collections.reverse(tour);
        tour.add(0);

        return new Result(minimumCost, tour);
    }
}
