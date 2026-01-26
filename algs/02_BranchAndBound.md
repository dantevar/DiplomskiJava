# Branch and Bound

## 📍 Lokacija
`src/main/java/fer/BruteForce.java` (metoda `branchAndBound`)

## 📊 Karakteristike
| Svojstvo | Vrijednost |
|----------|------------|
| Tip | Egzaktni |
| Složenost (vrijeme) | O(n!) worst case, praktično puno brže |
| Složenost (memorija) | O(n) - rekurzivni stack |
| Optimalnost | ✅ Garantirana |
| Praktični limit | N ≤ 15-18 |

## 🎯 Opis
Branch and Bound je **pametni brute force** koji koristi donje granice (lower bounds) za rano odbacivanje grana pretrage koje sigurno ne mogu dati bolje rješenje od trenutno najboljeg.

## 📝 Pseudokod

```
BRANCH_AND_BOUND(Graph G):
    n = broj čvorova
    best = ∞
    bestPath = null
    visited = boolean[n]
    visited[0] = true  // start je uvijek čvor 0
    
    // Preračunaj heuristiku jednom (optimizacija)
    minOutgoing = precomputeMinEdges(G)
    
    // Pokreni rekurzivnu pretragu
    RECURSIVE(current=0, cost=0, depth=1, path=[0])
    
    RETURN (best, bestPath)

RECURSIVE(current, cost, depth, path):
    // BAZNI SLUČAJ: svi vrhovi posjećeni
    IF depth == n:
        totalCost = cost + minDist[current][0]
        IF totalCost < best:
            best = totalCost
            bestPath = path + [0]
        RETURN
    
    // PRUNING: Izračunaj donju granicu
    lowerBound = CALCULATE_LOWER_BOUND(current, cost, visited)
    
    IF lowerBound >= best:
        RETURN  // ✂️ ODSIJECI GRANU - nema šanse za poboljšanje
    
    // BRANCH: Probaj sve neposjećene čvorove
    FOR each next from 1 to n-1:
        IF visited[next]: CONTINUE
        
        edgeCost = minDist[current][next]
        IF cost + edgeCost >= best: CONTINUE  // Rano odbacivanje
        
        // Rekurzivni poziv
        visited[next] = true
        RECURSIVE(next, cost + edgeCost, depth + 1, path + [next])
        visited[next] = false  // Backtrack

CALCULATE_LOWER_BOUND(current, currentCost, visited):
    bound = currentCost
    
    // Broji neposjećene čvorove
    unvisitedCount = count(visited == false)
    IF unvisitedCount == 0:
        RETURN bound + minDist[current][0]
    
    // MST-based lower bound:
    // Za svaki neposjećeni čvor dodaj najmanji brid / 2
    FOR each unvisited node i:
        minEdge = min over all j ≠ i: minDist[i][j]
        bound += minEdge / 2
    
    // Dodaj najmanji povratak na 0
    minToZero = min over unvisited i: minDist[i][0]
    bound += minToZero
    
    RETURN bound
```

## 💡 Ključne ideje

1. **Pruning**: Ako je `lowerBound >= best`, sigurno nema boljeg rješenja u toj grani
   - Lower bound mora biti **admisibilan** (optimističan, ≤ stvarni cost)

2. **Backtracking**: Koristi `visited` array koji se vraća u prethodno stanje

3. **Rano odbacivanje**: Čak i bez lower bound, provjeravamo `cost + edgeCost >= best`

## ⚠️ Važno: Admisibilnost Lower Bounda

Lower bound MORA biti ≤ od stvarnog troška bilo kojeg rješenja u grani, inače možemo odbaciti optimalno rješenje!

```
Stari (bugovani) lower bound:
  bound = cost + minFromCurrent + Σ(minIn + minOut)/2 + minToZero
  Problem: duplo broji neke bridove → NIJE admisibilan

Novi (ispravljeni) lower bound:
  bound = cost + Σ(minEdge/2) + minToZero
  Garantirano admisibilan jer MST ≤ optimal tour
```

## 📊 Usporedba s Brute Force

| N | Brute Force | Branch & Bound | Speedup |
|---|-------------|----------------|---------|
| 8 | 5,040 | ~500 | ~10x |
| 10 | 362,880 | ~5,000 | ~70x |
| 12 | 39,916,800 | ~50,000 | ~800x |

## 🔧 Moguća poboljšanja
- Bolja heuristika za lower bound (MST, 1-tree)
- Sortiranje djece po obećavajućnosti (best-first)
- Paralelizacija grana
