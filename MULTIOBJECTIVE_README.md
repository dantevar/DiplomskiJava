# Višekriterijska Optimizacija MCW Problema

## Pregled

Ovaj projekt proširuje tradicionalni Minimum Closed Walk (MCW) problem s **višekriterijskom optimizacijom** koristeći **NSGA-II algoritam** (Non-dominated Sorting Genetic Algorithm II).

## Kriteriji Optimizacije

Tradicionalni MCW problem minimizira samo ukupnu cijenu puta. Naš pristup optimizira **5 kriterija istovremeno**:

1. **Ukupna cijena** (`totalCost`) - tradicionalni kriterij, suma svih težina bridova
2. **Broj bridova** (`edgeCount`) - minimizacija duljine puta
3. **Maksimalna težina brida** (`maxEdgeWeight`) - min-max optimizacija
4. **Varijanca težina** (`variance`) - ravnomjerna distribucija težina
5. **Broj ponavljanja vrhova** (`vertexRepetitions`) - minimizacija redundancije

## Implementirane Metode

### 1. NSGA-II (Primary Approach)

**Klasa:** `NSGAII_MCW`

NSGA-II je state-of-the-art evolucijski algoritam za višekriterijsku optimizaciju. Vraća **Pareto frontu** - skup ne-dominiranih rješenja.

**Karakteristike:**
- Pareto dominacija i non-dominated sorting
- Crowding distance za diversifikaciju
- Tournament selekcija
- Order crossover (OX) i swap mutacija
- Elitizam

**Primjer korištenja:**
```java
Graph graph = InstanceLoader.loadInstance("data/n10/instance_0.txt");

NSGAII_MCW nsgaii = new NSGAII_MCW(
    graph,
    100,    // Veličina populacije
    100,    // Broj generacija
    0.1,    // Stopa mutacije
    0.8     // Stopa crossover-a
);

List<MultiObjectiveResult> paretoFront = nsgaii.optimize();
```

### 2. Scalarization Methods (Secondary Approach)

**Klasa:** `ScalarizationMethods`

Pretvaranje višekriterijskog problema u jednokriterjski pomoću scalarizing funkcija.

**Dostupne metode:**

#### a) Weighted Sum
Linearna kombinacija kriterija s težinama:
```java
double score = ScalarizationMethods.weightedSum(result, weights);
```

**Preddefinirane težine:**
- `COST_FOCUSED` - naglasak na ukupnu cijenu (60%)
- `BALANCED` - svi kriteriji jednako važni (20% svaki)
- `SPEED_FOCUSED` - minimizacija broja bridova (50%)
- `SMOOTH` - minimizacija varijance (50%)
- `MINMAX` - minimizacija maksimalne težine (50%)

#### b) Weighted Chebyshev
Minimizira maksimalno odstupanje od idealne točke:
```java
double score = ScalarizationMethods.weightedChebyshev(result, weights, idealPoint);
```

#### c) Achievement Scalarizing Function (ASF)
Generalizacija Chebyshev metode:
```java
double score = ScalarizationMethods.achievementFunction(result, weights, referencePoint);
```

#### d) Compromise Programming
Koristi Lp metriku:
```java
double score = ScalarizationMethods.compromiseProgramming(result, idealPoint, nadirPoint, p);
```

## Struktura Klasa

```
utils/
├── MultiObjectiveResult.java    - Rezultat s više kriterija
├── ScalarizationMethods.java    - Scalarizing funkcije
├── ParetoFrontVisualizer.java   - Vizualizacija i export
└── Graph.java, Result.java      - Postojeće util klase

metaheuristika/
├── NSGAII_MCW.java              - NSGA-II implementacija
└── ACO.java                     - Postojeći ACO algoritam

fer/
└── MultiObjectiveMCWExample.java - Primjer korištenja
```

## Pokretanje

### 1. Osnovni primjer s NSGA-II

```bash
mvn compile exec:java -Dexec.mainClass="fer.MultiObjectiveMCWExample"
```

### 2. Vlastiti kod

```java
// Učitaj graf
Graph graph = InstanceLoader.loadInstance("data/n10/instance_0.txt");

// Pokreni NSGA-II
NSGAII_MCW nsgaii = new NSGAII_MCW(graph, 100, 100, 0.1, 0.8);
List<MultiObjectiveResult> paretoFront = nsgaii.optimize();

// Prikaži statistiku
ParetoFrontVisualizer.printStatistics(paretoFront);

// Izvezi u CSV
ParetoFrontVisualizer.exportToCSV(paretoFront, "pareto_front.csv");

// Kreiraj HTML vizualizaciju
ParetoFrontVisualizer.generateHTML(paretoFront, "pareto_front.html");
```

