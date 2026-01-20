package utils;

import java.util.List;

/**
 * Skalarizirajuće funkcije za pretvaranje višekriterijske optimizacije 
 * u jednokritrijsku pomoću težinskih koeficijenata
 */
public class ScalarizationMethods {
    
    /**
     * Weighted Sum metoda - linearna kombinacija kriterija
     * 
     * @param result Rješenje za evaluaciju
     * @param weights Težine za svaki kriterij [wCost, wEdges, wMaxWeight, wVar, wReps]
     *                Suma težina treba biti 1.0
     * @return Skalarna vrijednost
     */
    public static double weightedSum(MultiObjectiveResult result, double[] weights) {
        // Normalizacija nije potrebna ako su svi kriteriji u istim jedinicama
        // ali ovdje možemo koristiti direktno jer su sve težine pozitivne
        
        return weights[0] * result.totalCost +
               weights[1] * result.edgeCount +
               weights[2] * result.maxEdgeWeight +
               weights[3] * result.variance +
               weights[4] * result.vertexRepetitions;
    }
    
    /**
     * Weighted Chebyshev metoda - minimizira maksimalno odstupanje od idealne točke
     * 
     * @param result Rješenje za evaluaciju
     * @param weights Težine za svaki kriterij
     * @param idealPoint Idealna točka (najbolje vrijednosti za svaki kriterij)
     * @return Skalarna vrijednost
     */
    public static double weightedChebyshev(MultiObjectiveResult result, 
                                           double[] weights, 
                                           double[] idealPoint) {
        double[] deviations = new double[5];
        
        deviations[0] = weights[0] * Math.abs(result.totalCost - idealPoint[0]);
        deviations[1] = weights[1] * Math.abs(result.edgeCount - idealPoint[1]);
        deviations[2] = weights[2] * Math.abs(result.maxEdgeWeight - idealPoint[2]);
        deviations[3] = weights[3] * Math.abs(result.variance - idealPoint[3]);
        deviations[4] = weights[4] * Math.abs(result.vertexRepetitions - idealPoint[4]);
        
        // Vraća maksimalno odstupanje
        double max = deviations[0];
        for (int i = 1; i < deviations.length; i++) {
            if (deviations[i] > max) {
                max = deviations[i];
            }
        }
        
        return max;
    }
    
    /**
     * Augmented Weighted Chebyshev - kombinacija Chebyshev i weighted sum
     * 
     * @param result Rješenje za evaluaciju
     * @param weights Težine za svaki kriterij
     * @param idealPoint Idealna točka
     * @param rho Parametar augmentacije (obično mali, npr. 0.001)
     * @return Skalarna vrijednost
     */
    public static double augmentedChebyshev(MultiObjectiveResult result, 
                                           double[] weights, 
                                           double[] idealPoint,
                                           double rho) {
        double chebyshev = weightedChebyshev(result, weights, idealPoint);
        double sum = weightedSum(result, weights);
        
        return chebyshev + rho * sum;
    }
    
    /**
     * Achievement Scalarizing Function (ASF) - generalizacija Chebyshev metode
     * 
     * @param result Rješenje za evaluaciju
     * @param weights Težine/željeni smjer pretrage
     * @param referencePoint Referentna točka (može biti idealna, nadir, ili specifirana)
     * @return Skalarna vrijednost
     */
    public static double achievementFunction(MultiObjectiveResult result, 
                                            double[] weights, 
                                            double[] referencePoint) {
        double maxDeviation = Double.NEGATIVE_INFINITY;
        double epsilon = 1e-6; // Mali epsilon da se izbjegne dijeljenje s nulom
        
        double[] objectives = {
            result.totalCost,
            result.edgeCount,
            result.maxEdgeWeight,
            result.variance,
            result.vertexRepetitions
        };
        
        for (int i = 0; i < objectives.length; i++) {
            double deviation = (objectives[i] - referencePoint[i]) / (weights[i] + epsilon);
            if (deviation > maxDeviation) {
                maxDeviation = deviation;
            }
        }
        
        return maxDeviation;
    }
    
    /**
     * Compromise Programming - minimizira Lp metriku od idealne točke
     * 
     * @param result Rješenje za evaluaciju
     * @param idealPoint Idealna točka
     * @param nadirPoint Nadir točka (najgore vrijednosti)
     * @param p Parametar metrike (1=Manhattan, 2=Euclidean, Inf=Chebyshev)
     * @return Skalarna vrijednost
     */
    public static double compromiseProgramming(MultiObjectiveResult result, 
                                              double[] idealPoint,
                                              double[] nadirPoint,
                                              double p) {
        double[] objectives = {
            result.totalCost,
            result.edgeCount,
            result.maxEdgeWeight,
            result.variance,
            result.vertexRepetitions
        };
        
        double sum = 0.0;
        
        for (int i = 0; i < objectives.length; i++) {
            // Normalizirano odstupanje
            double range = nadirPoint[i] - idealPoint[i];
            if (range > 0) {
                double normalized = (objectives[i] - idealPoint[i]) / range;
                sum += Math.pow(Math.abs(normalized), p);
            }
        }
        
        return Math.pow(sum, 1.0 / p);
    }
    
    /**
     * Preporučene težine za različite scenarije
     */
    public static class PresetWeights {
        // Naglasak na ukupnu cijenu (tradicionalni pristup)
        public static final double[] COST_FOCUSED = {0.6, 0.1, 0.1, 0.1, 0.1};
        
        // Balansiran pristup - sve jednako važno
        public static final double[] BALANCED = {0.2, 0.2, 0.2, 0.2, 0.2};
        
        // Naglasak na brzinu (minimizacija broja bridova)
        public static final double[] SPEED_FOCUSED = {0.2, 0.5, 0.1, 0.1, 0.1};
        
        // Ravnomjerna distribucija (minimizacija varijance)
        public static final double[] SMOOTH = {0.2, 0.1, 0.1, 0.5, 0.1};
        
        // Minimizacija maksimalne težine (min-max)
        public static final double[] MINMAX = {0.2, 0.1, 0.5, 0.1, 0.1};
    }
    
    /**
     * Izračunava idealna i nadir točke iz liste rješenja
     */
    public static class ReferencePoints {
        public double[] ideal;
        public double[] nadir;
        
        public ReferencePoints(List<MultiObjectiveResult> solutions) {
            ideal = new double[5];
            nadir = new double[5];
            
            ideal[0] = solutions.stream().mapToDouble(r -> r.totalCost).min().orElse(0);
            ideal[1] = solutions.stream().mapToInt(r -> r.edgeCount).min().orElse(0);
            ideal[2] = solutions.stream().mapToDouble(r -> r.maxEdgeWeight).min().orElse(0);
            ideal[3] = solutions.stream().mapToDouble(r -> r.variance).min().orElse(0);
            ideal[4] = solutions.stream().mapToInt(r -> r.vertexRepetitions).min().orElse(0);
            
            nadir[0] = solutions.stream().mapToDouble(r -> r.totalCost).max().orElse(0);
            nadir[1] = solutions.stream().mapToInt(r -> r.edgeCount).max().orElse(0);
            nadir[2] = solutions.stream().mapToDouble(r -> r.maxEdgeWeight).max().orElse(0);
            nadir[3] = solutions.stream().mapToDouble(r -> r.variance).max().orElse(0);
            nadir[4] = solutions.stream().mapToInt(r -> r.vertexRepetitions).max().orElse(0);
        }
    }
}
