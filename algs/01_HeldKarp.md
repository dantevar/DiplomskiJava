# Held-Karp DP (Dinamičko programiranje)

## 📍 Lokacija
`src/main/java/fer/ClosedWalkSolver.java`

## 📊 Karakteristike
| Svojstvo | Vrijednost |
|----------|------------|
| Tip | Egzaktni |
| Složenost (vrijeme) | O(n² · 2ⁿ) |
| Složenost (memorija) | O(n · 2ⁿ) |
| Optimalnost | ✅ Garantirana |
| Praktični limit | N ≤ 20-22 |

## 🎯 Opis
Held-Karp algoritam koristi **dinamičko programiranje s bitmaskama** za rješavanje TSP/MCW problema. Umjesto isprobavanja svih n! permutacija, koristi činjenicu da optimalni put do čvora j koji prolazi kroz skup čvorova S ovisi samo o S i j, ne o redoslijedu posjeta u S.

## 📝 Pseudokod

```
HELD_KARP(Graph G):
    n = broj čvorova
    dp[mask][j] = minimalni trošak da posjetim čvorove u "mask" i završim u j
    parent[mask][j] = prethodni čvor za rekonstrukciju puta
    
    // Inicijalizacija
    dp[{0}][0] = 0  // samo čvor 0, trošak 0
    FOR all other states:
        dp[mask][j] = ∞
    
    // DP gradnja - iteriramo po svim mogućim maskama
    FOR each mask from 1 to 2^n - 1:
        IF 0 ∉ mask: 
            CONTINUE  // čvor 0 mora biti u svakoj maski
        
        FOR each j in mask (j ≠ 0):
            prevMask = mask \ {j}  // maska bez čvora j
            
            FOR each k in prevMask:
                newCost = dp[prevMask][k] + minDist[k][j]
                
                IF newCost < dp[mask][j]:
                    dp[mask][j] = newCost
                    parent[mask][j] = k
    
    // Zatvaranje ciklusa - vratiti se na 0
    fullMask = {0, 1, 2, ..., n-1}
    result = ∞
    lastNode = -1
    
    FOR each j from 1 to n-1:
        totalCost = dp[fullMask][j] + minDist[j][0]
        IF totalCost < result:
            result = totalCost
            lastNode = j
    
    // Rekonstrukcija puta
    path = reconstructPath(parent, fullMask, lastNode)
    
    RETURN (result, path)

RECONSTRUCT_PATH(parent, mask, lastNode):
    path = []
    current = lastNode
    currentMask = mask
    
    WHILE currentMask ≠ {0}:
        path.prepend(current)
        prev = parent[currentMask][current]
        currentMask = currentMask \ {current}
        current = prev
    
    path.prepend(0)
    path.append(0)  // zatvori ciklus
    
    RETURN path
```

## 💡 Ključne ideje

1. **Bitmaska reprezentacija**: Broj `mask` predstavlja skup posjećenih čvorova
   - bit i je 1 ako je čvor i posjećen
   - Primjer: mask=5 (binarno 101) = posjećeni čvorovi {0, 2}

2. **Optimalna podstruktura**: 
   - Najkraći put koji završava u j i posjećuje čvorove u S
   - = min over k∈S\{j}: (najkraći put do k kroz S\{j}) + dist(k,j)

3. **Razlika od TSP**: Koristi `min_distances` (Floyd-Warshall najkraći putevi) umjesto direktnih bridova

## 🔧 Paralelizacija
`ClosedWalkSolverParallel.java` paralelizira DP po maskama istog "popcount-a" (broja bitova).

## 📈 Primjer
Za N=10:
- Brute Force: 10! = 3,628,800 stanja
- Held-Karp: 2^10 × 10 = 10,240 stanja
- **Ubrzanje: ~350x**
