package metaheuristika;

import java.util.*;
import utils.*;

/**
 * NSGA-II (Non-dominated Sorting Genetic Algorithm II) za višekriterijsku 
 * optimizaciju MCW problema.
 */
public class NSGAII_MCW {
    
    private final Graph graph;
    private final int populationSize;
    private final int maxGenerations;
    private final double mutationRate;
    private final double crossoverRate;
    private final Random random;
    
    public NSGAII_MCW(Graph graph, int populationSize, int maxGenerations, 
                      double mutationRate, double crossoverRate) {
        this.graph = graph;
        this.populationSize = populationSize;
        this.maxGenerations = maxGenerations;
        this.mutationRate = mutationRate;
        this.crossoverRate = crossoverRate;
        this.random = new Random();
    }
    
    /**
     * Glavna metoda koja pokreće NSGA-II algoritam
     * @return Pareto fronta (skup ne-dominiranih rješenja)
     */
    public List<MultiObjectiveResult> optimize() {
        // Inicijalizacija populacije
        List<MultiObjectiveResult> population = initializePopulation();
        
        for (int gen = 0; gen < maxGenerations; gen++) {
            // Kreiranje potomaka
            List<MultiObjectiveResult> offspring = createOffspring(population);
            
            // Spajanje roditelja i potomaka
            List<MultiObjectiveResult> combined = new ArrayList<>(population);
            combined.addAll(offspring);
            
            // Non-dominated sorting
            List<List<MultiObjectiveResult>> fronts = nonDominatedSort(combined);
            
            // Selekcija nove populacije
            population = selectNextGeneration(fronts);
            
            // Ispis napretka
            if ((gen + 1) % 10 == 0) {
                System.out.printf("Generacija %d: Pareto fronta veličine %d%n", 
                                gen + 1, fronts.get(0).size());
            }
        }
        
        // Vraćanje Pareto fronte (prve fronte)
        return nonDominatedSort(population).get(0);
    }
    
    /**
     * Inicijalizacija početne populacije nasumičnim rješenjima
     */
    private List<MultiObjectiveResult> initializePopulation() {
        List<MultiObjectiveResult> population = new ArrayList<>();
        
        for (int i = 0; i < populationSize; i++) {
            List<Integer> tour = generateRandomTour();
            MultiObjectiveResult result = evaluateTour(tour);
            population.add(result);
        }
        
        return population;
    }
    
    /**
     * Generira nasumičnu šetnju kroz sve vrhove
     */
    private List<Integer> generateRandomTour() {
        int n = graph.n;
        List<Integer> vertices = new ArrayList<>();
        for (int i = 1; i < n; i++) {
            vertices.add(i);
        }
        Collections.shuffle(vertices, random);
        
        List<Integer> tour = new ArrayList<>();
        tour.add(0); // Počinje od vrha 0
        tour.addAll(vertices);
        tour.add(0); // Vraća se na vrh 0
        
        return tour;
    }
    
    /**
     * Evaluacija ture prema svim kriterijima
     */
    private MultiObjectiveResult evaluateTour(List<Integer> tour) {
        double totalCost = 0.0;
        int edgeCount = 0;
        double maxEdgeWeight = 0.0;
        List<Double> edgeWeights = new ArrayList<>();
        Set<Integer> visitedVertices = new HashSet<>();
        int vertexRepetitions = 0;
        
        for (int i = 0; i < tour.size() - 1; i++) {
            int from = tour.get(i);
            int to = tour.get(i + 1);
            double weight = graph.min_distances[from][to];
            
            totalCost += weight;
            edgeCount++;
            maxEdgeWeight = Math.max(maxEdgeWeight, weight);
            edgeWeights.add(weight);
            
            if (visitedVertices.contains(to)) {
                vertexRepetitions++;
            }
            visitedVertices.add(to);
        }
        
        // Računa varijancu
        double variance = calculateVariance(edgeWeights);
        
        return new MultiObjectiveResult(totalCost, edgeCount, maxEdgeWeight, 
                                       variance, vertexRepetitions, tour);
    }
    
