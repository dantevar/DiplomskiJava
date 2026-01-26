package metaheuristika;

import java.util.*;
import utils.*;


public class GA {


    private static final Random rand = new Random();

    // Overload bez crossoverRate - koristi default 0.70 (optimalni iz grid search-a)
    public static Result solve(Graph g, int popSize, int generations, double mutationRate, int printEvery) {
        return solve(g, popSize, generations, mutationRate, 0.70, printEvery);
    }

    // Glavna metoda s crossoverRate parametrom
    public static Result solve(Graph g, int popSize, int generations, double mutationRate, double crossoverRate, int printEvery) {
        int n = g.n;
        double[][] distances = g.min_distances;
        if (n <= 1) return new Result(0, Arrays.asList(0));

        // Initialize population (random permutations)
        List<int[]> population = new ArrayList<>();
        for (int i = 0; i < popSize; i++) {
            population.add(randomTour(n));
        }

        int[] bestTour = population.get(0);
        double bestCost = fitness(bestTour, distances);

        // Evolution
        for (int gen = 0; gen < generations; gen++) {
            if(printEvery > 0 && gen % printEvery == 0) {
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
            int eliteCount = popSize / 10;
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
                    // Bez crossovera - kopiramo boljeg roditelja
                    child = (fitness(parent1, distances) < fitness(parent2, distances)) 
                            ? parent1.clone() : parent2.clone();
                }
                
                if (rand.nextDouble() < mutationRate) {
                    mutate(child);
                }
                
                newPop.add(child);
            }

            population = newPop;
        }

        return new Result(bestCost, tourToList(bestTour));
    }



    // Random tour (permutation of 0..n-1)
    private static int[] randomTour(int n) {
        int[] tour = new int[n];
        for (int i = 0; i < n; i++) tour[i] = i;
        
        // Fisher-Yates shuffle
        for (int i = n - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = tour[i];
            tour[i] = tour[j];
            tour[j] = temp;
        }
        return tour;
    }

    // Fitness = total tour cost using min_distances (Floyd-Warshall)
    private static double fitness(int[] tour, double[][] minDist) {
        double cost = 0;
        for (int i = 0; i < tour.length - 1; i++) {
            cost += minDist[tour[i]][tour[i + 1]];
        }
        cost += minDist[tour[tour.length - 1]][tour[0]]; // Return to start
        return cost;
    }

    // Tournament selection (pick best of 3 random individuals)
    private static int[] tournamentSelect(List<int[]> pop, double[][] dist) {
        int[] best = pop.get(rand.nextInt(pop.size()));
        double bestFit = fitness(best, dist);
        
        for (int i = 0; i < 2; i++) {
            int[] candidate = pop.get(rand.nextInt(pop.size()));
            double fit = fitness(candidate, dist);
            if (fit < bestFit) {
                best = candidate;
                bestFit = fit;
            }
        }
        return best.clone();
    }

    // Order Crossover (OX)
    private static int[] orderCrossover(int[] p1, int[] p2) {
        int n = p1.length;
        int[] child = new int[n];
        Arrays.fill(child, -1);

        // Random segment from parent1
        int start = rand.nextInt(n);
        int end = rand.nextInt(n);
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        // Copy segment
        for (int i = start; i <= end; i++) {
            child[i] = p1[i];
        }

        // Fill remaining from parent2
        int pos = (end + 1) % n;
        for (int i = 0; i < n; i++) {
            int gene = p2[(end + 1 + i) % n];
            if (!contains(child, gene)) {
                child[pos] = gene;
                pos = (pos + 1) % n;
            }
        }

        return child;
    }

    // Swap mutation
    private static void mutate(int[] tour) {
        int i = rand.nextInt(tour.length);
        int j = rand.nextInt(tour.length);
        int temp = tour[i];
        tour[i] = tour[j];
        tour[j] = temp;
    }

    private static boolean contains(int[] arr, int val) {
        for (int v : arr) if (v == val) return true;
        return false;
    }

    private static List<Integer> tourToList(int[] tour) {
        List<Integer> list = new ArrayList<>();
        for (int v : tour) list.add(v);
        list.add(tour[0]); // Close the loop
        return list;
    }
}
