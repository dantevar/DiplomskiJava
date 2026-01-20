package metaheuristika;

import java.util.*;
import utils.*;

/**
 * CMA-ES (Covariance Matrix Adaptation Evolution Strategy) za MCW
 * 
 * Encoding: Walk Construction Encoding
 * - Continuous vector x ∈ ℝ^n
 * - Dekodiranje: x[i] bira sljedeći čvor iz remaining nodes
 * 
 * CMA-ES je continuous optimization algoritam adaptiran za diskretni MCW problem.
 * Koristi multivariate normal distribuciju koja se adaptira prema geometriji
 * landscape-a kroz covariance matrix.
 * 
 * Napomena: Ovaj pristup je eksperimentalan - CMA-ES nije prirodan za kombinatorne probleme.
 * Za bolje rezultate koristiti GA, SA ili Memetic algoritme.
 */
public class CMAES {
    
    private static final Random rand = new Random();
    
    /**
     * Solve with default parameters
     */
    public static Result solve(Graph g) {
        return solve(g, 100, 0);
    }
    
    /**
     * CMA-ES solver for MCW
     * 
     * @param g Graph instance
     * @param maxGenerations Maximum number of generations
     * @param printEvery Print progress (0 = no print)
     * @return Best solution found
     */
    public static Result solve(Graph g, int maxGenerations, int printEvery) {
        int n = g.n;
        int d = n; // Dimension of search space
        
        // CMA-ES population size
        int lambda = 4 + (int)(3 * Math.log(d));
        int mu = lambda / 2;
        
        // Initialize mean vector (centered at 0.5)
        double[] m = new double[d];
        Arrays.fill(m, 0.5);
        
        // Step size
        double sigma = 0.3;
        
        // Covariance matrix (start with identity)
        double[][] C = identityMatrix(d);
        
        // Evolution paths
        double[] pc = new double[d];
        double[] psigma = new double[d];
        
        // Weights for recombination
        double[] weights = computeWeights(mu);
        double mueff = 1.0 / Arrays.stream(weights).map(w -> w * w).sum();
        
        // Learning rates
        double cc = (4.0 + mueff / d) / (d + 4.0 + 2.0 * mueff / d);
        double cs = (mueff + 2.0) / (d + mueff + 5.0);
        double c1 = 2.0 / ((d + 1.3) * (d + 1.3) + mueff);
        double cmu = Math.min(1 - c1, 2.0 * (mueff - 2.0 + 1.0 / mueff) / ((d + 2.0) * (d + 2.0) + mueff));
        double damps = 1.0 + 2.0 * Math.max(0, Math.sqrt((mueff - 1.0) / (d + 1.0)) - 1.0) + cs;
        
        // Expected value of ||N(0,I)||
        double chiN = Math.sqrt(d) * (1.0 - 1.0 / (4.0 * d) + 1.0 / (21.0 * d * d));
        
        // Best solution tracking
        List<Integer> bestWalk = null;
        double bestCost = Double.MAX_VALUE;
        
        // === MAIN CMA-ES LOOP ===
        for (int gen = 0; gen < maxGenerations; gen++) {
            // === 1. SAMPLING ===
            List<Candidate> candidates = new ArrayList<>();
            
            for (int i = 0; i < lambda; i++) {
                // Sample from N(m, σ²C)
                double[] x = sampleMultivariateNormal(m, sigma, C);
                
                // Decode to walk using Walk Construction Encoding
                List<Integer> walk = decodeToWalk(x, g);
                
                // Evaluate
                double cost = evaluateWalk(walk, g.distance_matrix);
                
                candidates.add(new Candidate(x, walk, cost));
            }
            
            // === 2. SELECTION ===
            // Sort by fitness (ascending - lower cost is better)
            candidates.sort(Comparator.comparingDouble(c -> c.cost));
            
            // Update global best
            if (candidates.get(0).cost < bestCost) {
                bestCost = candidates.get(0).cost;
                bestWalk = new ArrayList<>(candidates.get(0).walk);
            }
            
            if (printEvery > 0 && gen % printEvery == 0) {
                double avgCost = candidates.stream()
                    .mapToDouble(c -> c.cost)
                    .average()
                    .orElse(0.0);
                System.out.printf("Gen %d: Best=%.4f, Avg=%.4f, Sigma=%.4f, BestLen=%d%n",
                    gen, bestCost, avgCost, sigma, bestWalk.size());
            }
            
            // === 3. RECOMBINATION ===
            double[] m_old = m.clone();
            Arrays.fill(m, 0.0);
            
            // Weighted average of top mu candidates
            for (int i = 0; i < mu; i++) {
                for (int j = 0; j < d; j++) {
                    m[j] += weights[i] * candidates.get(i).x[j];
                }
            }
            
            // === 4. ADAPTATION ===
            
            // Update evolution path for sigma
            double[] diff = subtract(m, m_old);
            double[] Cinvsqrt_diff = multiplyMatrixVector(matrixInverseSqrt(C), diff);
            
            for (int i = 0; i < d; i++) {
                psigma[i] = (1 - cs) * psigma[i] 
                    + Math.sqrt(cs * (2 - cs) * mueff) / sigma * Cinvsqrt_diff[i];
            }
            
            // Compute norm of psigma
            double normPsigma = norm(psigma);
            
            // Heaviside function
            double hsig = (normPsigma / Math.sqrt(1 - Math.pow(1 - cs, 2 * (gen + 1))) / chiN < 1.4 + 2.0 / (d + 1)) ? 1.0 : 0.0;
            
            // Update evolution path for C
            for (int i = 0; i < d; i++) {
                pc[i] = (1 - cc) * pc[i] 
                    + hsig * Math.sqrt(cc * (2 - cc) * mueff) / sigma * diff[i];
            }
            
            // Update covariance matrix
            double[][] C_old = copyMatrix(C);
            
            // Rank-one update
            double[][] pcpc = outerProduct(pc, pc);
            
            // Rank-mu update
            double[][] rankMuUpdate = new double[d][d];
            for (int k = 0; k < mu; k++) {
                double[] y = multiplyScalar(subtract(candidates.get(k).x, m_old), 1.0 / sigma);
                double[][] yy = outerProduct(y, y);
                rankMuUpdate = addMatrix(rankMuUpdate, multiplyScalar(yy, weights[k]));
            }
            
            // Combine updates
            for (int i = 0; i < d; i++) {
                for (int j = 0; j < d; j++) {
                    C[i][j] = (1 - c1 - cmu) * C_old[i][j]
                        + c1 * pcpc[i][j]
                        + cmu * rankMuUpdate[i][j];
                }
            }
            
            // Update step size
            sigma = sigma * Math.exp((cs / damps) * (normPsigma / chiN - 1));
            
            // Clamp sigma to reasonable bounds
            sigma = Math.max(0.001, Math.min(1.0, sigma));
        }
        
        return new Result(bestCost, bestWalk);
    }
    
