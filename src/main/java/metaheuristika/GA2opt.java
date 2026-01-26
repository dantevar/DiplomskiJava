package metaheuristika;

import java.util.*;
import utils.*;

/**
 * GA Standard + 2-opt Local Search
 * 
 * Kombinira genetski algoritam s 2-opt local search za intenzifikaciju.
 * 2-opt je moćan jer:
 * 1. O(n²) po iteraciji - brzo za male n
 * 2. Garantirano pronalazi lokalni optimum
 * 3. Za random instance često pronalazi globalni optimum
 * 
 * ZAŠTO JE 2-OPT TAKO DOBAR NA RANDOM INSTANCAMA?
 * - Random instance imaju "glatku" fitness površinu bez dubokih lokalnih optimuma
 * - Težine su uniformno distribuirane [0,1] - nema "zamki"
 * - Za N < 50, 2-opt prostor pretrage je mali (O(n²) poteza)
 * 
 * KADA 2-OPT NIJE DOVOLJAN?
 * - Strukturirane instance (TSPLIB benchmarki)
 * - Cluster instance (gradovi u grupama)
 * - Veliki N (> 100) - previše lokalnih optimuma
 */
public class GA2opt {
    
    private static final Random rand = new Random();
    
    public static Result solve(Graph g, int popSize, int generations, double mutationRate, int printEvery) {
        return solve(g, popSize, generations, mutationRate, 0.70, printEvery);
    }
    
    public static Result solve(Graph g, int popSize, int generations, double mutationRate, 
                                double crossoverRate, int printEvery) {
        int n = g.n;
        double[][] distances = g.min_distances;
        if (n <= 1) return new Result(0, Arrays.asList(0));

        // Initialize population (random permutations)
        List<int[]> population = new ArrayList<>();
        for (int i = 0; i < popSize; i++) {
            int[] tour = randomTour(n);
            // Primijeni 2-opt na početnu populaciju
            tour = twoOpt(tour, distances);
            population.add(tour);
        }

        int[] bestTour = population.get(0);
        double bestCost = fitness(bestTour, distances);

        // Evolution
        for (int gen = 0; gen < generations; gen++) {
            if (printEvery > 0 && gen % printEvery == 0) {
                System.out.println("Generation " + gen + ": Best cost = " + bestCost);
            }
            
            // Sort by fitness
            population.sort((a, b) -> Double.compare(fitness(a, distances), fitness(b, distances)));
            
            // Track best
            double currentBest = fitness(population.get(0), distances);
            if (currentBest < bestCost) {
                bestCost = currentBest;
                bestTour = population.get(0).clone();
            }

            // New generation
            List<int[]> newPop = new ArrayList<>();
            
            // Elitism: keep top 10%
            int eliteCount = Math.max(1, popSize / 10);
            for (int i = 0; i < eliteCount; i++) {
                newPop.add(population.get(i).clone());
            }

            // Crossover and mutation
            while (newPop.size() < popSize) {
                int[] parent1 = tournamentSelect(population, distances);
                int[] parent2 = tournamentSelect(population, distances);
                
                int[] child;
                if (rand.nextDouble() < crossoverRate) {
                    child = orderCrossover(parent1, parent2);
                } else {
                    child = (fitness(parent1, distances) < fitness(parent2, distances)) 
                            ? parent1.clone() : parent2.clone();
                }
                
                if (rand.nextDouble() < mutationRate) {
                    mutate(child);
                }
                
                // 2-opt local search na 20% offspring-a
                if (rand.nextDouble() < 0.2) {
                    child = twoOpt(child, distances);
                }
                
                newPop.add(child);
            }

            population = newPop;
        }
        
        // Finalni 2-opt na best solution
        bestTour = twoOpt(bestTour, distances);
        bestCost = fitness(bestTour, distances);

        return new Result(bestCost, tourToList(bestTour));
    }
    
    // 2-opt local search
    private static int[] twoOpt(int[] tour, double[][] d) {
        int n = tour.length;
        int[] best = tour.clone();
        boolean improved = true;
        int maxIter = 100;
        int iter = 0;
        
        while (improved && iter++ < maxIter) {
            improved = false;
            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 2; j < n; j++) {
                    // Calculate delta
                    int a = best[i];
                    int b = best[i + 1];
                    int c = best[j];
                    int dn = best[(j + 1) % n];
                    
                    double delta = (d[a][c] + d[b][dn]) - (d[a][b] + d[c][dn]);
                    
                    if (delta < -1e-10) {
                        // Reverse segment [i+1, j]
                        for (int l = i + 1, r = j; l < r; l++, r--) {
                            int tmp = best[l];
                            best[l] = best[r];
                            best[r] = tmp;
                        }
                        improved = true;
                    }
                }
            }
        }
        return best;
    }

    private static int[] randomTour(int n) {
        int[] tour = new int[n];
        for (int i = 0; i < n; i++) tour[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = tour[i];
            tour[i] = tour[j];
            tour[j] = temp;
        }
        return tour;
    }

    private static double fitness(int[] tour, double[][] d) {
        double cost = 0;
        for (int i = 0; i < tour.length - 1; i++) {
            cost += d[tour[i]][tour[i + 1]];
        }
        cost += d[tour[tour.length - 1]][tour[0]];
        return cost;
    }

    private static int[] tournamentSelect(List<int[]> pop, double[][] d) {
        int[] best = pop.get(rand.nextInt(pop.size()));
        double bestFit = fitness(best, d);
        for (int i = 0; i < 2; i++) {
            int[] comp = pop.get(rand.nextInt(pop.size()));
            double compFit = fitness(comp, d);
            if (compFit < bestFit) {
                best = comp;
                bestFit = compFit;
            }
        }
        return best;
    }

    private static int[] orderCrossover(int[] p1, int[] p2) {
        int n = p1.length;
        int[] child = new int[n];
        Arrays.fill(child, -1);
        
        int start = rand.nextInt(n);
        int end = rand.nextInt(n);
        if (start > end) { int t = start; start = end; end = t; }
        
        Set<Integer> used = new HashSet<>();
        for (int i = start; i <= end; i++) {
            child[i] = p1[i];
            used.add(p1[i]);
        }
        
        int idx = (end + 1) % n;
        for (int i = 0; i < n; i++) {
            int gene = p2[(end + 1 + i) % n];
            if (!used.contains(gene)) {
                child[idx] = gene;
                idx = (idx + 1) % n;
            }
        }
        return child;
    }

    private static void mutate(int[] tour) {
        int i = rand.nextInt(tour.length);
        int j = rand.nextInt(tour.length);
        int temp = tour[i];
        tour[i] = tour[j];
        tour[j] = temp;
    }

    private static List<Integer> tourToList(int[] tour) {
        List<Integer> list = new ArrayList<>();
        for (int node : tour) list.add(node);
        list.add(tour[0]);
        return list;
    }
    
    public static Result solve(Graph g) {
        return solve(g, 100, 100, 0.3, 0);
    }
}
