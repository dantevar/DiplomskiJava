package fer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TSPSolver {

    public static class Result {
        public final double cost;
        public final List<Integer> tour; // uključuje povratak na 0
        public Result(double cost, List<Integer> tour) {
            this.cost = cost;
            this.tour = tour;
        }
    }

    /**
     * Held-Karp bitmask DP for TSP (start at 0).
     * dist: n x n matrix of non-negative doubles (use Double.POSITIVE_INFINITY if no edge)
     */
    public static Result solve(double[][] dist) {
        int n = dist.length;
        if (n == 0) return new Result(0.0, new ArrayList<>());
        if (n == 1) {
            List<Integer> tour = new ArrayList<>();
            tour.add(0);
            tour.add(0);
            return new Result(0.0, tour);
        }

        int ALL = 1 << n;
        double INF = Double.POSITIVE_INFINITY;

        // dp[mask][j] = minimal cost to start at 0, visit set mask (mask includes 0), and end at j
        double[][] dp = new double[ALL][n];
        int[][] parent = new int[ALL][n]; // to reconstruct path: parent[mask][j] = previous node i
        for (int m = 0; m < ALL; m++) Arrays.fill(dp[m], INF);
        for (int m = 0; m < ALL; m++) Arrays.fill(parent[m], -1);

        // base: mask with only 0 visited -> cost 0 at node 0
        dp[1 << 0][0] = 0.0;

        // iterate masks that include 0
        for (int mask = 0; mask < ALL; mask++) {
            if ((mask & 1) == 0) continue; // must include start 0

            for (int j = 0; j < n; j++) {
                if ((mask & (1 << j)) == 0) continue; // j not in mask
                if (dp[mask][j] == INF) continue;

                // try to go to k not yet visited
                int remaining = (~mask) & (ALL - 1); // nodes not in mask (only lower n bits)
                for (int k = 0; k < n; k++) {
                    if ((mask & (1 << k)) != 0) continue; // already visited
                    double d = dist[j][k];
                    if (Double.isInfinite(d)) continue; // no edge
                    int nmask = mask | (1 << k);
                    double cand = dp[mask][j] + d;
                    if (cand < dp[nmask][k]) {
                        dp[nmask][k] = cand;
                        parent[nmask][k] = j;
                    }
                }
            }
        }

        // complete tour: all nodes visited, return to 0
        int fullMask = (1 << n) - 1;
        double best = INF;
        int lastBest = -1;
        for (int j = 1; j < n; j++) { // last node before returning to 0 (can't be 0)
            if (dp[fullMask][j] == INF) continue;
            double cand = dp[fullMask][j] + dist[j][0];
            if (cand < best) {
                best = cand;
                lastBest = j;
            }
        }

        if (Double.isInfinite(best)) {
            // no feasible Hamiltonian cycle
            return new Result(Double.POSITIVE_INFINITY, new ArrayList<>());
        }

        // reconstruct tour
        List<Integer> tourRev = new ArrayList<>();
        int mask = fullMask;
        int cur = lastBest;
        // add lastBest, then backtrack
        while (cur != -1) {
            tourRev.add(cur);
            int prev = parent[mask][cur];
            mask = mask ^ (1 << cur);
            cur = prev;
            if (mask == (1 << 0) && cur == 0) { // we've reached start
                tourRev.add(0);
                break;
            }
        }
        // ensure start at 0 at beginning
        if (tourRev.size() == 0 || tourRev.get(tourRev.size()-1) != 0) {
            tourRev.add(0);
        }

        // reverse and add final return to 0
        List<Integer> tour = new ArrayList<>();
        for (int i = tourRev.size() - 1; i >= 0; i--) tour.add(tourRev.get(i));
        // ensure ends with 0 (return)
        if (tour.get(tour.size()-1) != 0) tour.add(0);

        return new Result(best, tour);
    }

    // example usage
    public static void main(String[] args) {
        double[][] dist = {
            {0, 1, 1, 1},
            {1, 0, 1, 3},
            {1, 1, 0, 3},
            {1, 3, 3, 0}
        };

        Result r = solve(dist);
        System.out.println("Best cost: " + r.cost);
        System.out.println("Tour: " + r.tour);
    }
}
