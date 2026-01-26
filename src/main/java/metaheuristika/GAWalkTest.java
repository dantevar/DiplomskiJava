package metaheuristika;

import utils.*;
import java.util.*;

public class GAWalkTest {
    
    static Random rand = new Random();
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== USPOREDBA: GA vs GA+2opt vs GAWalk vs Multi-start 2-opt ===\n");
        
        int[] testInstances = {0, 1, 2, 3, 4};
        
        System.out.println("Instance\tOptimal\t\tGA\t\tGA+2opt\t\tGAWalk\t\tMS-2opt");
        System.out.println("─".repeat(85));
        
        double[] avgGaps = new double[4]; // GA, GA2opt, GAWalk, MS2opt
        
        for (int inst : testInstances) {
            Graph g = InstanceLoader.loadInstance("data/n24/instance_" + inst + ".txt");
            
            // 1. GA Standard
            Result gaResult = GA.solve(g, 100, 100, 0.3, 0);
            double gapGA = (gaResult.cost - g.optimalCost) / g.optimalCost * 100;
            
            // 2. GA + 2-opt
            Result ga2optResult = GA2opt.solve(g, 100, 100, 0.3, 0);
            double gapGA2opt = (ga2optResult.cost - g.optimalCost) / g.optimalCost * 100;
            
            // 3. GAWalk
            Result gaWalkResult = GAWalk.solve(g, 100, 100, 0.3, 0);
            double gapGAWalk = (gaWalkResult.cost - g.optimalCost) / g.optimalCost * 100;
            
            // 4. Multi-start 2-opt
            double bestMS = Double.MAX_VALUE;
            for (int i = 0; i < 100; i++) {
                int[] perm = randomPerm(g.n);
                perm = twoOpt(perm, g.min_distances);
                double cost = evaluatePerm(perm, g.min_distances);
                if (cost < bestMS) bestMS = cost;
            }
            double gapMS = (bestMS - g.optimalCost) / g.optimalCost * 100;
            
            avgGaps[0] += gapGA;
            avgGaps[1] += gapGA2opt;
            avgGaps[2] += gapGAWalk;
            avgGaps[3] += gapMS;
            
            System.out.printf("%d\t\t%.4f\t\t%.2f%%\t\t%.2f%%\t\t%.2f%%\t\t%.2f%%%n",
                inst, g.optimalCost, gapGA, gapGA2opt, gapGAWalk, gapMS);
        }
        
        System.out.println("─".repeat(85));
        int n = testInstances.length;
        System.out.printf("AVERAGE\t\t\t\t%.2f%%\t\t%.2f%%\t\t%.2f%%\t\t%.2f%%%n",
            avgGaps[0]/n, avgGaps[1]/n, avgGaps[2]/n, avgGaps[3]/n);
        
        System.out.println("\n=== ZAŠTO JE 2-OPT TAKO MOĆAN? ===");
        System.out.println("1. Random instance imaju 'glatku' fitness površinu");
        System.out.println("2. Nema dubokih lokalnih optimuma - lako se izađe");
        System.out.println("3. Za N=24, ima samo 24*23/2 = 276 mogućih 2-opt poteza");
        System.out.println("4. Uniformne težine [0,1] ne stvaraju 'zamke'");
        System.out.println("\n=== KADA 2-OPT NIJE DOVOLJAN? ===");
        System.out.println("1. TSPLIB benchmarki (realni gradovi)");
        System.out.println("2. Cluster instance (gradovi u grupama)");
        System.out.println("3. N > 100 (previše lokalnih optimuma)");
        System.out.println("4. Asimetrični TSP");
    }
    
    static int[] randomPerm(int n) {
        int[] perm = new int[n];
        for (int i = 0; i < n; i++) perm[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int tmp = perm[i]; perm[i] = perm[j]; perm[j] = tmp;
        }
        return perm;
    }
    
    static double evaluatePerm(int[] perm, double[][] minDist) {
        double cost = 0;
        int n = perm.length;
        for (int i = 0; i < n - 1; i++) {
            cost += minDist[perm[i]][perm[i + 1]];
        }
        cost += minDist[perm[n - 1]][perm[0]];
        return cost;
    }
    
    static int[] twoOpt(int[] perm, double[][] d) {
        int n = perm.length;
        boolean improved = true;
        while (improved) {
            improved = false;
            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 2; j < n; j++) {
                    int a = perm[i], b = perm[i+1];
                    int c = perm[j], dn = perm[(j+1) % n];
                    double delta = (d[a][c] + d[b][dn]) - (d[a][b] + d[c][dn]);
                    if (delta < -1e-10) {
                        for (int l = i+1, r = j; l < r; l++, r--) {
                            int tmp = perm[l]; perm[l] = perm[r]; perm[r] = tmp;
                        }
                        improved = true;
                    }
                }
            }
        }
        return perm;
    }
}