    /**
     * Walk Construction Encoding: Decode continuous vector to walk
     * 
     * x[0] určuje početni čvor
     * x[i] (i>0) bira sljedeći čvor iz remaining nodes, preferirajući bliske čvorove
     */
    private static List<Integer> decodeToWalk(double[] x, Graph g) {
        int n = g.n;
        double[][] minDist = g.min_distances;
        
        List<Integer> walk = new ArrayList<>();
        Set<Integer> remaining = new HashSet<>();
        for (int i = 0; i < n; i++) remaining.add(i);
        
        // Start node determined by x[0]
        int start = (int)(Math.abs(x[0]) * n) % n;
        walk.add(start);
        remaining.remove(start);
        
        int current = start;
        
        // Build walk by selecting from remaining nodes
        for (int i = 1; i < n; i++) {
            if (remaining.isEmpty()) break;
            
            // Sort remaining nodes by distance from current
            final int currentNode = current; // Make effectively final for lambda
            List<Integer> sortedRemaining = new ArrayList<>(remaining);
            sortedRemaining.sort(Comparator.comparingDouble(node -> minDist[currentNode][node]));
            
            // Use x[i] to select from sorted list (with some randomness)
            double val = Math.abs(x[i % x.length]);
            
            // Bias towards closer nodes (using exponential distribution)
            // val ∈ [0, ∞) → index ∈ [0, remaining.size()-1]
            double biasedVal = 1.0 - Math.exp(-3.0 * val); // Bias towards 0
            int index = (int)(biasedVal * sortedRemaining.size()) % sortedRemaining.size();
            
            int next = sortedRemaining.get(index);
            walk.add(next);
            remaining.remove(next);
            current = next;
        }
        
        // Ensure all nodes are covered (safety)
        for (int node : remaining) {
            walk.add(node);
        }
        
        return walk;
    }
    
    /**
     * Sample from multivariate normal distribution N(m, σ²C)
     */
    private static double[] sampleMultivariateNormal(double[] mean, double sigma, double[][] C) {
        int d = mean.length;
        
        // Cholesky decomposition: C = L * L^T
        double[][] L = choleskyDecomposition(C);
        
        // Sample z ~ N(0, I)
        double[] z = new double[d];
        for (int i = 0; i < d; i++) {
            z[i] = rand.nextGaussian();
        }
        
        // x = m + σ * L * z
        double[] Lz = multiplyMatrixVector(L, z);
        double[] x = new double[d];
        for (int i = 0; i < d; i++) {
            x[i] = mean[i] + sigma * Lz[i];
        }
        
        return x;
    }
    
