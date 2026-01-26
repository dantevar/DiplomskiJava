package utils;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Instance loader - učitava random instance iz data/nX foldera.
 */
public class InstanceLoader {
    
    /**
     * Učitava random instancu za zadani n.
     * 
     * @param n Veličina grafa
     * @return GraphInstance sa distance matricom i optimalnim costom
     * @throws IOException ako folder ne postoji ili nema instanci
     */
    public static Graph loadRandomInstance(int n) throws IOException {
        String folderPath = "data/n" + n;
        File folder = new File(folderPath);
        
        if (!folder.exists() || !folder.isDirectory()) {
            throw new IOException("Folder does not exist: " + folderPath);
        }
        
        // Nađi sve .txt fileove
        File[] files = folder.listFiles((dir, name) -> name.startsWith("instance_") && name.endsWith(".txt"));
        
        if (files == null || files.length == 0) {
            throw new IOException("No instances found in: " + folderPath);
        }
        
        // Odaberi random file
        Random rand = new Random();
        File selectedFile = files[rand.nextInt(files.length)];
        
        return loadInstance(selectedFile.getPath());
    }
    
    /**
     * Učitava specifičnu instancu iz filea.
     */
    public static Graph loadInstance(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        
        int n = 0;
        double optimalCost = 0.0;
        double[][] distances = null;
        List<Integer> optimalWalk = null;
        
        int lineIdx = 0;
        boolean readingMatrix = false;
        int matrixRow = 0;
        
        for (String line : lines) {
            line = line.trim();
            
            // Skip empty lines
            if (line.isEmpty()) continue;
            
            // Parse metadata
            if (line.startsWith("# Graph size:")) {
                n = Integer.parseInt(line.split(":")[1].trim());
                distances = new double[n][n];
            } else if (line.startsWith("# Optimal cost:")) {
                optimalCost = Double.parseDouble(line.split(":")[1].trim());
            } else if (line.startsWith("# Distance Matrix")) {
                readingMatrix = true;
                matrixRow = 0;
            } else if (line.startsWith("# Optimal Walk")) {
                readingMatrix = false;
            } else if (line.startsWith("#")) {
                // Skip other comments
                continue;
            } else if (readingMatrix) {
                // Parse matrix row
                String[] parts = line.split("\\s+");
                for (int j = 0; j < parts.length && j < n; j++) {
                    distances[matrixRow][j] = Double.parseDouble(parts[j]);
                }
                matrixRow++;
            } else {
                // Parse optimal walk
                String[] parts = line.split("\\s+");
                optimalWalk = new ArrayList<>();
                for (String part : parts) {
                    if (!part.isEmpty()) {
                        optimalWalk.add(Integer.parseInt(part));
                    }
                }
            }
        }
        
        return new Graph(distances,optimalCost, optimalWalk);
    }
    

}
