package analiza;

import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Analiza optimalnih cijena MCW rješenja u ovisnosti o N
 * 
 * 1. Učitava sve instance iz data/nX/ za X = 4..23
 * 2. Računa avg i stdev cijene za svaki N
 * 3. Fitira funkciju cost(n) i stdev(n)
 * 4. Crta graf s empirijskim podacima i fitted funkcijama
 */
public class CostAnalysis {
    
    // Dataset options: "data" (uniform), "data_exp" (exponential)
    private static String DATA_DIR = "data";
    private static String OUTPUT_DIR = "results/random";
    private static final int MIN_N = 4;
    private static final int MAX_N = 23;
    
    // Rezultati za jedan N
    static class NStats {
        int n;
        List<Double> costs = new ArrayList<>();
        
        double avg() {
            return costs.stream().mapToDouble(d -> d).average().orElse(0.0);
        }
        
        double stdev() {
            double avg = avg();
            return Math.sqrt(costs.stream().mapToDouble(d -> (d - avg) * (d - avg)).average().orElse(0.0));
        }
        
        double min() {
            return costs.stream().mapToDouble(d -> d).min().orElse(0.0);
        }
        
        double max() {
            return costs.stream().mapToDouble(d -> d).max().orElse(0.0);
        }
        
        int count() {
            return costs.size();
        }
    }
    