    /**
     * Evaluate walk fitness
     */
    private static double evaluateWalk(List<Integer> walk, double[][] distances) {
        if (walk.size() < 2) return Double.MAX_VALUE;
        
        int n = distances.length;
        Set<Integer> visited = new HashSet<>(walk);
        
        // Must cover all nodes
        if (visited.size() < n) {
            return Double.MAX_VALUE;
        }
        
        double cost = 0.0;
        for (int i = 0; i < walk.size() - 1; i++) {
            cost += distances[walk.get(i)][walk.get(i + 1)];
        }
        cost += distances[walk.get(walk.size() - 1)][walk.get(0)];
        
        return cost;
    }
    
    // === MATRIX OPERATIONS ===
    
    private static double[][] identityMatrix(int n) {
        double[][] I = new double[n][n];
        for (int i = 0; i < n; i++) {
            I[i][i] = 1.0;
        }
        return I;
    }
    
    private static double[] computeWeights(int mu) {
        double[] weights = new double[mu];
        double sum = 0.0;
        
        for (int i = 0; i < mu; i++) {
            weights[i] = Math.log(mu + 0.5) - Math.log(i + 1);
            sum += weights[i];
        }
        
        // Normalize
        for (int i = 0; i < mu; i++) {
            weights[i] /= sum;
        }
        
        return weights;
    }
    
    private static double[] subtract(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] - b[i];
        }
        return result;
    }
    
    private static double[] multiplyScalar(double[] v, double s) {
        double[] result = new double[v.length];
        for (int i = 0; i < v.length; i++) {
            result[i] = v[i] * s;
        }
        return result;
    }
    
    private static double norm(double[] v) {
        double sum = 0.0;
        for (double x : v) {
            sum += x * x;
        }
        return Math.sqrt(sum);
    }
    
    private static double[] multiplyMatrixVector(double[][] M, double[] v) {
        int n = M.length;
        double[] result = new double[n];
        
        for (int i = 0; i < n; i++) {
            result[i] = 0.0;
            for (int j = 0; j < n; j++) {
                result[i] += M[i][j] * v[j];
            }
        }
        
        return result;
    }
    
    private static double[][] outerProduct(double[] u, double[] v) {
        int n = u.length;
        double[][] result = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = u[i] * v[j];
            }
        }
        
        return result;
    }
    
    private static double[][] addMatrix(double[][] A, double[][] B) {
        int n = A.length;
        double[][] result = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = A[i][j] + B[i][j];
            }
        }
        
        return result;
    }
    
    private static double[][] multiplyScalar(double[][] M, double s) {
        int n = M.length;
        double[][] result = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = M[i][j] * s;
            }
        }
        
        return result;
    }
    
    private static double[][] copyMatrix(double[][] M) {
        int n = M.length;
        double[][] copy = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            System.arraycopy(M[i], 0, copy[i], 0, n);
        }
        
        return copy;
    }
    
    /**
     * Cholesky decomposition: A = L * L^T
     * Simplified version - assumes positive definite matrix
     */
    private static double[][] choleskyDecomposition(double[][] A) {
        int n = A.length;
        double[][] L = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= i; j++) {
                double sum = 0.0;
                
                if (j == i) {
                    for (int k = 0; k < j; k++) {
                        sum += L[j][k] * L[j][k];
                    }
                    L[j][j] = Math.sqrt(Math.max(0.0, A[j][j] - sum));
                } else {
                    for (int k = 0; k < j; k++) {
                        sum += L[i][k] * L[j][k];
                    }
                    if (L[j][j] > 0) {
                        L[i][j] = (A[i][j] - sum) / L[j][j];
                    }
                }
            }
        }
        
        return L;
    }
    
    /**
     * Compute inverse square root of matrix (simplified)
     */
    private static double[][] matrixInverseSqrt(double[][] M) {
        // Simplified: just return identity scaled by average diagonal
        int n = M.length;
        double avgDiag = 0.0;
        for (int i = 0; i < n; i++) {
            avgDiag += M[i][i];
        }
        avgDiag /= n;
        
        double scale = 1.0 / Math.sqrt(Math.max(0.001, avgDiag));
        
        double[][] result = identityMatrix(n);
        for (int i = 0; i < n; i++) {
            result[i][i] = scale;
        }
        
        return result;
    }
    
    /**
     * Candidate solution container
     */
    private static class Candidate {
        double[] x;           // Continuous representation
        List<Integer> walk;   // Decoded walk
        double cost;          // Fitness
        
        Candidate(double[] x, List<Integer> walk, double cost) {
            this.x = x;
            this.walk = walk;
            this.cost = cost;
        }
    }
}
