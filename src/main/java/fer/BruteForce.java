package fer;

import java.util.ArrayList;
import java.util.List;
import utils.*;
public class BruteForce {

    public static double permutations(Graph g){
        int n = g.n;
        
        // Kreiraj listu vrhova bez 0 (jer 0 je fiksiran na početku)
        List<Integer> vertices = new ArrayList<>();
        for (int i = 1; i < n; i++) {
            vertices.add(i);
        }
        
        double[] best = {Double.POSITIVE_INFINITY};
        
        // Generiraj sve permutacije i pronađi najbolju cijenu
        generatePermutations(g, vertices, 0, best);
        
        return best[0];
    }
    
    /**
     * Branch & Bound verzija - PUNO BRŽA!
     * Rano odbacuje grane koje sigurno ne mogu biti bolje od trenutnog optimuma
     */
    public static double branchAndBound(Graph g) {
        int n = g.n;
        
        boolean[] visited = new boolean[n];
        visited[0] = true; // Start je uvijek 0
        
        double[] best = {Double.POSITIVE_INFINITY};
        
        // Preračunaj lower bound info jednom
        double[][] minOutgoing = new double[n][2]; // 2 najmanja outgoing brida za svaki vrh
        for (int i = 0; i < n; i++) {
            minOutgoing[i] = findTwoSmallest(g.min_distances[i], i);
        }
        
        // Pokreni rekurzivnu pretragu s pruningom
        branchAndBoundRecursive(g, 0, 0.0, visited, 1, best, minOutgoing);
        
        return best[0];
    }
    
    private static void branchAndBoundRecursive(Graph g, int current, double currentCost, 
                                                 boolean[] visited, int depth, double[] best,
                                                 double[][] minOutgoing) {
        int n = g.n;
        
        // Bazni slučaj: svi vrhovi posjećeni
        if (depth == n) {
            double totalCost = currentCost + g.min_distances[current][0];
            if (totalCost < best[0]) {
                best[0] = totalCost;
            }
            return;
        }
        
        // PRUNING: Izračunaj donju granicu (lower bound)
        double lowerBound = calculateLowerBound(g, current, currentCost, visited, minOutgoing);
        
        // Ako je lower bound već gori od najboljeg, odbaci ovu granu
        if (lowerBound >= best[0]) {
            return; // PRUNING!
        }
        
        // Probaj sve neposjećene vrhove
        for (int next = 1; next < n; next++) {
            if (visited[next]) continue;
            
            double edgeCost = g.min_distances[current][next];
            if (Double.isInfinite(edgeCost)) continue;
            
            double newCost = currentCost + edgeCost;
            
            // Rano odbacivanje ako već prešli best
            if (newCost >= best[0]) continue;
            
            visited[next] = true;
            branchAndBoundRecursive(g, next, newCost, visited, depth + 1, best, minOutgoing);
            visited[next] = false;
        }
    }
    
    /**
     * Izračunava donju granicu troška za dovršenje ture.
     * 
     * Koristi MST-based lower bound koji je garantirano admisibilan:
     * - Minimalni trošak da se posjete svi preostali čvorovi
     * - Najjeftiniji povratak na početak
     * 
     * ISPRAVLJENO: Stari algoritam je bio preoptimističan i odbacivao dobre grane!
     */
    private static double calculateLowerBound(Graph g, int current, double currentCost, 
                                               boolean[] visited, double[][] minOutgoing) {
        int n = g.n;
        
        // Počnemo s trenutnim troškom
        double bound = currentCost;
        
        // Brojimo neposjećene čvorove
        int unvisitedCount = 0;
        for (int i = 1; i < n; i++) {
            if (!visited[i]) unvisitedCount++;
        }
        
        // Ako nema više neposjećenih, samo dodaj povratak
        if (unvisitedCount == 0) {
            return bound + g.min_distances[current][0];
        }
        
        // MST-based lower bound:
        // 1. Najjeftiniji brid od trenutnog čvora do bilo kojeg neposjećenog
        double minToCurrent = Double.POSITIVE_INFINITY;
        for (int i = 1; i < n; i++) {
            if (!visited[i]) {
                minToCurrent = Math.min(minToCurrent, g.min_distances[current][i]);
            }
        }
        
        // 2. MST svih neposjećenih čvorova (aproksimiran sumom najmanjih bridova)
        // Za svaki neposjećeni čvor: najmanji brid prema bilo kojem DRUGOM neposjećenom ili current
        double mstCost = 0;
        for (int i = 1; i < n; i++) {
            if (!visited[i]) {
                double minEdge = Double.POSITIVE_INFINITY;
                
                // Najmanji brid prema bilo kojem drugom čvoru (posjećenom ili neposjećenom)
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        minEdge = Math.min(minEdge, g.min_distances[i][j]);
                    }
                }
                
                // Dodaj pola (jer MST koristi svaki brid samo jednom, ali mi brojimo i ulaz i izlaz)
                mstCost += minEdge / 2.0;
            }
        }
        
        // 3. Najjeftiniji povratak na 0 od bilo kojeg neposjećenog
        double minToZero = Double.POSITIVE_INFINITY;
        for (int i = 1; i < n; i++) {
            if (!visited[i]) {
                minToZero = Math.min(minToZero, g.min_distances[i][0]);
            }
        }
        
        // Kombinacija: trenutni + putovanje do prvog + MST preostalih + povratak
        // ALI: minToCurrent je već dio MST-a, pa ne dodajemo dvaput
        bound += mstCost + minToZero;
        
        return bound;
    }
    
    /**
     * Nađi 2 najmanja elementa u nizu (osim dijagonale)
     */
    private static double[] findTwoSmallest(double[] arr, int skipIndex) {
        double first = Double.POSITIVE_INFINITY;
        double second = Double.POSITIVE_INFINITY;
        
        for (int i = 0; i < arr.length; i++) {
            if (i == skipIndex) continue;
            
            if (arr[i] < first) {
                second = first;
                first = arr[i];
            } else if (arr[i] < second) {
                second = arr[i];
            }
        }
        
        return new double[]{first, second};
    }
    
    private static void generatePermutations(Graph g, List<Integer> vertices, int index, double[] best) {
        if (index == vertices.size()) {
            // Izračunaj cijenu za ovu permutaciju
            double cost = calculateTourCost(g, vertices);
            if (cost < best[0]) {
                best[0] = cost;
            }
            return;
        }
        
        // Generiraj permutacije zamjenom pozicija
        for (int i = index; i < vertices.size(); i++) {
            // Swap
            swap(vertices, i, index);
            
            // Rekurzivno generiraj permutacije za preostale elemente
            generatePermutations(g, vertices, index + 1, best);
            
            // Backtrack (swap natrag)
            swap(vertices, i, index);
        }
    }
    
    private static double calculateTourCost(Graph g, List<Integer> vertices) {
        double cost = 0.0;
        
        // Počinjemo od vrha 0
        int current = 0;
        
        // Prolazimo kroz sve vrhove u permutaciji
        for (int next : vertices) {
            cost += g.min_distances[current][next];
            if (Double.isInfinite(cost)) {
                return Double.POSITIVE_INFINITY; // Nema puta
            }
            current = next;
        }
        
        // Dodaj cost od zadnjeg vrha natrag do 0
        cost += g.min_distances[current][0];
        
        return cost;
    }
    
    private static void swap(List<Integer> list, int i, int j) {
        int temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
    
}