    public static void main(String[] args) {
        // Dataset selection: "data" (uniform) or "data_exp" (exponential)
        if (args.length > 0) {
            if (args[0].equals("exp") || args[0].equals("data_exp")) {
                DATA_DIR = "data_exp";
                OUTPUT_DIR = "results/exp";
            } else if (args[0].equals("uniform") || args[0].equals("data")) {
                DATA_DIR = "data";
                OUTPUT_DIR = "results/random";
            } else {
                DATA_DIR = args[0]; // Custom path
                OUTPUT_DIR = "results/custom";
            }
        }
        
        // Create output directory if it doesn't exist
        new File(OUTPUT_DIR).mkdirs();
        
        String datasetType = DATA_DIR.equals("data_exp") ? "EXPONENTIAL" : "UNIFORM";
        
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.printf("║      MCW OPTIMAL COST ANALYSIS BY N  [%s]              ║%n", datasetType);
        System.out.println("║  Usage: java CostAnalysis [data|data_exp|exp|uniform]            ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // Prikupi podatke za sve N
        Map<Integer, NStats> allStats = new TreeMap<>();
        
        for (int n = MIN_N; n <= MAX_N; n++) {
            String nDir = DATA_DIR + File.separator + "n" + n;
            File dir = new File(nDir);
            
            if (!dir.exists() || !dir.isDirectory()) {
                System.out.println("Skipping N=" + n + " (no data)");
                continue;
            }
            
            NStats stats = new NStats();
            stats.n = n;
            
            // Učitaj sve instance
            File[] files = dir.listFiles((d, name) -> name.startsWith("instance_") && name.endsWith(".txt"));
            if (files == null) continue;
            
            for (File file : files) {
                try {
                    double cost = loadOptimalCost(file.getAbsolutePath());
                    if (cost > 0) {
                        stats.costs.add(cost);
                    }
                } catch (Exception e) {
                    // Skip problematic files
                }
            }
            
            if (stats.count() > 0) {
                allStats.put(n, stats);
            }
        }
        
        // Ispis rezultata
        System.out.println("┌───────┬──────────┬───────────┬───────────┬───────────┬───────────┬───────────┐");
        System.out.println("│   N   │  Count   │    Avg    │   Stdev   │    Min    │    Max    │  CV [%]   │");
        System.out.println("├───────┼──────────┼───────────┼───────────┼───────────┼───────────┼───────────┤");
        
        List<double[]> dataPoints = new ArrayList<>(); // [n, avg, stdev]
        
        for (NStats s : allStats.values()) {
            double cv = (s.avg() > 0) ? (s.stdev() / s.avg() * 100) : 0;
            System.out.printf("│ %5d │ %8d │ %9.4f │ %9.4f │ %9.4f │ %9.4f │ %8.2f%% │%n",
                s.n, s.count(), s.avg(), s.stdev(), s.min(), s.max(), cv);
            
            dataPoints.add(new double[]{s.n, s.avg(), s.stdev()});
        }
        
        System.out.println("└───────┴──────────┴───────────┴───────────┴───────────┴───────────┴───────────┘");
        System.out.println();
        
        // Regresijska analiza
        System.out.println("═══════════════════════════════════════════════════════════════════");
        System.out.println("                    REGRESSION ANALYSIS");
        System.out.println("═══════════════════════════════════════════════════════════════════");
        
        // Fitiranje funkcija
        // Hipoteza: cost(n) ≈ a * n + b  (linearno)
        // Alternativa: cost(n) ≈ a * sqrt(n) + b
        // Alternativa: cost(n) ≈ a * n^b
        
        double[] nVals = dataPoints.stream().mapToDouble(p -> p[0]).toArray();
        double[] avgVals = dataPoints.stream().mapToDouble(p -> p[1]).toArray();
        double[] stdevVals = dataPoints.stream().mapToDouble(p -> p[2]).toArray();
        
        // 1. Linearni fit za avg: cost = a*n + b
        double[] linearFit = linearRegression(nVals, avgVals);
        System.out.printf("%nLinearni fit za Avg Cost:%n");
        System.out.printf("  cost(n) = %.4f * n + %.4f%n", linearFit[0], linearFit[1]);
        System.out.printf("  R² = %.4f%n", linearFit[2]);
        
        // 2. Sqrt fit za avg: cost = a*sqrt(n) + b
        double[] sqrtNVals = Arrays.stream(nVals).map(Math::sqrt).toArray();
        double[] sqrtFit = linearRegression(sqrtNVals, avgVals);
        System.out.printf("%nSqrt fit za Avg Cost:%n");
        System.out.printf("  cost(n) = %.4f * sqrt(n) + %.4f%n", sqrtFit[0], sqrtFit[1]);
        System.out.printf("  R² = %.4f%n", sqrtFit[2]);
        
        // 3. Log-log fit za avg: log(cost) = a*log(n) + b => cost = e^b * n^a
        double[] logNVals = Arrays.stream(nVals).map(Math::log).toArray();
        double[] logAvgVals = Arrays.stream(avgVals).map(Math::log).toArray();
        double[] powerFit = linearRegression(logNVals, logAvgVals);
        double powerA = powerFit[0];
        double powerB = Math.exp(powerFit[1]);
        System.out.printf("%nPower fit za Avg Cost:%n");
        System.out.printf("  cost(n) = %.4f * n^%.4f%n", powerB, powerA);
        System.out.printf("  R² = %.4f%n", powerFit[2]);
        
        // 4. Logaritamski fit: cost = a*log(n) + b (konvergencija!)
        double[] logFit = linearRegression(logNVals, avgVals);
        System.out.printf("%nLogaritamski fit za Avg Cost:%n");
        System.out.printf("  cost(n) = %.4f * ln(n) + %.4f%n", logFit[0], logFit[1]);
        System.out.printf("  R² = %.4f%n", logFit[2]);
        
        // 5. Asimptotski fit: cost = C - a/n (konvergencija ka C)
        double[] invNVals = Arrays.stream(nVals).map(n -> 1.0/n).toArray();
        double[] asymFit = linearRegression(invNVals, avgVals);
        double asymC = asymFit[1];  // intercept je asimptotska vrijednost
        double asymA = -asymFit[0]; // koeficijent
        System.out.printf("%nAsimptotski fit za Avg Cost:%n");
        System.out.printf("  cost(n) = %.4f - %.4f/n%n", asymC, asymA);
        System.out.printf("  Asimptotska vrijednost C = %.4f%n", asymC);
        System.out.printf("  R² = %.4f%n", asymFit[2]);
        
        // Fit za stdev
        double[] stdevLinearFit = linearRegression(nVals, stdevVals);
        System.out.printf("%nLinearni fit za Stdev:%n");
        System.out.printf("  stdev(n) = %.4f * n + %.4f%n", stdevLinearFit[0], stdevLinearFit[1]);
        System.out.printf("  R² = %.4f%n", stdevLinearFit[2]);
        
        double[] stdevSqrtFit = linearRegression(sqrtNVals, stdevVals);
        System.out.printf("%nSqrt fit za Stdev:%n");
        System.out.printf("  stdev(n) = %.4f * sqrt(n) + %.4f%n", stdevSqrtFit[0], stdevSqrtFit[1]);
        System.out.printf("  R² = %.4f%n", stdevSqrtFit[2]);
        
        // Odabir najboljeg fita - uključujući log i asimptotski
        String bestAvgFit;
        double[] bestAvgParams;
        double bestR2 = Math.max(Math.max(Math.max(Math.max(linearFit[2], sqrtFit[2]), powerFit[2]), logFit[2]), asymFit[2]);
        
        if (asymFit[2] == bestR2) {
            bestAvgFit = "asymptotic";
            bestAvgParams = new double[]{asymC, asymA, asymFit[2]};
        } else if (logFit[2] == bestR2) {
            bestAvgFit = "log";
            bestAvgParams = logFit;
        } else if (sqrtFit[2] == bestR2) {
            bestAvgFit = "sqrt";
            bestAvgParams = sqrtFit;
        } else if (powerFit[2] == bestR2) {
            bestAvgFit = "power";
            bestAvgParams = new double[]{powerB, powerA, powerFit[2]};
        } else {
            bestAvgFit = "linear";
            bestAvgParams = linearFit;
        }
        
        String bestStdevFit = (stdevSqrtFit[2] > stdevLinearFit[2]) ? "sqrt" : "linear";
        double[] bestStdevParams = (bestStdevFit.equals("sqrt")) ? stdevSqrtFit : stdevLinearFit;
        
        System.out.println("\n═══════════════════════════════════════════════════════════════════");
        System.out.println("                    BEST FIT SUMMARY");
        System.out.println("═══════════════════════════════════════════════════════════════════");
        System.out.printf("Avg Cost: %s model (R² = %.4f)%n", bestAvgFit, bestAvgParams[2]);
        System.out.printf("Stdev: %s model (R² = %.4f)%n", bestStdevFit, bestStdevParams[2]);
        
        if (bestAvgFit.equals("asymptotic")) {
            System.out.printf("%n>>> KONVERGENCIJA: Cijena teži ka %.4f za n→∞%n", asymC);
        }
        
        // Predikcije za veće N
        System.out.println("\n═══════════════════════════════════════════════════════════════════");
        System.out.println("                    PREDICTIONS");
        System.out.println("═══════════════════════════════════════════════════════════════════");
        System.out.println("N\tPredicted Avg Cost\tPredicted Stdev");
        
        final double finalAsymC = asymC;
        final double finalAsymA = asymA;
        final double[] finalLogFit = logFit;
        
        for (int n : new int[]{25, 30, 40, 50, 100}) {
            double predAvg = predictCost(n, bestAvgFit, linearFit, sqrtFit, powerB, powerA, logFit, asymC, asymA);
            double predStdev = predictStdev(n, bestStdevFit, stdevLinearFit, stdevSqrtFit);
            System.out.printf("%d\t%.4f\t\t\t%.4f%n", n, predAvg, predStdev);
        }
        
        // Prikaz grafa
        final double[] finalLinearFit = linearFit;
        final double[] finalSqrtFit = sqrtFit;
        final double finalPowerB = powerB;
        final double finalPowerA = powerA;
        final double[] finalStdevLinearFit = stdevLinearFit;
        final double[] finalStdevSqrtFit = stdevSqrtFit;
        final String finalBestAvgFit = bestAvgFit;
        final String finalBestStdevFit = bestStdevFit;
        
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("MCW Cost Analysis");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1400, 600);
            
            JPanel mainPanel = new JPanel(new GridLayout(1, 2));
            
            // Graf 1: Avg Cost vs N (s log i asym fitom)
            mainPanel.add(new CostGraphPanel(dataPoints, "Average Optimal Cost vs N", 
                finalLinearFit, finalSqrtFit, finalPowerB, finalPowerA, 
                finalLogFit, finalAsymC, finalAsymA, finalBestAvgFit, true));
            
            // Graf 2: Stdev vs N
            mainPanel.add(new CostGraphPanel(dataPoints, "Standard Deviation vs N",
                finalStdevLinearFit, finalStdevSqrtFit, 0, 0, 
                null, 0, 0, finalBestStdevFit, false));
            
            frame.add(mainPanel);
            frame.setVisible(true);
        });
        
        // Export to CSV
        exportToCsv(allStats, linearFit, sqrtFit, logFit, asymC, asymA, powerB, powerA);
    }
    
    private static double predictCost(int n, String fitType, double[] linearFit, 
                                       double[] sqrtFit, double powerB, double powerA,
                                       double[] logFit, double asymC, double asymA) {
        return switch (fitType) {
            case "linear" -> linearFit[0] * n + linearFit[1];
            case "sqrt" -> sqrtFit[0] * Math.sqrt(n) + sqrtFit[1];
            case "power" -> powerB * Math.pow(n, powerA);
            case "log" -> logFit[0] * Math.log(n) + logFit[1];
            case "asymptotic" -> asymC - asymA / n;
            default -> 0;
        };
    }
    
    private static double predictStdev(int n, String fitType, double[] linearFit, double[] sqrtFit) {
        return switch (fitType) {
            case "linear" -> linearFit[0] * n + linearFit[1];
            case "sqrt" -> sqrtFit[0] * Math.sqrt(n) + sqrtFit[1];
            default -> 0;
        };
    }
    
    /**
     * Linearna regresija: y = ax + b
     * Vraća [a, b, R²]
     */
    private static double[] linearRegression(double[] x, double[] y) {
        int n = x.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        
        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
        }
        
        double a = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double b = (sumY - a * sumX) / n;
        
        // R² calculation
        double yMean = sumY / n;
        double ssTot = 0, ssRes = 0;
        for (int i = 0; i < n; i++) {
            double yPred = a * x[i] + b;
            ssTot += (y[i] - yMean) * (y[i] - yMean);
            ssRes += (y[i] - yPred) * (y[i] - yPred);
        }
        double r2 = 1 - (ssRes / ssTot);
        
        return new double[]{a, b, r2};
    }
    
    private static double loadOptimalCost(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("# Optimal cost:")) {
                    return Double.parseDouble(line.substring("# Optimal cost:".length()).trim());
                }
            }
        }
        return -1;
    }
    
    private static void exportToCsv(Map<Integer, NStats> stats, double[] linearFit, 
                                     double[] sqrtFit, double[] logFit, 
                                     double asymC, double asymA,
                                     double powerB, double powerA) {
        String filename = OUTPUT_DIR + File.separator + "cost_analysis_results.csv";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("N,Count,AvgCost,Stdev,Min,Max,CV,LinearPred,SqrtPred,PowerPred,LogPred,AsymPred");
            
            for (NStats s : stats.values()) {
                double cv = (s.avg() > 0) ? (s.stdev() / s.avg() * 100) : 0;
                double linearPred = linearFit[0] * s.n + linearFit[1];
                double sqrtPred = sqrtFit[0] * Math.sqrt(s.n) + sqrtFit[1];
                double powerPred = powerB * Math.pow(s.n, powerA);
                double logPred = logFit[0] * Math.log(s.n) + logFit[1];
                double asymPred = asymC - asymA / s.n;
                
                writer.printf("%d,%d,%.6f,%.6f,%.6f,%.6f,%.2f,%.6f,%.6f,%.6f,%.6f,%.6f%n",
                    s.n, s.count(), s.avg(), s.stdev(), s.min(), s.max(), cv,
                    linearPred, sqrtPred, powerPred, logPred, asymPred);
            }
            
            System.out.println("\n📁 Results exported to: " + filename);
            
        } catch (IOException e) {
            System.err.println("Error exporting CSV: " + e.getMessage());
        }
    }
    
    // Panel za crtanje grafa
    static class CostGraphPanel extends JPanel {
        private final List<double[]> data;
        private final String title;
        private final double[] linearFit;
        private final double[] sqrtFit;
        private final double powerB, powerA;
        private final double[] logFit;
        private final double asymC, asymA;
        private final String bestFit;
        private final boolean isAvgGraph;
        
        public CostGraphPanel(List<double[]> data, String title, 
                              double[] linearFit, double[] sqrtFit,
                              double powerB, double powerA,
                              double[] logFit, double asymC, double asymA,
                              String bestFit, boolean isAvgGraph) {
            this.data = data;
            this.title = title;
            this.linearFit = linearFit;
            this.sqrtFit = sqrtFit;
            this.powerB = powerB;
            this.powerA = powerA;
            this.logFit = logFit;
            this.asymC = asymC;
            this.asymA = asymA;
            this.bestFit = bestFit;
            this.isAvgGraph = isAvgGraph;
            setBackground(Color.WHITE);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int padding = 60;
            int width = getWidth() - 2 * padding;
            int height = getHeight() - 2 * padding - 30;
            
            // Nađi min/max
            double minN = data.stream().mapToDouble(p -> p[0]).min().orElse(0);
            double maxN = data.stream().mapToDouble(p -> p[0]).max().orElse(1);
            double minY = data.stream().mapToDouble(p -> isAvgGraph ? p[1] : p[2]).min().orElse(0);
            double maxY = data.stream().mapToDouble(p -> isAvgGraph ? p[1] : p[2]).max().orElse(1);
            
            // Dodaj margin
            double yRange = maxY - minY;
            minY -= yRange * 0.1;
            maxY += yRange * 0.1;
            if (minY < 0) minY = 0;
            
            // Osi
            g2.setColor(Color.BLACK);
            g2.drawLine(padding, getHeight() - padding, padding + width, getHeight() - padding); // X os
            g2.drawLine(padding, padding, padding, getHeight() - padding); // Y os
            
            // Naslov
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString(title, padding + width/2 - g2.getFontMetrics().stringWidth(title)/2, 25);
            
            // Oznake osi
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            for (int n = (int)minN; n <= maxN; n += 2) {
                int x = padding + (int)((n - minN) / (maxN - minN) * width);
                g2.drawLine(x, getHeight() - padding, x, getHeight() - padding + 5);
                g2.drawString(String.valueOf(n), x - 5, getHeight() - padding + 18);
            }
            
            // Y oznake
            for (int i = 0; i <= 5; i++) {
                double y = minY + (maxY - minY) * i / 5;
                int yPos = getHeight() - padding - (int)(i * height / 5.0);
                g2.drawLine(padding - 5, yPos, padding, yPos);
                g2.drawString(String.format("%.2f", y), 5, yPos + 4);
            }
            
            // Labele osi
            g2.drawString("N", padding + width/2, getHeight() - 10);
            
            // Crtanje fitted funkcija
            g2.setStroke(new BasicStroke(2));
            
            // Linear fit - crvena
            g2.setColor(new Color(255, 100, 100));
            drawFitLine(g2, "linear", linearFit, padding, width, height, minN, maxN, minY, maxY);
            
            // Sqrt fit - zelena
            g2.setColor(new Color(100, 200, 100));
            drawFitLine(g2, "sqrt", sqrtFit, padding, width, height, minN, maxN, minY, maxY);
            
            // Power fit - samo za avg graf (plava)
            if (isAvgGraph && powerB > 0) {
                g2.setColor(new Color(100, 100, 255));
                drawPowerLine(g2, powerB, powerA, padding, width, height, minN, maxN, minY, maxY);
            }
            
            // Log fit - samo za avg graf (narančasta)
            if (isAvgGraph && logFit != null) {
                g2.setColor(new Color(255, 165, 0));
                drawLogLine(g2, logFit, padding, width, height, minN, maxN, minY, maxY);
            }
            
            // Asymptotic fit - samo za avg graf (ljubičasta)
            if (isAvgGraph && asymC > 0) {
                g2.setColor(new Color(148, 0, 211));
                drawAsymptoticLine(g2, asymC, asymA, padding, width, height, minN, maxN, minY, maxY);
            }
            
            // Crtanje točaka podataka
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(1));
            for (double[] point : data) {
                double n = point[0];
                double val = isAvgGraph ? point[1] : point[2];
                
                int x = padding + (int)((n - minN) / (maxN - minN) * width);
                int y = getHeight() - padding - (int)((val - minY) / (maxY - minY) * height);
                
                g2.fillOval(x - 5, y - 5, 10, 10);
            }
            
            // Legenda
            int legendX = padding + 10;
            int legendY = padding + 20;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            
            g2.setColor(Color.BLACK);
            g2.fillOval(legendX, legendY - 4, 8, 8);
            g2.drawString("Data points", legendX + 15, legendY + 4);
            
            g2.setColor(new Color(255, 100, 100));
            g2.drawLine(legendX, legendY + 20, legendX + 12, legendY + 20);
            g2.setColor(Color.BLACK);
            g2.drawString("Linear fit (R²=" + String.format("%.3f", linearFit[2]) + ")", legendX + 15, legendY + 24);
            
            g2.setColor(new Color(100, 200, 100));
            g2.drawLine(legendX, legendY + 40, legendX + 12, legendY + 40);
            g2.setColor(Color.BLACK);
            g2.drawString("Sqrt fit (R²=" + String.format("%.3f", sqrtFit[2]) + ")", legendX + 15, legendY + 44);
            
            if (isAvgGraph && powerB > 0) {
                g2.setColor(new Color(100, 100, 255));
                g2.drawLine(legendX, legendY + 60, legendX + 12, legendY + 60);
                g2.setColor(Color.BLACK);
                g2.drawString("Power fit", legendX + 15, legendY + 64);
                
                if (logFit != null) {
                    g2.setColor(new Color(255, 165, 0));
                    g2.drawLine(legendX, legendY + 80, legendX + 12, legendY + 80);
                    g2.setColor(Color.BLACK);
                    g2.drawString("Log fit (R²=" + String.format("%.3f", logFit[2]) + ")", legendX + 15, legendY + 84);
                }
                
                if (asymC > 0) {
                    g2.setColor(new Color(148, 0, 211));
                    g2.drawLine(legendX, legendY + 100, legendX + 12, legendY + 100);
                    g2.setColor(Color.BLACK);
                    g2.drawString("Asymptotic (C=" + String.format("%.2f", asymC) + ")", legendX + 15, legendY + 104);
                }
            }
        }
        
        private void drawFitLine(Graphics2D g2, String type, double[] params,
                                  int padding, int width, int height,
                                  double minN, double maxN, double minY, double maxY) {
            Path2D path = new Path2D.Double();
            boolean first = true;
            
            for (double n = minN; n <= maxN; n += 0.1) {
                double val = type.equals("sqrt") ? 
                    params[0] * Math.sqrt(n) + params[1] :
                    params[0] * n + params[1];
                
                int x = padding + (int)((n - minN) / (maxN - minN) * width);
                int y = getHeight() - padding - (int)((val - minY) / (maxY - minY) * height);
                
                if (first) {
                    path.moveTo(x, y);
                    first = false;
                } else {
                    path.lineTo(x, y);
                }
            }
            g2.draw(path);
        }
        
        private void drawPowerLine(Graphics2D g2, double b, double a,
                                    int padding, int width, int height,
                                    double minN, double maxN, double minY, double maxY) {
            Path2D path = new Path2D.Double();
            boolean first = true;
            
            for (double n = minN; n <= maxN; n += 0.1) {
                double val = b * Math.pow(n, a);
                
                int x = padding + (int)((n - minN) / (maxN - minN) * width);
                int y = getHeight() - padding - (int)((val - minY) / (maxY - minY) * height);
                
                if (first) {
                    path.moveTo(x, y);
                    first = false;
                } else {
                    path.lineTo(x, y);
                }
            }
            g2.draw(path);
        }
        
        private void drawLogLine(Graphics2D g2, double[] params,
                                  int padding, int width, int height,
                                  double minN, double maxN, double minY, double maxY) {
            Path2D path = new Path2D.Double();
            boolean first = true;
            
            for (double n = Math.max(minN, 1); n <= maxN; n += 0.1) {
                double val = params[0] * Math.log(n) + params[1];
                
                int x = padding + (int)((n - minN) / (maxN - minN) * width);
                int y = getHeight() - padding - (int)((val - minY) / (maxY - minY) * height);
                
                if (first) {
                    path.moveTo(x, y);
                    first = false;
                } else {
                    path.lineTo(x, y);
                }
            }
            g2.draw(path);
        }
        
        private void drawAsymptoticLine(Graphics2D g2, double C, double a,
                                         int padding, int width, int height,
                                         double minN, double maxN, double minY, double maxY) {
            Path2D path = new Path2D.Double();
            boolean first = true;
            
            for (double n = Math.max(minN, 1); n <= maxN; n += 0.1) {
                double val = C - a / n;
                
                int x = padding + (int)((n - minN) / (maxN - minN) * width);
                int y = getHeight() - padding - (int)((val - minY) / (maxY - minY) * height);
                
                if (first) {
                    path.moveTo(x, y);
                    first = false;
                } else {
                    path.lineTo(x, y);
                }
            }
            g2.draw(path);
        }
    }
}
