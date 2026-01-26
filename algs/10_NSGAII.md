# NSGA-II (Multi-objective Optimization)

## 📍 Lokacija
`src/main/java/metaheuristika/NSGAII_MCW.java`

## 📊 Karakteristike
| Svojstvo | Vrijednost |
|----------|------------|
| Tip | Multi-objective metaheuristika |
| Složenost (vrijeme) | O(generations × popSize² × objectives) |
| Složenost (memorija) | O(popSize × n) |
| Optimalnost | ❌ Pareto skup |
| Izlaz | Pareto fronta (skup ne-dominiranih rješenja) |

## 🎯 Opis
NSGA-II (Non-dominated Sorting Genetic Algorithm II) optimizira **više ciljeva istovremeno**. Umjesto jednog optimalnog rješenja, vraća **Pareto frontu** - skup rješenja gdje nijedno nije bolje od drugog u svim kriterijima.

## 📝 Pseudokod

```
NSGA_II(Graph G, popSize, generations, mutationRate, crossoverRate):
    n = broj čvorova
    
    // ═══════════════════════════════════════════
    // INICIJALIZACIJA
    // ═══════════════════════════════════════════
    population = []
    FOR i from 1 to popSize:
        population.add(RANDOM_PERMUTATION(n))
    
    // ═══════════════════════════════════════════
    // GLAVNA PETLJA
    // ═══════════════════════════════════════════
    FOR gen from 1 to generations:
        
        // ─────────────────────────────────────────
        // EVALUACIJA VIŠE CILJEVA
        // ─────────────────────────────────────────
        FOR each individual in population:
            individual.objectives = [
                totalCost(individual),      // f1: minimizacija
                edgeCount(individual),      // f2: minimizacija
                maxEdgeWeight(individual),  // f3: min-max
                variance(individual),       // f4: minimizacija
                vertexRepetitions(individual) // f5: minimizacija
            ]
        
        // ─────────────────────────────────────────
        // NON-DOMINATED SORTING
        // ─────────────────────────────────────────
        fronts = NON_DOMINATED_SORT(population)
        
        // ─────────────────────────────────────────
        // CROWDING DISTANCE (unutar svake fronte)
        // ─────────────────────────────────────────
        FOR each front F in fronts:
            CALCULATE_CROWDING_DISTANCE(F)
        
        // ─────────────────────────────────────────
        // KREIRANJE POTOMAKA
        // ─────────────────────────────────────────
        offspring = []
        WHILE |offspring| < popSize:
            // Binary tournament (rank, zatim crowding distance)
            parent1 = CROWDED_TOURNAMENT(population)
            parent2 = CROWDED_TOURNAMENT(population)
            
            IF random() < crossoverRate:
                child = ORDER_CROSSOVER(parent1, parent2)
            ELSE:
                child = parent1.clone()
            
            IF random() < mutationRate:
                SWAP_MUTATE(child)
            
            offspring.add(child)
        
        // ─────────────────────────────────────────
        // SELEKCIJA SLJEDEĆE GENERACIJE
        // ─────────────────────────────────────────
        combined = population + offspring  // 2N jedinki
        fronts = NON_DOMINATED_SORT(combined)
        
        population = []
        frontIndex = 0
        
        // Dodaj cijele fronte dok stane
        WHILE |population| + |fronts[frontIndex]| <= popSize:
            population.addAll(fronts[frontIndex])
            frontIndex++
        
        // Zadnju frontu sortiraj po crowding distance
        IF |population| < popSize:
            remaining = popSize - |population|
            lastFront = fronts[frontIndex]
            CALCULATE_CROWDING_DISTANCE(lastFront)
            lastFront.sortBy(crowdingDistance, descending)
            population.addAll(lastFront[0:remaining])
    
    // Vrati prvu frontu (Pareto optimalna rješenja)
    RETURN NON_DOMINATED_SORT(population)[0]

// ─────────────────────────────────────────────────
// Non-dominated sorting
// ─────────────────────────────────────────────────
NON_DOMINATED_SORT(population):
    fronts = [[]]
    
    FOR each p in population:
        p.dominationCount = 0
        p.dominatedSet = []
        
        FOR each q in population:
            IF DOMINATES(p, q):
                p.dominatedSet.add(q)
            ELSE IF DOMINATES(q, p):
                p.dominationCount++
        
        IF p.dominationCount == 0:
            p.rank = 0
            fronts[0].add(p)
    
    i = 0
    WHILE fronts[i] not empty:
        nextFront = []
        FOR each p in fronts[i]:
            FOR each q in p.dominatedSet:
                q.dominationCount--
                IF q.dominationCount == 0:
                    q.rank = i + 1
                    nextFront.add(q)
        i++
        IF nextFront not empty:
            fronts.add(nextFront)
    
    RETURN fronts

// ─────────────────────────────────────────────────
// Pareto dominacija
// ─────────────────────────────────────────────────
DOMINATES(p, q):
    // p dominira q ako je bolji ili jednak u svim ciljevima
    // i striktno bolji u barem jednom
    
    atLeastOneBetter = false
    
    FOR i from 0 to numObjectives - 1:
        IF p.objectives[i] > q.objectives[i]:
            RETURN false  // p je lošiji u barem jednom
        IF p.objectives[i] < q.objectives[i]:
            atLeastOneBetter = true
    
    RETURN atLeastOneBetter

// ─────────────────────────────────────────────────
// Crowding distance
// ─────────────────────────────────────────────────
CALCULATE_CROWDING_DISTANCE(front):
    n = |front|
    IF n == 0: RETURN
    
    FOR each individual:
        individual.crowdingDistance = 0
    
    FOR each objective m:
        // Sortiraj frontu po ovom cilju
        front.sortBy(objectives[m])
        
        // Rubne jedinke imaju beskonačnu udaljenost
        front[0].crowdingDistance = ∞
        front[n-1].crowdingDistance = ∞
        
        // Računaj udaljenost za ostale
        range = front[n-1].objectives[m] - front[0].objectives[m]
        IF range == 0: CONTINUE
        
        FOR i from 1 to n-2:
            front[i].crowdingDistance += 
                (front[i+1].objectives[m] - front[i-1].objectives[m]) / range

// ─────────────────────────────────────────────────
// Crowded tournament selekcija
// ─────────────────────────────────────────────────
CROWDED_TOURNAMENT(population):
    i = random(0, |population| - 1)
    j = random(0, |population| - 1)
    
    // Preferiraj niži rank
    IF population[i].rank < population[j].rank:
        RETURN population[i]
    ELSE IF population[i].rank > population[j].rank:
        RETURN population[j]
    ELSE:
        // Isti rank - preferiraj veću crowding distance (više diverziteta)
        IF population[i].crowdingDistance > population[j].crowdingDistance:
            RETURN population[i]
        ELSE:
            RETURN population[j]
```

