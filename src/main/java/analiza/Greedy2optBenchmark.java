package analiza;

import java.io.*;
import java.util.*;
import heuristika.Greedy;
import utils.*;

/**
 * Usporedba Greedy i Greedy+2opt na MCW instancama iz dataseta
 */
public class Greedy2optBenchmark {
    
    private static String DATA_DIR = "data";
    private static String OUTPUT_DIR = "results/random";
    
    public static void main(String[] args) {
        // Dataset selection
        if (args.length > 0) {
            if (args[0].equals("exp") || args[0].equals("data_exp")) {
                DATA_DIR = "data_exp";
                OUTPUT_DIR = "results/exp";
            }
        }
        new File(OUTPUT_DIR).mkdirs();
        
        String datasetType = DATA_DIR.equals("data_exp") ? "EXPONENTIAL" : "UNIFORM";
        
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║         GREEDY vs GREEDY+2OPT BENCHMARK                          ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Dataset: %s%n", datasetType);
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // Rezultati po N
        List<NResult> allResults = new ArrayList<>();
        
        for (int n = 4; n <= 24; n++) {
            String nDir = DATA_DIR + File.separator + "n" + n;
            File dir = new File(nDir);
            if (!dir.exists()) continue;
            
            File[] files = dir.listFiles((d, name) -> name.startsWith("instance_") && name.endsWith(".txt"));
            if (files == null || files.length == 0) continue;
            
            NResult result = new NResult(n);
            
            for (File file : files) {
                try {
                    InstanceData data = loadInstance(file.getAbsolutePath());
                    if (data.matrix == null || data.optimalCost <= 0) continue;
                    
                    Graph g = new Graph(data.matrix, data.optimalCost, data.optimalWalk);
                    
                    // Greedy
                    long t1 = System.nanoTime();
                    Result greedyResult = Greedy.solve(g);
                    long t2 = System.nanoTime();
                    
                    // Greedy + 2opt
                    long t3 = System.nanoTime();
                    Result greedy2optResult = solveGreedy2opt(g);
                    long t4 = System.nanoTime();
                    
                    double greedyGap = (greedyResult.cost - data.optimalCost) / data.optimalCost * 100;
                    double greedy2optGap = (greedy2optResult.cost - data.optimalCost) / data.optimalCost * 100;
                    
                    result.greedyGaps.add(greedyGap);
                    result.greedy2optGaps.add(greedy2optGap);
                    result.greedyTimes.add((t2 - t1) / 1e6);
                    result.greedy2optTimes.add((t4 - t3) / 1e6);
                    
                } catch (Exception e) {
                    // Skip
                }
            }
            
            if (!result.greedyGaps.isEmpty()) {
                allResults.add(result);
                System.out.printf("N=%2d: Greedy=%.2f%% (%.3fms), Greedy+2opt=%.2f%% (%.3fms), instances=%d%n",
                    n, result.avgGreedyGap(), result.avgGreedyTime(),
                    result.avgGreedy2optGap(), result.avgGreedy2optTime(),
                    result.greedyGaps.size());
            }
        }
        
        // Summary
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════════════");
        System.out.println("                         SUMMARY TABLE");
        System.out.println("═══════════════════════════════════════════════════════════════════");
        System.out.println("┌──────┬────────────────────────────┬────────────────────────────┐");
        System.out.println("│  N   │        Greedy              │       Greedy+2opt          │");
        System.out.println("│      │  Gap%   │ Stdev  │ Time   │  Gap%   │ Stdev  │ Time   │");
        System.out.println("├──────┼─────────┼────────┼────────┼─────────┼────────┼────────┤");
        
        for (NResult r : allResults) {
            System.out.printf("│ %4d │ %6.2f%% │ %5.2f%% │ %5.2fms│ %6.2f%% │ %5.2f%% │ %5.2fms│%n",
                r.n, 
                r.avgGreedyGap(), r.stdevGreedyGap(), r.avgGreedyTime(),
                r.avgGreedy2optGap(), r.stdevGreedy2optGap(), r.avgGreedy2optTime());
        }
        System.out.println("└──────┴─────────┴────────┴────────┴─────────┴────────┴────────┘");
        
        // Export CSV
        exportCsv(allResults);
    }
    
    static Result solveGreedy2opt(Graph g) {
        // First run Greedy
        Result greedy = Greedy.solve(g);
        
        // Convert to array for 2-opt
        int[] perm = new int[g.n];
        for (int i = 0; i < g.n; i++) {
            perm[i] = greedy.tour.get(i);
        }
        
        // Apply 2-opt
        perm = twoOpt(perm, g.min_distances);
        
        // Calculate cost
        double cost = 0;
        for (int i = 0; i < perm.length; i++) {
            cost += g.min_distances[perm[i]][perm[(i + 1) % perm.length]];
        }
        
        List<Integer> tour = new ArrayList<>();
        for (int node : perm) tour.add(node);
        tour.add(perm[0]);
        
        return new Result(cost, tour);
    }
    
