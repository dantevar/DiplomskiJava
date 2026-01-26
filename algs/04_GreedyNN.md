# Greedy Nearest Neighbor

## 📍 Lokacija
`src/main/java/heuristika/Greedy.java`

## 📊 Karakteristike
| Svojstvo | Vrijednost |
|----------|------------|
| Tip | Konstrukcijska heuristika |
| Složenost (vrijeme) | O(n²) |
| Složenost (memorija) | O(n) |
| Optimalnost | ❌ Približno |
| Tipični gap | 15-30% od optimuma |

## 🎯 Opis
Najjednostavnija heuristika za MCW/TSP. U svakom koraku bira **najbliži neposjećeni čvor**. Brza i jednostavna, ali često daje loša rješenja jer ne gleda unaprijed.

## 📝 Pseudokod

```
GREEDY_NEAREST_NEIGHBOR(Graph G):
    n = broj čvorova
    
    // Inicijalizacija
    tour = [0]              // Počni od čvora 0
    visited = {0}           // Set posjećenih
    current = 0             // Trenutna pozicija
    
    // Konstrukcija ture
    WHILE |visited| < n:
        // Pronađi najbližeg neposjećenog susjeda
        nextNode = -1
        minDistance = ∞
        
        FOR each j from 0 to n-1:
            IF j ∉ visited:
                // Koristi DIREKTNU udaljenost za odluku
                d = directDist[current][j]
                IF d < minDistance:
                    minDistance = d
                    nextNode = j
        
        // Dodaj u turu
        tour.append(nextNode)
        visited.add(nextNode)
        current = nextNode
    
    // Zatvori turu
    tour.append(0)
    
    // Izračunaj cost koristeći MIN_DISTANCES (shortest paths)
    // Ovo je ključna razlika za MCW vs TSP!
    totalCost = 0
    FOR i from 0 to |tour| - 2:
        u = tour[i]
        v = tour[i + 1]
        totalCost += minDist[u][v]
    
    RETURN (totalCost, tour)
```

## 💡 Ključne ideje

1. **Pohlepna strategija**: Uvijek biramo lokalno najbolju opciju

2. **MCW prilagodba**: 
   - Odluka se donosi na temelju `directDist` (direktni bridovi)
   - Cost se računa s `minDist` (shortest paths) - dozvoljava prečace

3. **Jednostavnost**: 
   - Lako za implementirati i razumjeti
   - Brzo izvršavanje - O(n²)

## ⚠️ Problemi s Greedy pristupom

```
Primjer zašto greedy ne radi optimalno:

Graf (trokut):
    0 ---1--- 1
     \       /
      2     1
       \   /
         2

Greedy od 0: 0 → 1 (cost 1) → 2 (cost 1) → 0 (cost 2) = 4
Optimalno:   0 → 2 (cost 2) → 1 (cost 1) → 0 (cost 1) = 4

U ovom slučaju jednako, ali u općem slučaju greedy može biti
značajno lošiji jer "zaglavi" u lošim izborima na početku.
```

## 📊 Tipični rezultati

| Veličina (N) | Prosječni gap | Vrijeme |
|--------------|---------------|---------|
| 10 | 15-20% | <1ms |
| 20 | 18-25% | <1ms |
| 50 | 20-30% | 2-3ms |
| 100 | 25-35% | 5-10ms |

## 🔧 Varijante i poboljšanja

### 1. Repeated Nearest Neighbor
```
best = ∞
FOR each startNode from 0 to n-1:
    result = GREEDY_NN(G, startNode)
    IF result.cost < best: best = result
RETURN best
```

### 2. Greedy + 2-opt
```
initial = GREEDY_NN(G)
improved = TWO_OPT(initial)
RETURN improved
```

### 3. Randomized Greedy
```
// Umjesto uvijek najbližeg, biramo među top-3 s vjerojatnošću
candidates = top 3 nearest unvisited
next = randomChoice(candidates, weights inversely proportional to distance)
```

## 🎓 Uloga u diplomskom radu

**Baseline heuristika** - koristi se za usporedbu s naprednijim metodama:
- Pokazuje da jednostavne heuristike daju brze ali loše rezultate
- Motivira potrebu za metaheuristikama
- Često se koristi kao početno rješenje za SA, GA, ACO
