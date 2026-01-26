# MyAlg BFS (Naivni pristup)

## 📍 Lokacija
`src/main/java/fer/MyAlg.java`

## 📊 Karakteristike
| Svojstvo | Vrijednost |
|----------|------------|
| Tip | Egzaktni |
| Složenost (vrijeme) | O(eksponencijalno) |
| Složenost (memorija) | O(eksponencijalno) - priority queue |
| Optimalnost | ✅ Garantirana |
| Praktični limit | N ≤ 10-12 |

## 🎯 Opis
**Naivni pristup** koji istražuje prostor stanja korištenjem BFS/Dijkstra pristupa. Svako stanje je par (trenutni čvor, maska posjećenih). Koristi priority queue za best-first pretragu.

**Uloga u radu**: Motivacija za bolje algoritme - pokazuje zašto jednostavan pristup ne skalira.

## 📝 Pseudokod

```
BFS_WALK(Graph G):
    n = broj čvorova
    maxLength = 2*n - 2 + 1  // Maksimalna duljina walka
    
    best = ∞
    bestWalk = null
    
    // Memoizacija: (state) -> najmanji cost za to stanje
    visited = HashMap<Long, Double>
    
    // Priority queue sortirano po costu (Dijkstra stil)
    queue = PriorityQueue<Walk>(by walk.cost)
    
    // Početno stanje: na čvoru 0, cost 0, posjećen samo 0
    queue.add(Walk(node=0, cost=0, visitedMask=1, length=1))
    
    WHILE queue not empty:
        walk = queue.poll()  // Uzmi walk s najmanjim costom
        
        // Pruning 1: Heuristička granica
        estimatedTotal = walk.cost + minDist[walk.head][0]
        IF estimatedTotal >= best: 
            CONTINUE
        
        // Pruning 2: Predugi walkovi
        IF walk.length >= maxLength: 
            CONTINUE
        
        // Pruning 3: Već viđeno stanje s boljim costom
        state = encodeState(walk.visitedMask, walk.head)
        IF visited[state] exists AND visited[state] <= walk.cost:
            CONTINUE
        visited[state] = walk.cost
        
        // Goal check: svi čvorovi posjećeni?
        IF walk.coversAllNodes(n):
            totalCost = walk.cost + minDist[walk.head][0]
            IF totalCost < best:
                best = totalCost
                bestWalk = walk
            CONTINUE  // Ne expandiramo dalje
        
        // Expand: probaj sve susjede
        FOR each neighbor hop of walk.head:
            newCost = walk.cost + directDist[walk.head][hop]
            IF newCost >= best: CONTINUE
            
            newWalk = Walk(
                node = hop,
                cost = newCost,
                visitedMask = walk.visitedMask | (1 << hop),
                length = walk.length + 1,
                parent = walk
            )
            queue.add(newWalk)
    
    RETURN bestWalk

ENCODE_STATE(visitedMask, headNode):
    // Kombinira masku i trenutni čvor u jedinstveni ključ
    RETURN (visitedMask << 5) | headNode  // Pretpostavka: n < 32
```

## 💡 Ključne ideje

1. **State-space search**: Prostor stanja = (posjećeni čvorovi, trenutna pozicija)

2. **Priority Queue (Dijkstra stil)**: Uvijek expandiramo najjeftiniji walk prvi
   - Garantira da prvi pronađeni cilj ima optimalni cost DO cilja
   - Ali moramo nastaviti jer trebamo još ići do 0

3. **Memoizacija**: Ne posjećujemo isto stanje dvaput ako imamo bolji cost

4. **Walk objekt**: Pamti parent za rekonstrukciju puta

## ⚠️ Zašto je spor?

```
Broj mogućih stanja: 2^n × n (maske × pozicije)
Za N=15: 2^15 × 15 = 491,520 stanja

ALI: svako stanje može biti dostignuto na mnogo načina,
     i queue može eksplodirati prije pruning efekta.

Usporedba za N=10:
  MyAlg BFS: ~10-100ms
  Held-Karp: ~0.5-1ms
  Razlika: 10-100x sporije!
```

## 📊 Usporedba s drugim egzaktnima

| N | MyAlg BFS | Held-Karp | Branch&Bound |
|---|-----------|-----------|--------------|
| 8 | 5ms | 0.2ms | 0.4ms |
| 10 | 50ms | 0.8ms | 4ms |
| 12 | 500ms+ | 3ms | 40ms |

## 🎓 Uloga u diplomskom radu

**Poglavlje 2.1: "Prvotni pristup - BFS pretraga prostora stanja"**

1. Pokazuje intuitivno razmišljanje o problemu
2. Motivira potrebu za boljim algoritmima
3. Ilustrira eksponencijalnu složenost kombinatornih problema
4. Uvodi koncept state-space pretrage koji se koristi i drugdje

## 🔧 Paralelna verzija
`MyAlgOptimized.java` koristi `PriorityBlockingQueue` i više threadova, ali overhead paralelizacije često nadmašuje korist za mali N.