    static int[] twoOpt(int[] perm, double[][] d) {
        int n = perm.length;
        boolean improved = true;
        
        while (improved) {
            improved = false;
            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 2; j < n; j++) {
                    if (i == 0 && j == n - 1) continue;
                    
                    int a = perm[i], b = perm[i + 1];
                    int c = perm[j], d2 = perm[(j + 1) % n];
                    
                    double before = d[a][b] + d[c][d2];
                    double after = d[a][c] + d[b][d2];
                    
                    if (after < before - 1e-9) {
                        // Reverse segment [i+1, j]
                        int left = i + 1, right = j;
                        while (left < right) {
                            int temp = perm[left];
                            perm[left] = perm[right];
                            perm[right] = temp;
                            left++;
                            right--;
                        }
                        improved = true;
                    }
                }
            }
        }
        return perm;
    }
    
    static class NResult {
        int n;
        List<Double> greedyGaps = new ArrayList<>();
        List<Double> greedy2optGaps = new ArrayList<>();
        List<Double> greedyTimes = new ArrayList<>();
        List<Double> greedy2optTimes = new ArrayList<>();
        
        NResult(int n) { this.n = n; }
        
        double avgGreedyGap() { return greedyGaps.stream().mapToDouble(d -> d).average().orElse(0); }
        double avgGreedy2optGap() { return greedy2optGaps.stream().mapToDouble(d -> d).average().orElse(0); }
        double avgGreedyTime() { return greedyTimes.stream().mapToDouble(d -> d).average().orElse(0); }
        double avgGreedy2optTime() { return greedy2optTimes.stream().mapToDouble(d -> d).average().orElse(0); }
        
        double stdevGreedyGap() {
            double avg = avgGreedyGap();
            return Math.sqrt(greedyGaps.stream().mapToDouble(d -> (d - avg) * (d - avg)).average().orElse(0));
        }
        double stdevGreedy2optGap() {
            double avg = avgGreedy2optGap();
            return Math.sqrt(greedy2optGaps.stream().mapToDouble(d -> (d - avg) * (d - avg)).average().orElse(0));
        }
    }
    
    static class InstanceData {
        double[][] matrix;
        double optimalCost;
        List<Integer> optimalWalk;
    }
    
    static InstanceData loadInstance(String filename) throws IOException {
        InstanceData data = new InstanceData();
        List<double[]> matrixRows = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                if (line.startsWith("# Optimal cost:")) {
                    data.optimalCost = Double.parseDouble(line.substring("# Optimal cost:".length()).trim());
                } else if (line.startsWith("# Optimal walk (full path):")) {
                    data.optimalWalk = parseIntList(line.substring(line.indexOf(':') + 1).trim());
                } else if (line.startsWith("# Optimal tour")) {
                    if (data.optimalWalk == null) {
                        data.optimalWalk = parseIntList(line.substring(line.indexOf(':') + 1).trim());
                    }
                } else if (!line.startsWith("#")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length > 1) {
                        try {
                            double[] row = new double[parts.length];
                            for (int j = 0; j < parts.length; j++) {
                                row[j] = Double.parseDouble(parts[j]);
                            }
                            matrixRows.add(row);
                        } catch (NumberFormatException e) {}
                    }
                }
            }
        }
        
        if (!matrixRows.isEmpty()) {
            data.matrix = matrixRows.toArray(new double[0][]);
        }
        
        return data;
    }
    
    static List<Integer> parseIntList(String s) {
        s = s.replace("[", "").replace("]", "");
        String[] parts = s.split(",\\s*");
        List<Integer> result = new ArrayList<>();
        for (String part : parts) {
            try { result.add(Integer.parseInt(part.trim())); } catch (Exception e) {}
        }
        return result.isEmpty() ? null : result;
    }
    
    static void exportCsv(List<NResult> results) {
        String filename = OUTPUT_DIR + File.separator + "greedy_2opt_comparison.csv";
        try (PrintWriter w = new PrintWriter(new FileWriter(filename))) {
            w.println("N,GreedyGap,GreedyStdev,GreedyTime,Greedy2optGap,Greedy2optStdev,Greedy2optTime,Instances");
            for (NResult r : results) {
                w.printf("%d,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%d%n",
                    r.n, r.avgGreedyGap(), r.stdevGreedyGap(), r.avgGreedyTime(),
                    r.avgGreedy2optGap(), r.stdevGreedy2optGap(), r.avgGreedy2optTime(),
                    r.greedyGaps.size());
            }
            System.out.println("\n📁 Results exported to: " + filename);
        } catch (Exception e) {
            System.err.println("Error exporting: " + e.getMessage());
        }
    }
}