    /**
     * Računa varijancu liste vrijednosti
     */
    private double calculateVariance(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0.0);
        
        return variance;
    }
    
    /**
     * Non-dominated sorting - razvrstavanje rješenja po Pareto frontama
     */
    private List<List<MultiObjectiveResult>> nonDominatedSort(List<MultiObjectiveResult> population) {
        List<List<MultiObjectiveResult>> fronts = new ArrayList<>();
        int n = population.size();
        int[] dominationCount = new int[n];
        @SuppressWarnings("unchecked")
        List<Integer>[] dominatedSolutions = new List[n];
        
        // Mapa za praćenje indeksa rješenja
        Map<MultiObjectiveResult, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            dominatedSolutions[i] = new ArrayList<>();
            indexMap.put(population.get(i), i);
        }
        
        // Pronađi dominacijske odnose
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (population.get(i).dominates(population.get(j))) {
                    dominatedSolutions[i].add(j);
                    dominationCount[j]++;
                } else if (population.get(j).dominates(population.get(i))) {
                    dominatedSolutions[j].add(i);
                    dominationCount[i]++;
                }
            }
        }
        
        // Prva fronta - ne-dominirana rješenja
        List<MultiObjectiveResult> firstFront = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (dominationCount[i] == 0) {
                population.get(i).paretoRank = 0;
                firstFront.add(population.get(i));
            }
        }
        fronts.add(firstFront);
        
        // Ostale fronte
        int currentRank = 0;
        while (currentRank < fronts.size() && !fronts.get(currentRank).isEmpty()) {
            List<MultiObjectiveResult> nextFront = new ArrayList<>();
            for (MultiObjectiveResult solution : fronts.get(currentRank)) {
                Integer idx = indexMap.get(solution);
                if (idx != null && idx < n) {
                    for (int dominated : dominatedSolutions[idx]) {
                        dominationCount[dominated]--;
                        if (dominationCount[dominated] == 0) {
                            population.get(dominated).paretoRank = currentRank + 1;
                            nextFront.add(population.get(dominated));
                        }
                    }
                }
            }
            currentRank++;
            if (!nextFront.isEmpty()) {
                fronts.add(nextFront);
            }
        }
        
        // Izračunaj crowding distance za svaku frontu
        for (List<MultiObjectiveResult> front : fronts) {
            calculateCrowdingDistance(front);
        }
        
        return fronts;
    }
    
    /**
     * Izračunava crowding distance za rješenja u istoj fronti
     */
    private void calculateCrowdingDistance(List<MultiObjectiveResult> front) {
        int size = front.size();
        if (size <= 2) {
            for (MultiObjectiveResult sol : front) {
                sol.crowdingDistance = Double.POSITIVE_INFINITY;
            }
            return;
        }
        
        // Inicijalizacija
        for (MultiObjectiveResult sol : front) {
            sol.crowdingDistance = 0.0;
        }
        
        // Za svaki kriterij
        calculateCrowdingForObjective(front, r -> r.totalCost);
        calculateCrowdingForObjective(front, r -> (double) r.edgeCount);
        calculateCrowdingForObjective(front, r -> r.maxEdgeWeight);
        calculateCrowdingForObjective(front, r -> r.variance);
        calculateCrowdingForObjective(front, r -> (double) r.vertexRepetitions);
    }
    
    private void calculateCrowdingForObjective(List<MultiObjectiveResult> front, 
                                               java.util.function.ToDoubleFunction<MultiObjectiveResult> extractor) {
        front.sort(Comparator.comparingDouble(extractor));
        
        double minValue = extractor.applyAsDouble(front.get(0));
        double maxValue = extractor.applyAsDouble(front.get(front.size() - 1));
        double range = maxValue - minValue;
        
        if (range == 0) return;
        
        front.get(0).crowdingDistance = Double.POSITIVE_INFINITY;
        front.get(front.size() - 1).crowdingDistance = Double.POSITIVE_INFINITY;
        
        for (int i = 1; i < front.size() - 1; i++) {
            double distance = (extractor.applyAsDouble(front.get(i + 1)) - 
                             extractor.applyAsDouble(front.get(i - 1))) / range;
            front.get(i).crowdingDistance += distance;
        }
    }
    
    /**
     * Kreiranje potomaka pomoću crossover i mutacije
     */
    private List<MultiObjectiveResult> createOffspring(List<MultiObjectiveResult> population) {
        List<MultiObjectiveResult> offspring = new ArrayList<>();
        
        while (offspring.size() < populationSize) {
            // Tournament selekcija roditelja
            MultiObjectiveResult parent1 = tournamentSelection(population);
            MultiObjectiveResult parent2 = tournamentSelection(population);
            
            // Crossover
            List<Integer> childTour;
            if (random.nextDouble() < crossoverRate) {
                childTour = orderCrossover(parent1.tour, parent2.tour);
            } else {
                childTour = new ArrayList<>(parent1.tour);
            }
            
            // Mutacija
            if (random.nextDouble() < mutationRate) {
                mutate(childTour);
            }
            
            // Evaluacija potomka
            MultiObjectiveResult child = evaluateTour(childTour);
            offspring.add(child);
        }
        
        return offspring;
    }
    
    /**
     * Tournament selekcija - odabir boljeg od dva nasumična rješenja
     */
    private MultiObjectiveResult tournamentSelection(List<MultiObjectiveResult> population) {
        MultiObjectiveResult candidate1 = population.get(random.nextInt(population.size()));
        MultiObjectiveResult candidate2 = population.get(random.nextInt(population.size()));
        
        return candidate1.compareTo(candidate2) < 0 ? candidate1 : candidate2;
    }
    
    /**
     * Order crossover (OX) operator za permutacije
     */
    private List<Integer> orderCrossover(List<Integer> parent1, List<Integer> parent2) {
        int size = parent1.size();
        List<Integer> child = new ArrayList<>(Collections.nCopies(size, -1));
        
        // Kopiraj prvi i zadnji element (uvijek 0)
        child.set(0, 0);
        child.set(size - 1, 0);
        
        // Odaberi segment iz parent1
        int start = 1 + random.nextInt(size - 3);
        int end = start + random.nextInt(size - start - 1);
        
        for (int i = start; i <= end; i++) {
            child.set(i, parent1.get(i));
        }
        
        // Popuni preostale iz parent2
        Set<Integer> used = new HashSet<>(child.subList(start, end + 1));
        int pos = (end + 1) % (size - 1);
        if (pos == 0) pos = 1;
        
        for (int i = 1; i < size - 1; i++) {
            int gene = parent2.get(i);
            if (!used.contains(gene)) {
                child.set(pos, gene);
                pos++;
                if (pos == size - 1) pos = 1;
            }
        }
        
        return child;
    }
    
    /**
     * Swap mutacija - zamjena dva nasumična grada
     */
    private void mutate(List<Integer> tour) {
        int size = tour.size();
        int idx1 = 1 + random.nextInt(size - 2);
        int idx2 = 1 + random.nextInt(size - 2);
        
        Collections.swap(tour, idx1, idx2);
    }
    
    /**
     * Selekcija nove generacije iz fronta
     */
    private List<MultiObjectiveResult> selectNextGeneration(List<List<MultiObjectiveResult>> fronts) {
        List<MultiObjectiveResult> nextGen = new ArrayList<>();
        
        for (List<MultiObjectiveResult> front : fronts) {
            if (nextGen.size() + front.size() <= populationSize) {
                nextGen.addAll(front);
            } else {
                // Sortiraj po crowding distance i dodaj najbolje
                front.sort(Comparator.comparingDouble((MultiObjectiveResult r) -> r.crowdingDistance).reversed());
                int remaining = populationSize - nextGen.size();
                nextGen.addAll(front.subList(0, remaining));
                break;
            }
        }
        
        return nextGen;
    }
}
