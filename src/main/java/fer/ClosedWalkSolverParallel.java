package fer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import utils.*;
public class ClosedWalkSolverParallel {

    public static Result solve(Graph g) {
        final int n = g.n;
        final double[][] minDistances = g.min_distances;
        
        if (n == 0) return new Result(0.0, new ArrayList<>());
        if (n == 1) {
            List<Integer> seq = new ArrayList<>();
            seq.add(0);
            seq.add(0);
            return new Result(0.0, seq);
        }

        final int subsetCount = 1 << n;
        final double INFINITY = Double.MAX_VALUE / 4;

        // dp[mask][j]
        double[][] dp = new double[subsetCount][n];
        // parents[mask][j]
        int[][] parents = new int[subsetCount][n];

        // Inicijalizacija
        // Možemo koristiti parallel stream za inicijalizaciju ako je n velik, ali Arrays.fill je brz
        for (int i = 0; i < subsetCount; i++) {
            Arrays.fill(dp[i], INFINITY);
            Arrays.fill(parents[i], -1);
        }

        // Bazni slučaj
        dp[1][0] = 0;

        // 1. Grupiranje maski po broju bitova (slojevi)
        // Ovo omogućuje paralelizaciju jer sloj K ovisi samo o sloju K-1
        List<Integer>[] layers = new List[n + 1];
        for (int i = 0; i <= n; i++) {
            layers[i] = new ArrayList<>();
        }

        // O(2^N) - brzo prolazimo i sortiramo maske u "kantice"
        for (int mask = 1; mask < subsetCount; mask++) {
            if ((mask & 1) != 0) { // Samo maske koje sadrže početni vrh 0
                layers[Integer.bitCount(mask)].add(mask);
            }
        }

        // 2. Procesiranje po slojevima
        // Krećemo od r=2 jer r=1 je samo {0} što je bazni slučaj
        for (int r = 2; r <= n; r++) {
            final List<Integer> currentLayer = layers[r];

            // PARALELIZACIJA: Svaka maska u ovom sloju se može računati nezavisno
            currentLayer.parallelStream().forEach(mask -> {
                
                for (int j = 1; j < n; j++) {
                    if ((mask & (1 << j)) == 0) continue;

                    int previousMask = mask ^ (1 << j);
                    
                    // Tražimo najbolji prethodni vrh k
                    for (int k = 0; k < n; k++) {
                        if ((previousMask & (1 << k)) > 0) {
                            double cost = dp[previousMask][k] + minDistances[k][j];
                            
                            // Nema race condition-a jer samo ova dretva piše u dp[mask][j]
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
        
        // Ovo je mali loop (N iteracija), ne treba paralelizam
        double minimumCost = INFINITY;
        int lastCity = 0;
        
        for (int j = 1; j < n; j++) {
            double cost = dp[fullMask][j] + minDistances[j][0];
            if (cost < minimumCost) {
                minimumCost = cost;
                lastCity = j;
            }
        }

        if (minimumCost >= INFINITY) {
            return new Result(Double.POSITIVE_INFINITY, new ArrayList<>());
        }

        // Rekonstrukcija puta
        List<Integer> sequence = new ArrayList<>(n + 1);
        int tourMask = fullMask;
        int currentCity = lastCity;
        
        while (currentCity != 0) {
            sequence.add(currentCity);
            int parent = parents[tourMask][currentCity];
            tourMask ^= (1 << currentCity);
            currentCity = parent;
        }

        sequence.add(0);
        java.util.Collections.reverse(sequence);
        sequence.add(0);

        return new Result(minimumCost, sequence);
    }
}
