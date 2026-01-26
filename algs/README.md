# 📚 Algoritmi za Minimum Closed Walk (MCW)

Ovaj folder sadrži detaljnu dokumentaciju i pseudokod svih algoritama implementiranih u projektu.

## 📋 Pregled algoritama

### Egzaktni algoritmi (garantiraju optimalno rješenje)

| # | Algoritam | Datoteka | Složenost | Praktični limit |
|---|-----------|----------|-----------|-----------------|
| 01 | [Held-Karp DP](01_HeldKarp.md) | `ClosedWalkSolver.java` | O(n² · 2ⁿ) | N ≤ 20 |
| 02 | [Branch & Bound](02_BranchAndBound.md) | `BruteForce.java` | O(n!) | N ≤ 15 |
| 03 | [MyAlg BFS](03_MyAlgBFS.md) | `MyAlg.java` | O(exp) | N ≤ 10 |

### Konstrukcijske heuristike (brze, približne)

| # | Algoritam | Datoteka | Složenost | Tipični gap |
|---|-----------|----------|-----------|-------------|
| 04 | [Greedy NN](04_GreedyNN.md) | `Greedy.java` | O(n²) | 15-30% |
| 05 | [ASPW](05_ASPW.md) | `ASPW.java` | O(n³) | 5-15% |

### Metaheuristike (napredne, iterativne)

| # | Algoritam | Datoteka | Tipični gap | Napomena |
|---|-----------|----------|-------------|----------|
| 06 | [Genetic Algorithm](06_GeneticAlgorithm.md) | `GA.java` | 5-12% | Evolucijski |
| 07 | [Simulated Annealing](07_SimulatedAnnealing.md) | `SA.java` | 5-15% | Trajectory |
| 08 | [Ant Colony Optimization](08_AntColonyOptimization.md) | `ACO.java` | 3-8% | Swarm |
| 09 | [Memetic GA-SA](09_MemeticGASA.md) | `MemeticGASA.java` | 2-5% | **Najbolji!** |
| 10 | [NSGA-II](10_NSGAII.md) | `NSGAII_MCW.java` | Pareto | Multi-objective |

## 🏆 Preporučeni algoritmi po scenariju

### Za mali N (≤ 20): Egzaktni
```
Held-Karp DP → garantira optimum
```

### Za srednji N (20-100): Metaheuristike
```
Memetic GA-SA > ACO > GA > SA
```

### Za veliki N (100+): Brze heuristike + Local Search
```
ASPW + 2-opt ili Greedy + 2-opt
```

### Za višekriterijsku optimizaciju
```
NSGA-II → vraća Pareto frontu
```

## 📊 Usporedba performansi (N=20)

| Algoritam | Avg Gap | Vrijeme | Memorija |
|-----------|---------|---------|----------|
| Held-Karp | 0% | 100-500ms | O(2ⁿ·n) |
| B&B | 0% | 1-60s | O(n) |
| Greedy | 20% | <1ms | O(n) |
| ASPW | 10% | 2-5ms | O(n²) |
| GA | 8% | 50-200ms | O(pop·n) |
| SA | 10% | 50-100ms | O(n) |
| ACO | 5% | 100-300ms | O(n²) |
| Memetic | **3%** | 200-500ms | O(pop·n) |

## 🔗 Veze

- [MULTIOBJECTIVE_README.md](../MULTIOBJECTIVE_README.md) - Višekriterijska optimizacija
- [notes](../notes) - Bilješke za diplomski rad
- [src/main/java/](../src/main/java/) - Implementacije
