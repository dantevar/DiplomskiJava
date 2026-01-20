package utils;

import java.util.List;

/**
 * Rezultat sa više kriterija za višekriterijsku optimizaciju
 */
public class MultiObjectiveResult implements Comparable<MultiObjectiveResult> {
    
    public final double totalCost;           // Ukupna cijena (originalni kriterij)
    public final int edgeCount;              // Broj prijeđenih bridova
    public final double maxEdgeWeight;       // Maksimalna težina pojedinog brida
    public final double variance;            // Varijanca težina bridova
    public final int vertexRepetitions;      // Broj ponavljanja vrhova
    public final List<Integer> tour;         // Slijed vrhova
    
    // Pareto dominacija rank (0 = ne-dominiran)
    public int paretoRank = 0;
    
    // Crowding distance za sortiranje unutar ranka
    public double crowdingDistance = 0.0;
    
    public MultiObjectiveResult(double totalCost, int edgeCount, double maxEdgeWeight, 
                                double variance, int vertexRepetitions, List<Integer> tour) {
        this.totalCost = totalCost;
        this.edgeCount = edgeCount;
        this.maxEdgeWeight = maxEdgeWeight;
        this.variance = variance;
        this.vertexRepetitions = vertexRepetitions;
        this.tour = tour;
    }
    
    /**
     * Provjera da li ovo rješenje Pareto dominira drugo rješenje.
     * Rješenje A dominira B ako je A barem jednako dobro u svim kriterijima,
     * i striktno bolje u barem jednom kriteriju.
     */
    public boolean dominates(MultiObjectiveResult other) {
        boolean strictlyBetter = false;
        
        // Svi kriteriji su za minimizaciju
        if (this.totalCost < other.totalCost) strictlyBetter = true;
        else if (this.totalCost > other.totalCost) return false;
        
        if (this.edgeCount < other.edgeCount) strictlyBetter = true;
        else if (this.edgeCount > other.edgeCount) return false;
        
        if (this.maxEdgeWeight < other.maxEdgeWeight) strictlyBetter = true;
        else if (this.maxEdgeWeight > other.maxEdgeWeight) return false;
        
        if (this.variance < other.variance) strictlyBetter = true;
        else if (this.variance > other.variance) return false;
        
        if (this.vertexRepetitions < other.vertexRepetitions) strictlyBetter = true;
        else if (this.vertexRepetitions > other.vertexRepetitions) return false;
        
        return strictlyBetter;
    }
    
    @Override
    public int compareTo(MultiObjectiveResult other) {
        // Prvo po Pareto ranku, zatim po crowding distance (veći je bolji)
        if (this.paretoRank != other.paretoRank) {
            return Integer.compare(this.paretoRank, other.paretoRank);
        }
        return Double.compare(other.crowdingDistance, this.crowdingDistance);
    }
    
    @Override
    public String toString() {
        return String.format("Cost=%.2f, Edges=%d, MaxWeight=%.2f, Var=%.2f, Reps=%d, Rank=%d, CD=%.2f",
                totalCost, edgeCount, maxEdgeWeight, variance, vertexRepetitions, paretoRank, crowdingDistance);
    }
}
