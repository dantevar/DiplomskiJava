# Memetic Algorithm (GA + SA Hybrid)

## 📍 Lokacija
`src/main/java/metaheuristika/MemeticGASA.java`

## 📊 Karakteristike
| Svojstvo | Vrijednost |
|----------|------------|
| Tip | Hibridna metaheuristika |
| Složenost (vrijeme) | O(generations × popSize × SA_cost) |
| Složenost (memorija) | O(popSize × walk_length) |
| Optimalnost | ❌ Približno |
| Tipični gap | 2-5% od optimuma |

## 🎯 Opis
Memetic algoritam kombinira **globalnu pretragu** (GA) s **lokalnom optimizacijom** (SA). Također poznat kao **Lamarckian Evolution** - poboljšanja stečena tijekom života jedinke se nasljeđuju na potomke.

## 📝 Pseudokod

```
MEMETIC_GA_SA(Graph G, popSize, generations, mutationRate, saApplyRate):
    n = broj čvorova
    
    // ═══════════════════════════════════════════
    // INICIJALIZACIJA S LOKALNOM OPTIMIZACIJOM
    // ═══════════════════════════════════════════
    population = []
    FOR i from 1 to popSize:
        individual = RANDOM_WALK(n)
        
        // Početna lokalna optimizacija (kratki SA)
        individual = SHORT_SA(individual, T=30, iter=20)
        
        population.add(individual)
    
    globalBest = null
    globalBestCost = ∞
    
    // ═══════════════════════════════════════════
    // GLAVNA GA-SA PETLJA
    // ═══════════════════════════════════════════
    FOR gen from 1 to generations:
        
        // Evaluacija populacije
        fitness = []
        FOR i from 0 to popSize - 1:
            fitness[i] = EVALUATE_WALK(population[i])
            
            IF fitness[i] < globalBestCost:
                globalBestCost = fitness[i]
                globalBest = population[i].clone()
        
        // Adaptivni SA parametri (decay over generations)
        saParams = GET_ADAPTIVE_SA_PARAMS(gen, generations)
        
        // ─────────────────────────────────────────
        // KREIRANJE NOVE GENERACIJE
        // ─────────────────────────────────────────
        newPopulation = []
        
        WHILE |newPopulation| < popSize:
            // Selekcija
            parent1 = TOURNAMENT_SELECT(population, fitness, k=3)
            parent2 = TOURNAMENT_SELECT(population, fitness, k=3)
            
            // Križanje
            offspring = CROSSOVER(parent1, parent2, n)
            
            // Mutacija
            IF random() < mutationRate:
                MUTATE(offspring, n)
            
            // ═══════════════════════════════════
            // LAMARCKIAN LEARNING (ključna razlika!)
            // ═══════════════════════════════════
            IF random() < saApplyRate:
                offspring = LOCAL_SEARCH_SA(offspring, saParams)
            
            newPopulation.add(offspring)
        
        population = newPopulation
    
    RETURN (globalBestCost, globalBest)

// ─────────────────────────────────────────────────
// Adaptivni SA parametri
// ─────────────────────────────────────────────────
GET_ADAPTIVE_SA_PARAMS(gen, maxGen):
    // Na početku: agresivniji SA (više iteracija, viša T)
    // Na kraju: finiji SA (manje iteracija, niža T)
    
    progress = gen / maxGen  // 0.0 → 1.0
    
    T0 = 50 * (1 - progress * 0.7)    // 50 → 15
    alpha = 0.9 + progress * 0.05     // 0.9 → 0.95
    iterations = 30 - progress * 15   // 30 → 15
    
    RETURN SAParams(T0, alpha, iterations)

// ─────────────────────────────────────────────────
// Lokalna pretraga (kratki SA)
// ─────────────────────────────────────────────────
LOCAL_SEARCH_SA(walk, params):
    current = walk.clone()
    currentCost = EVALUATE_WALK(current)
    best = current
    bestCost = currentCost
    
    T = params.T0
    
    FOR iter from 1 to params.iterations:
        // Generiraj susjeda
        neighbor = GENERATE_NEIGHBOR(current)
        neighborCost = EVALUATE_WALK(neighbor)
        
        Δ = neighborCost - currentCost
        
        // Metropolis
        IF Δ < 0 OR random() < exp(-Δ/T):
            current = neighbor
            currentCost = neighborCost
            
            IF currentCost < bestCost:
                best = current.clone()
                bestCost = currentCost
        
        T = T * params.alpha
    
    RETURN best  // Lamarckian: vraćamo poboljšanu jedinku

// ─────────────────────────────────────────────────
// Crossover za walk reprezentaciju
// ─────────────────────────────────────────────────
CROSSOVER(parent1, parent2, n):
    // Segment exchange crossover
    offspring = []
    
    // Uzmi početni segment iz parent1
    cutPoint = random(1, |parent1| - 2)
    offspring = parent1[0:cutPoint]
    
    // Dodaj čvorove iz parent2 koji nedostaju
    covered = set(offspring)
    
    FOR node in parent2:
        IF node not in covered:
            offspring.append(node)
            covered.add(node)
    
    // Provjeri i dodaj bilo koje nedostajuće čvorove
    FOR node from 0 to n-1:
        IF node not in covered:
            offspring.append(node)
    
    // Zatvori walk (vrati se na 0)
    IF offspring.last() != 0:
        offspring.append(0)
    
    RETURN offspring

// ─────────────────────────────────────────────────
// Mutacija za walk
// ─────────────────────────────────────────────────
MUTATE(walk, n):
    operation = random(0, 3)
    
    SWITCH operation:
        CASE 0: SWAP_NODES(walk)     // Zamijeni dva čvora
        CASE 1: REVERSE_SEGMENT(walk) // 2-opt
        CASE 2: INSERT_NODE(walk, n)  // Ubaci čvor
        CASE 3: REMOVE_DUPLICATE(walk) // Ukloni duplikat
```

