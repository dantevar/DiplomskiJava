# ASPW - Adaptive Shortest Path Walker

## 📍 Lokacija
`src/main/java/heuristika/ASPW.java`

## 📊 Karakteristike
| Svojstvo | Vrijednost |
|----------|------------|
| Tip | Konstrukcijska heuristika (MCW-specifična) |
| Složenost (vrijeme) | O(n³) |
| Složenost (memorija) | O(n²) |
| Optimalnost | ❌ Približno |
| Tipični gap | 5-15% od optimuma |

## 🎯 Opis
**Originalna heuristika dizajnirana specifično za MCW problem.** Za razliku od TSP heuristika, ASPW eksplicitno koristi shortest paths i dozvoljava ponavljanje čvorova kao "mostove" između udaljenih dijelova grafa.

## 📝 Pseudokod

```
ASPW(Graph G, α):
    n = broj čvorova
    
    // ═══════════════════════════════════════════
    // FAZA 1: Greedy Coverage (pokrivanje čvorova)
    // ═══════════════════════════════════════════
    walk = [0]
    uncovered = {1, 2, 3, ..., n-1}
    
    WHILE uncovered not empty:
        current = walk.last()
        
        // Odaberi sljedeći čvor za pokriti
        next = SELECT_NEXT(current, uncovered, α)
        
        // Idi shortest pathom do njega (može proći kroz druge čvorove!)
        path = SHORTEST_PATH(current, next)
        
        // Dodaj put u walk (bez dupliciranja prvog čvora)
        walk.extend(path[1:])
        
        // Označi SVE čvorove na putu kao pokrivene
        FOR each node in path:
            uncovered.remove(node)
    
    // ═══════════════════════════════════════════
    // FAZA 2: Closing (zatvaranje walka)
    // ═══════════════════════════════════════════
    IF walk.last() ≠ 0:
        closingPath = SHORTEST_PATH(walk.last(), 0)
        walk.extend(closingPath[1:])
    
    // ═══════════════════════════════════════════
    // FAZA 3: Local Optimization (poboljšanje)
    // ═══════════════════════════════════════════
    walk = LOCAL_OPTIMIZATION(walk)
    
    RETURN (evaluateWalk(walk), walk)

// ─────────────────────────────────────────────────
// Selekcija sljedećeg čvora
// ─────────────────────────────────────────────────
SELECT_NEXT(current, uncovered, α):
    bestScore = ∞
    bestCandidate = -1
    
    FOR each candidate in uncovered:
        // Komponenta 1: Udaljenost do kandidata
        distance = minDist[current][candidate]
        
        // Komponenta 2: Centralnost (prosječna udaljenost do ostalih)
        avgRemaining = AVERAGE_DISTANCE_TO_REMAINING(candidate, uncovered)
        
        // Kombinirani score
        score = distance + α * avgRemaining
        
        IF score < bestScore:
            bestScore = score
            bestCandidate = candidate
    
    RETURN bestCandidate

AVERAGE_DISTANCE_TO_REMAINING(node, uncovered):
    IF |uncovered| ≤ 1: RETURN 0
    
    sum = 0
    count = 0
    FOR each other in uncovered:
        IF other ≠ node:
            sum += minDist[node][other]
            count++
    
    RETURN sum / count

// ─────────────────────────────────────────────────
// Rekonstrukcija shortest path
// ─────────────────────────────────────────────────
SHORTEST_PATH(start, end):
    IF start == end: RETURN [start]
    
    path = [start]
    current = start
    
    WHILE current ≠ end:
        // Greedy: biramo čvor koji nas vodi bliže cilju
        bestNext = -1
        bestProgress = ∞
        
        FOR each next from 0 to n-1:
            IF next == current: CONTINUE
            
            // Provjeri je li next na optimalnom putu
            IF directDist[current][next] + minDist[next][end] ≈ minDist[current][end]:
                IF directDist[current][next] + minDist[next][end] < bestProgress:
                    bestProgress = directDist[current][next] + minDist[next][end]
                    bestNext = next
        
        IF bestNext == -1:
            path.append(end)  // Direktan skok
            BREAK
        
        path.append(bestNext)
        current = bestNext
    
    RETURN path

// ─────────────────────────────────────────────────
// Lokalna optimizacija
// ─────────────────────────────────────────────────
LOCAL_OPTIMIZATION(walk):
    REPEAT max 50 iterations:
        improved = false
        currentCost = evaluateWalk(walk)
        
        // Operator 1: 2-opt
        result = TRY_2OPT(walk, currentCost)
        IF result ≠ null:
            walk = result
            improved = true
            CONTINUE
        
        // Operator 2: Shortcut (zamjena segmenta kraćim putem)
        result = TRY_SHORTCUT(walk, currentCost)
        IF result ≠ null:
            walk = result
            improved = true
            CONTINUE
        
        // Operator 3: Node removal (uklanjanje duplikata)
        result = TRY_NODE_REMOVAL(walk, currentCost)
        IF result ≠ null:
            walk = result
            improved = true
            CONTINUE
        
        IF not improved: BREAK
    
    RETURN walk

// ─────────────────────────────────────────────────
// Multi-start verzija (koristi se po defaultu)
// ─────────────────────────────────────────────────
ASPW_MULTI_START(Graph G):
    best = null
    
    // Isprobaj različite vrijednosti α
    FOR α in [0.0, 0.1, 0.2, 0.3, 0.4, 0.5]:
        result = ASPW(G, α, considerReturn=false)
        IF result.cost < best.cost: best = result
        
        result = ASPW(G, α, considerReturn=true)
        IF result.cost < best.cost: best = result
    
    // Isprobaj i čisti nearest neighbor
    result = GREEDY_ASPW(G)
    IF result.cost < best.cost: best = result
    
    RETURN best
```

## 💡 Ključne ideje

1. **MCW-specifična**: Eksplicitno koristi shortest paths, ne direktne bridove

2. **Parametar α (alpha)**:
   - α = 0: Pure greedy (najbliži čvor)
   - α = 0.5: Balans udaljenost + centralnost
   - α = 1: Strateški odabir hubova

3. **Usputno pokrivanje**: Ako shortest path prolazi kroz nepokrivene čvorove, i oni se pokrivaju

4. **3-fazni pristup**: Construction → Closing → Local Search

## 📊 Tipični rezultati

| Veličina (N) | Prosječni gap | Vrijeme |
|--------------|---------------|---------|
| 10 | 5-10% | 0.5ms |
| 20 | 8-15% | 2-5ms |
| 50 | 10-18% | 20-50ms |

## 🔧 Lokalni operatori

### 2-opt
```
Reverse segment [i+1, j]:
Before: ... → a → b → ... → c → d → ...
After:  ... → a → c → ... → b → d → ...
```

### Shortcut
```
IF segment [a → ... → b] has nodes covered elsewhere:
    Replace with shortest_path(a, b)
```

### Node Removal
```
IF node x appears multiple times in walk:
    Try removing one occurrence and bridging with shortest path
```

## 🎓 Uloga u diplomskom radu
**Originalni doprinos** - heuristika dizajnirana specifično za MCW problem, za razliku od prilagođenih TSP heuristika.
