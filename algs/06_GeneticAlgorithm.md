# Genetic Algorithm (GA)

## 📍 Lokacija
`src/main/java/metaheuristika/GA.java`

## 📊 Karakteristike
| Svojstvo | Vrijednost |
|----------|------------|
| Tip | Metaheuristika (evolucijska) |
| Složenost (vrijeme) | O(generations × popSize × n) |
| Složenost (memorija) | O(popSize × n) |
| Optimalnost | ❌ Približno |
| Tipični gap | 5-12% od optimuma |

## 🎯 Opis
Genetski algoritam simulira **prirodnu evoluciju** za optimizaciju. Populacija rješenja (permutacija) evoluira kroz generacije pomoću selekcije, križanja i mutacije. Dobra rješenja imaju veću šansu za reprodukciju.

## 📝 Pseudokod

```
GENETIC_ALGORITHM(Graph G, popSize, generations, mutationRate):
    n = broj čvorova
    
    // ═══════════════════════════════════════════
    // INICIJALIZACIJA
    // ═══════════════════════════════════════════
    population = []
    FOR i from 1 to popSize:
        individual = RANDOM_PERMUTATION(n)  // [0, 3, 1, 4, 2, ...]
        population.add(individual)
    
    best = population[0]
    bestCost = FITNESS(best)
    
    // ═══════════════════════════════════════════
    // GLAVNA PETLJA EVOLUCIJE
    // ═══════════════════════════════════════════
    FOR gen from 1 to generations:
        
        // Evaluacija fitnessa cijele populacije
        fitness = []
        FOR each individual in population:
            fitness.add(FITNESS(individual))
            
            // Ažuriraj globalno najbolje
            IF fitness.last() < bestCost:
                bestCost = fitness.last()
                best = individual.clone()
        
        // Kreiraj novu generaciju
        newPopulation = []
        
        // ELITIZAM: zadrži top 10%
        eliteCount = popSize / 10
        sortedIndices = argsort(fitness)  // ascending
        FOR i from 0 to eliteCount - 1:
            newPopulation.add(population[sortedIndices[i]].clone())
        
        // REPRODUKCIJA: popuni ostatak populacije
        WHILE |newPopulation| < popSize:
            // Selekcija roditelja
            parent1 = TOURNAMENT_SELECT(population, fitness)
            parent2 = TOURNAMENT_SELECT(population, fitness)
            
            // Križanje
            child = ORDER_CROSSOVER(parent1, parent2)
            
            // Mutacija
            IF random() < mutationRate:
                SWAP_MUTATE(child)
            
            newPopulation.add(child)
        
        population = newPopulation
    
    RETURN (bestCost, best)

// ─────────────────────────────────────────────────
// Fitness funkcija
// ─────────────────────────────────────────────────
FITNESS(individual):
    // individual = permutacija [0, 3, 1, 4, 2]
    cost = 0
    FOR i from 0 to n - 2:
        cost += minDist[individual[i]][individual[i+1]]
    cost += minDist[individual[n-1]][individual[0]]  // povratak
    RETURN cost

// ─────────────────────────────────────────────────
// Tournament selekcija
// ─────────────────────────────────────────────────
TOURNAMENT_SELECT(population, fitness, k=3):
    // Odaberi k random jedinki, vrati najbolju
    best = null
    bestFit = ∞
    
    FOR i from 1 to k:
        idx = random(0, |population| - 1)
        IF fitness[idx] < bestFit:
            bestFit = fitness[idx]
            best = population[idx]
    
    RETURN best.clone()

// ─────────────────────────────────────────────────
// Order Crossover (OX)
// ─────────────────────────────────────────────────
ORDER_CROSSOVER(parent1, parent2):
    n = |parent1|
    child = [-1, -1, ..., -1]  // n elemenata
    
    // 1. Odaberi random segment iz parent1
    start = random(0, n-1)
    end = random(0, n-1)
    IF start > end: swap(start, end)
    
    // 2. Kopiraj segment iz parent1
    FOR i from start to end:
        child[i] = parent1[i]
    
    // 3. Popuni ostatak iz parent2 (u redoslijedu)
    pos = (end + 1) mod n
    FOR i from 0 to n - 1:
        gene = parent2[(end + 1 + i) mod n]
        IF gene not in child:
            child[pos] = gene
            pos = (pos + 1) mod n
    
    RETURN child

// ─────────────────────────────────────────────────
// Swap mutacija
// ─────────────────────────────────────────────────
SWAP_MUTATE(individual):
    i = random(0, n-1)
    j = random(0, n-1)
    swap(individual[i], individual[j])

// ─────────────────────────────────────────────────
// Random permutacija (Fisher-Yates shuffle)
// ─────────────────────────────────────────────────
RANDOM_PERMUTATION(n):
    perm = [0, 1, 2, ..., n-1]
    FOR i from n-1 downto 1:
        j = random(0, i)
        swap(perm[i], perm[j])
    RETURN perm
```

## 💡 Ključne ideje

1. **Reprezentacija**: Permutacija čvorova [0, 3, 1, 4, 2] = redoslijed posjeta

2. **Fitness = Tour cost**: Niži je bolji (minimizacija)

3. **Elitizam**: Čuva najbolje jedinke → sprječava gubitak dobrih rješenja

4. **Order Crossover (OX)**: Sačuva relativni redoslijed čvorova

5. **Eksploatacija vs Eksploracija**:
   - Mutation rate visok → više eksploracije
   - Mutation rate nizak → više eksploatacije

## 📊 Tipični parametri

| Parametar | Preporučena vrijednost | Napomena |
|-----------|------------------------|----------|
| popSize | 50-200 | Veći za veći N |
| generations | 100-500 | Više = bolje, ali sporije |
| mutationRate | 0.1-0.2 | Previše = kaos, premalo = stagnacija |
| eliteRatio | 0.1 | 10% najboljih |
| tournamentSize | 3 | Balans selekcijskog pritiska |

## 🔧 Varijante u projektu

### GAWalk (Walk reprezentacija)
`GAWalk.java` - koristi walk s ponavljanjima umjesto permutacije

### MemeticGASA (Hibrid)
`MemeticGASA.java` - GA + lokalna optimizacija (SA)

## 📈 Konvergencija

```
Tipična konvergencija:
Gen 0:   Avg fitness = 5.2, Best = 4.1
Gen 50:  Avg fitness = 3.8, Best = 2.9
Gen 100: Avg fitness = 3.2, Best = 2.5
Gen 200: Avg fitness = 2.8, Best = 2.3
Gen 500: Avg fitness = 2.5, Best = 2.2

Stagnacija često nakon 100-200 generacija bez hibridizacije.
```

## 🎓 Uloga u diplomskom radu
Klasična metaheuristika za usporedbu s ACO, SA i hibridnim pristupima.
