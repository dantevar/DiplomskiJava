package analiza;

import java.io.*;
import java.util.*;

/**
 * Analizira koliko rješenja su šetnje (s ponavljanjem čvorova) vs putevi (bez ponavljanja)
 */
public class WalkVsPathAnalysis {
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║           WALK VS PATH ANALYSIS                                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        analyzeDataset("data", "UNIFORM (Random)");
        System.out.println();
        analyzeDataset("data_exp", "EXPONENTIAL");
    }
    
    private static void analyzeDataset(String dataDir, String name) {
        System.out.println("═══════════════════════════════════════════════════════════════════");
        System.out.printf("  Dataset: %s (%s)%n", dataDir, name);
        System.out.println("═══════════════════════════════════════════════════════════════════");
        
        File baseDir = new File(dataDir);
        if (!baseDir.exists()) {
            System.out.println("  Directory not found: " + dataDir);
            return;
        }
        
        System.out.println("┌───────┬──────────┬──────────┬──────────┬──────────┐");
        System.out.println("│   N   │  Total   │  Paths   │  Walks   │ Walk [%] │");
        System.out.println("├───────┼──────────┼──────────┼──────────┼──────────┤");
        
        int totalPaths = 0;
        int totalWalks = 0;
        int totalInstances = 0;
        
        for (int n = 4; n <= 23; n++) {
            File nDir = new File(dataDir + File.separator + "n" + n);
            if (!nDir.exists() || !nDir.isDirectory()) continue;
            
            File[] files = nDir.listFiles((d, fname) -> fname.startsWith("instance_") && fname.endsWith(".txt"));
            if (files == null || files.length == 0) continue;
            
            int paths = 0;
            int walks = 0;
            
            for (File file : files) {
                try {
                    List<Integer> walk = loadOptimalWalk(file.getAbsolutePath());
                    if (walk == null || walk.isEmpty()) continue;
                    
                    // Check if it's a walk (has duplicates) or path (no duplicates)
                    // A path visits each node exactly once: size = n+1 (includes return to start)
                    // A walk may visit nodes multiple times: size > n+1 OR has duplicates
                    
                    boolean isWalk = false;
                    
                    // Method 1: Size check
                    if (walk.size() > n + 1) {
                        isWalk = true;
                    } else {
                        // Method 2: Check for duplicates (excluding start/end which is 0)
                        Set<Integer> seen = new HashSet<>();
                        for (int i = 0; i < walk.size() - 1; i++) { // Exclude last element (return to start)
                            int node = walk.get(i);
                            if (seen.contains(node)) {
                                isWalk = true;
                                break;
                            }
                            seen.add(node);
                        }
                    }
                    
                    if (isWalk) {
                        walks++;
                    } else {
                        paths++;
                    }
                    
                } catch (Exception e) {
                    // Skip problematic files
                }
            }
            
            int total = paths + walks;
            double walkPct = total > 0 ? (walks * 100.0 / total) : 0;
            
            System.out.printf("│ %5d │ %8d │ %8d │ %8d │ %7.2f%% │%n", 
                n, total, paths, walks, walkPct);
            
            totalPaths += paths;
            totalWalks += walks;
            totalInstances += total;
        }
        
        System.out.println("├───────┼──────────┼──────────┼──────────┼──────────┤");
        double totalWalkPct = totalInstances > 0 ? (totalWalks * 100.0 / totalInstances) : 0;
        System.out.printf("│ TOTAL │ %8d │ %8d │ %8d │ %7.2f%% │%n", 
            totalInstances, totalPaths, totalWalks, totalWalkPct);
        System.out.println("└───────┴──────────┴──────────┴──────────┴──────────┘");
    }
    
    private static List<Integer> loadOptimalWalk(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Format 1: "# Optimal walk: [0, 4, 15, ...]" (exponential)
                if (line.startsWith("# Optimal walk:")) {
                    String walkStr = line.substring("# Optimal walk:".length()).trim();
                    walkStr = walkStr.replace("[", "").replace("]", "");
                    String[] parts = walkStr.split(",\\s*");
                    List<Integer> walk = new ArrayList<>();
                    for (String part : parts) {
                        walk.add(Integer.parseInt(part.trim()));
                    }
                    return walk;
                }
                
                // Format 2: "# Optimal Walk" header followed by nodes (uniform)
                // Nodes can be on one line separated by spaces OR one per line
                if (line.equals("# Optimal Walk")) {
                    List<Integer> walk = new ArrayList<>();
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) break;
                        // Split by spaces - could be one node or multiple
                        String[] parts = line.split("\\s+");
                        for (String part : parts) {
                            if (!part.isEmpty()) {
                                walk.add(Integer.parseInt(part));
                            }
                        }
                    }
                    return walk;
                }
            }
        }
        return null;
    }
}