## 💡 Ključne ideje

### 1. Pareto dominacija
```
Rješenje A dominira B ako:
  - A je bolji ili jednak u SVIM ciljevima
  - A je STRIKTNO bolji u barem jednom cilju

Primjer (minimizacija oba cilja):
  A = (3, 5)
  B = (4, 6)  → A dominira B (3<4, 5<6)
  C = (2, 7)  → A ne dominira C (5<7 ali 3>2)
```

### 2. Pareto fronta
```
Skup svih ne-dominiranih rješenja

          f2
           │    × B
           │  × A
           │    × C
           ├────────── f1
           
A, B, C su na Pareto fronti ako se međusobno ne dominiraju
```

### 3. Crowding distance
```
Mjeri koliko je rješenje "usamljeno" u prostoru ciljeva
Preferiramo veću udaljenost → očuvanje diverziteta

    × ─ 1.5 ─ × ─ 0.5 ─ ×
    
Krajnje točke imaju ∞ (uvijek se čuvaju)
```

## 📊 Ciljevi u MCW

| Cilj | Formula | Značenje |
|------|---------|----------|
| f1: totalCost | Σ dist[i][i+1] | Ukupna duljina puta |
| f2: edgeCount | |walk| - 1 | Broj koraka |
| f3: maxEdge | max(dist[i][i+1]) | Najduži pojedinačni brid |
| f4: variance | var(dist) | Ujednačenost |
| f5: repetitions | |walk| - |unique(walk)| | Ponavljanja čvorova |

## 🎨 Vizualizacija Pareto fronte

```
Primjer izlaza (2D projekcija):

Cost ↑
  15 ┤      ×
  14 ┤    ×
  13 ┤  ×   ×
  12 ┤×       ×
  11 ┤──────────→ EdgeCount
      5  6  7  8

Trade-off: Kraći put = skuplji
           Jeftiniji put = duži
```

## 🔧 Odabir rješenja iz Pareto fronte

### 1. Weighted Sum
```java
score = w1*f1 + w2*f2 + w3*f3 + w4*f4 + w5*f5
best = argmin(score)
```

### 2. Compromise Programming
```java
// Minimizira udaljenost od idealne točke
distance = sqrt(Σ((fi - ideal_i) / (nadir_i - ideal_i))^2)
```

### 3. Korisnikova preferencija
```
Korisnik bira na temelju konteksta:
- Dostava: prioritet = vrijeme (f1)
- Robotika: prioritet = energija (f3, max edge)
- Sigurnost: prioritet = varijanca (f4)
```

## 🎓 Uloga u diplomskom radu
Pokazuje višekriterijsku perspektivu MCW problema - u praksi često postoji više ciljeva, ne samo minimalna cijena.