## 💡 Ključne ideje

### 1. Lamarckian vs Baldwinian Evolution
```
LAMARCKIAN (korišteno ovdje):
  Poboljšanja se ZAPISUJU u genom
  Potomci nasljeđuju optimizirana rješenja
  + Brža konvergencija
  - Može smanjiti diverzitet

BALDWINIAN:
  Poboljšanja se NE zapisuju u genom
  Koriste se samo za evaluaciju fitnessa
  + Očuva diverzitet
  - Sporija konvergencija
```

### 2. Walk vs Permutacija reprezentacija
```
Permutacija: [0, 3, 1, 4, 2]
  - Fiksna duljina n
  - Nema ponavljanja
  
Walk: [0, 3, 1, 3, 4, 2, 0]
  - Varijabilna duljina
  - Dozvoljava ponavljanje (mostovi)
  - Fleksibilnije za MCW!
```

### 3. Adaptivni SA parametri
```
Generacija 0 (početak):
  - Visoka T: eksploracija
  - Duže trajanje: temeljito poboljšanje
  
Generacija 80 (kraj):
  - Niska T: fine-tuning
  - Kraće trajanje: brzina
```

## 📊 Tipični parametri

| Parametar | Preporučena vrijednost |
|-----------|------------------------|
| popSize | 30-50 |
| generations | 50-100 |
| mutationRate | 0.15-0.25 |
| saApplyRate | 0.3-0.5 |

## 📈 Zašto je Memetic bolji od čistog GA?

```
Problem: GA stagnira jer:
  1. Crossover ne čuva lokalnu strukturu
  2. Mutacija je "nasumična"
  3. Fitness landscape je rugged

Rješenje: SA lokalno "polira" svako rješenje
  1. Crossover daje grubu smjernicu
  2. SA fino podešava
  3. Najbolje od oba svijeta!

Empirijski rezultati (N=20):
  Pure GA: 8-12% gap
  Pure SA: 6-10% gap
  Memetic: 2-5% gap  ← 2-3x bolji!
```

## 🔧 Poboljšanja

### Restart strategija
```
IF no improvement for 20 generations:
    Regenerate 30% population with new random walks
```

### Elitizam
```
Sačuvaj top 10% populacije bez promjena
```

### Niching
```
Održavaj diverzitet kažnjavanjem sličnih jedinki
```

## 🎓 Uloga u diplomskom radu
**Najbolji algoritam u projektu!** Pokazuje snagu hibridizacije - kombinacija globalne i lokalne pretrage daje superiorne rezultate.
