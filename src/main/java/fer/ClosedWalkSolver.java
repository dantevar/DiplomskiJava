package fer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import utils.*;
/**
 * Held-Karp Dynamic Programming za problem zatvorene šetnje.
 * Koristi min_distances (najkraće puteve) umjesto direktnih bridova.
 */
public class ClosedWalkSolver {

    public static class Result {
        public final double cost;
        public final List<Integer> sequence; // sekvenca posjeta (može imati duplikate u stvarnoj šetnji)
        
        public Result(double cost, List<Integer> sequence) {
            this.cost = cost;
            this.sequence = sequence;
        }
    }

    /**
     * Held-Karp DP za najkraću zatvorenu šetnju koja posjećuje sve vrhove.
     * 
     * @param g Graf s min_distances matricom (najkraći putevi između svih parova)
     * @return Result s cijenom i sekvencom posjeta vrhova
     */
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

        // dp[mask][j] = minimalni trošak da krenem od 0, posjetim točno vrhove u maski, i završim u j
        double[][] dp = new double[subsetCount][n];
        for (int i = 0; i < subsetCount; i++) {
            Arrays.fill(dp[i], INFINITY);
        }

        // parents[mask][j] = prethodni vrh u optimalnoj šetnji do j s maskom mask
        int[][] parents = new int[subsetCount][n];
        for (int i = 0; i < subsetCount; i++) {
            Arrays.fill(parents[i], -1);
        }

        // Bazni slučaj: maska s samo vrhom 0, završavam u 0, trošak = 0
        dp[1][0] = 0;

        // Gradi DP tablicu
        for (int mask = 1; mask < subsetCount; mask++) {
            if ((mask & 1) == 0) {
                continue; // Vrh 0 uvijek mora biti u šetnji
            }

            for (int j = 1; j < n; j++) {
                if ((mask & (1 << j)) == 0) {
                    continue; // Vrh j nije u ovoj maski
                }

                final int previousMask = mask ^ (1 << j);
                
                // Pokušaj doći do vrha j iz vrha k koji je u previousMask
                for (int k = 0; k < n; k++) {
                    if ((previousMask & (1 << k)) > 0) {
                        // KLJUČNA RAZLIKA: koristi min_distances (najkraći put k→j)
                        // što dozvoljava ponavljanje vrhova u stvarnoj šetnji
                        final double cost = dp[previousMask][k] + minDistances[k][j];
                        
                        if (cost < dp[mask][j]) {
                            dp[mask][j] = cost;
                            parents[mask][j] = k;
                        }
                    }
                }
            }
        }

        // Zatvori šetnju vraćanjem na 0
        final int fullMask = subsetCount - 1;
        double minimumCost = INFINITY;
        int lastCity = 0;
        
        for (int j = 1; j < n; j++) {
            final double cost = dp[fullMask][j] + minDistances[j][0];
            if (cost < minimumCost) {
                minimumCost = cost;
                lastCity = j;
            }
        }

        if (minimumCost >= INFINITY) {
            // Nema izvodive zatvorene šetnje
            return new Result(Double.POSITIVE_INFINITY, new ArrayList<>());
        }

        // Rekonstruiraj sekvencu posjeta (obavezni čvorovi, ne punu šetnju!)
        List<Integer> sequence = new ArrayList<>(n + 1);
        int tourMask = fullMask;
        int currentCity = lastCity;
        
        // Backtrack dok ne dođeš do 0
        while (currentCity != 0) {
            sequence.add(currentCity);
            final int parent = parents[tourMask][currentCity];
            tourMask ^= (1 << currentCity);
            currentCity = parent;
        }

        sequence.add(0); // Dodaj start
        java.util.Collections.reverse(sequence);
        sequence.add(0); // Dodaj povratak na 0

        return new Result(minimumCost, sequence);
    }

    /**
     * Rekonstruira punu šetnju (s ponavljanjima) iz sekvence obaveznih posjeta.
     * Za svaki par uzastopnih vrhova u sekvenci, ubaci najkraći put između njih.
     */
    public static List<Integer> reconstructFullWalk(Graph g, List<Integer> sequence) {
        List<Integer> fullWalk = new ArrayList<>();
        
        for (int i = 0; i < sequence.size() - 1; i++) {
            int from = sequence.get(i);
            int to = sequence.get(i + 1);
            
            // Dodaj najkraći put od 'from' do 'to'
            List<Integer> path = reconstructPath(g, from, to);
            
            // Dodaj sve osim zadnjeg (jer će biti dodan kao prvi sljedeći put)
            for (int j = 0; j < path.size() - 1; j++) {
                fullWalk.add(path.get(j));
            }
        }
        
        fullWalk.add(0); // Dodaj finalni povratak na 0
        return fullWalk;
    }

    /**
     * Rekonstruira najkraći put između dva vrha koristeći Floyd-Warshall next matricu.
     * Ako Graph ne čuva 'next' matricu, ova metoda vraća samo [from, to].
     */
    private static List<Integer> reconstructPath(Graph g, int from, int to) {
        // Jednostavna verzija: ako nema next matrice, vrati samo direktan skok
        // U kompletnijoj implementaciji, Graph bi trebao imati next[][] matricu iz Floyd-Warshall
        List<Integer> path = new ArrayList<>();
        path.add(from);
        path.add(to);
        return path;
    }


}
