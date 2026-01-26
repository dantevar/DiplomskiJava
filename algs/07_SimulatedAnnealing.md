# Simulated Annealing (SA)

## 📍 Lokacija
`src/main/java/metaheuristika/SimulatedAnnealingPermutation.java`

## 📊 Karakteristike
| Svojstvo | Vrijednost |
|----------|------------|
| Tip | Metaheuristika (trajectory-based) |
| Složenost (vrijeme) | O(iterations × n) |
| Složenost (memorija) | O(n) |
| Optimalnost | ❌ Približno |
| Tipični gap | 5-15% od optimuma |

## 🎯 Opis
Simulated Annealing simulira **proces hlađenja metala** u metalurgiji. Na visokoj temperaturi prihvaća i loše poteze (escape local optima), a kako se hladi postaje sve "pohlepniji" i prihvaća samo poboljšanja.

## 📝 Pseudokod

```
SIMULATED_ANNEALING(Graph G, T0, α, iterPerTemp, minTemp):
    n = broj čvorova
    
    // ═══════════════════════════════════════════
    // INICIJALIZACIJA
    // ═══════════════════════════════════════════
    current = RANDOM_PERMUTATION(n)
    currentCost = EVALUATE(current)
    
    best = current.clone()
    bestCost = currentCost
    
    T = T0  // Početna temperatura
    
    // ═══════════════════════════════════════════
    // GLAVNA PETLJA HLAĐENJA
    // ═══════════════════════════════════════════
    WHILE T > minTemp:
        
        FOR i from 1 to iterPerTemp:
            
            // Generiraj susjedno rješenje
            neighbor = GENERATE_NEIGHBOR(current)
            neighborCost = EVALUATE(neighbor)
            
            // Izračunaj promjenu energije
            Δ = neighborCost - currentCost
            
            // ─────────────────────────────────
            // METROPOLIS KRITERIJ
            // ─────────────────────────────────
            IF Δ < 0:
                // Poboljšanje - uvijek prihvati
                current = neighbor
                currentCost = neighborCost
            ELSE:
                // Pogoršanje - prihvati s vjerojatnošću
                p = exp(-Δ / T)
                IF random() < p:
                    current = neighbor
                    currentCost = neighborCost
            
            // Ažuriraj globalno najbolje
            IF currentCost < bestCost:
                best = current.clone()
                bestCost = currentCost
        
        // HLAĐENJE
        T = T * α
    
    RETURN (bestCost, best)

// ─────────────────────────────────────────────────
// Evaluacija (fitness)
// ─────────────────────────────────────────────────
EVALUATE(permutation):
    cost = 0
    FOR i from 0 to n - 2:
        cost += minDist[permutation[i]][permutation[i+1]]
    cost += minDist[permutation[n-1]][permutation[0]]
    RETURN cost

// ─────────────────────────────────────────────────
// Generiranje susjeda - 2-opt
// ─────────────────────────────────────────────────
GENERATE_NEIGHBOR(permutation):
    neighbor = permutation.clone()
    
    // Odaberi dva random indeksa
    i = random(0, n-1)
    j = random(0, n-1)
    IF i > j: swap(i, j)
    
    // Preokreni segment [i+1, j]
    REVERSE(neighbor, i + 1, j)
    
    RETURN neighbor

REVERSE(array, start, end):
    WHILE start < end:
        swap(array[start], array[end])
        start++
        end--
```

## 💡 Ključne ideje

### 1. Metropolis kriterij
```
P(prihvati lošije) = exp(-Δ/T)

Primjer:
  Δ = 0.5 (pogoršanje od 0.5)
  T = 10:  P = exp(-0.5/10) = 0.95  (gotovo sigurno prihvati)
  T = 1:   P = exp(-0.5/1)  = 0.61  (vjerojatno prihvati)
  T = 0.1: P = exp(-0.5/0.1)= 0.007 (gotovo sigurno odbaci)
```

### 2. Annealing Schedule (raspored hlađenja)
```
Geometric: T_new = T * α  (α tipično 0.95-0.99)

Primjer za T0=100, α=0.95:
  T=100 → 95 → 90.25 → 85.74 → ... → 0.01 (minTemp)
```

### 3. 2-opt susjedstvo
```
Before: 0 → 1 → 2 → 3 → 4 → 5 → 0
              ─────────
After:  0 → 1 → 4 → 3 → 2 → 5 → 0
              ─────────
              (reversed)
```

## 📊 Tipični parametri

| Parametar | Oznaka | Tipična vrijednost | Utjecaj |
|-----------|--------|-------------------|---------|
| Početna temp | T0 | 100 | Visoko = više eksploracije |
| Faktor hlađenja | α | 0.95-0.99 | Niže = brže hlađenje |
| Iter po temp | iterPerTemp | 100 | Više = bolja pretraga |
| Min temp | minTemp | 0.01 | Stop uvjet |

## 📈 Tipično ponašanje

```
T=100: Prihvaća ~95% poteza (eksplozija)
T=10:  Prihvaća ~70% poteza
T=1:   Prihvaća ~40% poteza
T=0.1: Prihvaća ~5% poteza (gotovo greedy)
T=0.01: Prihvaća ~0.5% poteza (pure local search)
```

## 🔧 Varijante u projektu

### SimulatedAnnealingWalk
`SimulatedAnnealingWalk.java` - koristi walk reprezentaciju s više operatora:
- Insert node
- Delete node (duplikate)
- Swap nodes
- Replace node

## ⚠️ Prednosti i mane

| Prednosti | Mane |
|-----------|------|
| ✅ Escape local optima | ❌ Osjetljiv na parametre |
| ✅ Jednostavan za implementaciju | ❌ Spor za konvergenciju |
| ✅ Teoretska garancija (asimptotski) | ❌ Jedna trajektorija |
| ✅ Malo memorije | ❌ Nema pamćenje dobre regije |

## 🎓 Uloga u diplomskom radu
Klasična metaheuristika, koristi se kao:
1. Standalone algoritam za usporedbu
2. Lokalna optimizacija unutar Memetic GA