## Analiza Rezultata

### 1. Pareto Fronta

Pareto fronta sadrži sva **ne-dominirana rješenja** - rješenja gdje ne postoji drugo rješenje koje je bolje u svim kriterijima.

**Svojstva:**
- Svako rješenje u fronti je optimalno s nekog stanovišta
- Korisnik može izabrati rješenje prema svojim preferencama
- Trade-off-ovi između kriterija su vidljivi

### 2. Selekcija Najboljeg Rješenja

Iz Pareto fronte možete odabrati rješenje na nekoliko načina:

#### a) Prema jednom kriteriju
```java
// Minimalna ukupna cijena
MultiObjectiveResult best = Collections.min(paretoFront, 
    Comparator.comparingDouble(r -> r.totalCost));
```

#### b) Prema scalarizing funkciji
```java
// Weighted sum s custom težinama
double[] myWeights = {0.4, 0.2, 0.2, 0.1, 0.1};
MultiObjectiveResult best = Collections.min(paretoFront,
    Comparator.comparingDouble(r -> ScalarizationMethods.weightedSum(r, myWeights)));
```

#### c) Najbliže idealnoj točki
```java
ScalarizationMethods.ReferencePoints refs = 
    new ScalarizationMethods.ReferencePoints(paretoFront);

MultiObjectiveResult best = Collections.min(paretoFront,
    Comparator.comparingDouble(r -> 
        ScalarizationMethods.compromiseProgramming(r, refs.ideal, refs.nadir, 2)));
```

## Parametri NSGA-II

| Parametar | Opis | Preporučena Vrijednost |
|-----------|------|------------------------|
| `populationSize` | Veličina populacije | 50-200 |
| `maxGenerations` | Broj generacija | 100-500 |
| `mutationRate` | Vjerojatnost mutacije | 0.05-0.2 |
| `crossoverRate` | Vjerojatnost crossover-a | 0.7-0.9 |

**Upute za podešavanje:**
- Veći problem (više vrhova) → veća populacija i više generacija
- Složenija geometrija → viša stopa mutacije
- Brža konvergencija → veća stopa crossover-a

## Vizualizacija

### CSV Export
```java
ParetoFrontVisualizer.exportToCSV(paretoFront, "results.csv");
```
Otvori u Excel ili Python (pandas) za detaljnu analizu.

### HTML Grafovi
```java
ParetoFrontVisualizer.generateHTML(paretoFront, "visualization.html");
```
Interaktivni grafovi s Plotly.js - otvori u browseru.

## Primjeri Korištenja

### 1. Ušteđivanje goriva (minimizacija varijance)
Ravnomjerna brzina → manja potrošnja:
```java
double[] weights = ScalarizationMethods.PresetWeights.SMOOTH;
```

### 2. Hitne dostave (minimizacija max težine)
Izbjegavanje ekstremno dugih bridova:
```java
double[] weights = ScalarizationMethods.PresetWeights.MINMAX;
```

### 3. Turistička tura (balans svih kriterija)
```java
double[] weights = ScalarizationMethods.PresetWeights.BALANCED;
```

## Usporedba s Single-Objective

Tradicionalni Held-Karp algoritam daje **jedno optimalno rješenje** za ukupnu cijenu.

NSGA-II daje **Pareto frontu** - skup različitih kompromisa:

| Pristup | Prednosti | Nedostaci |
|---------|-----------|-----------|
| **Held-Karp** | Brz, garantirano optimalan za cijenu | Ignorira druge kriterije |
| **NSGA-II** | Mnoštvo opcija, fleksibilnost | Sporiji, heuristički |

**Hybrid pristup:** Koristite Held-Karp za brzu inicijalnu soluciju, zatim NSGA-II za istraživanje trade-off-ova.

## Performance

**Složenost:**
- Held-Karp: O(2^n * n^2)
- NSGA-II: O(P * G * N^2 * M) gdje je P=populacija, G=generacije, N=populacija, M=kriteriji

**Empirijska vremena** (Intel i7, n=10):
- Held-Karp: ~50ms
- NSGA-II (100 pop, 100 gen): ~5s

## Literatura

1. Deb, K., et al. (2002). "A fast and elitist multiobjective genetic algorithm: NSGA-II"
2. Miettinen, K. (1999). "Nonlinear Multiobjective Optimization"
3. Coello, C., et al. (2007). "Evolutionary Algorithms for Solving Multi-Objective Problems"

## Autor

Proširenje za višekriterijsku optimizaciju MCW problema.

## Licenca

MIT
