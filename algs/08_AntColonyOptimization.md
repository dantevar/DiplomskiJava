# Ant Colony Optimization (ACO)

## 📍 Lokacija
`src/main/java/metaheuristika/ACO.java`

## 📊 Karakteristike
| Svojstvo | Vrijednost |
|----------|------------|
| Tip | Metaheuristika (swarm intelligence) |
| Složenost (vrijeme) | O(iterations × ants × n²) |
| Složenost (memorija) | O(n²) - feromonska matrica |
| Optimalnost | ❌ Približno |
| Tipični gap | 3-8% od optimuma |

## 🎯 Opis
ACO simulira ponašanje **kolonije mrava** u potrazi za hranom. Mravi ostavljaju **feromone** na putevima, a budući mravi preferiraju puteve s više feromona. Ovo vodi emergentnom ponašanju gdje kolonija konvergira prema dobrim rješenjima.

## 📝 Pseudokod

```
ANT_COLONY_OPTIMIZATION(Graph G, numAnts, iterations, α, β, ρ, Q):
    n = broj čvorova
    
    // ═══════════════════════════════════════════
    // INICIJALIZACIJA FEROMONA
    // ═══════════════════════════════════════════
    τ = matrica n × n
    FOR all i, j:
        τ[i][j] = 1.0  // Jednaki početni feromoni
    
    best = null
    bestCost = ∞
    
    // ═══════════════════════════════════════════
    // GLAVNA PETLJA
    // ═══════════════════════════════════════════
    FOR iter from 1 to iterations:
        
        tours = []
        costs = []
        
        // Svaki mrav gradi svoju turu
        FOR ant from 1 to numAnts:
            tour = CONSTRUCT_TOUR(τ, α, β)
            cost = EVALUATE_TOUR(tour)
            
            tours.add(tour)
            costs.add(cost)
            
            IF cost < bestCost:
                bestCost = cost
                best = tour.clone()
        
        // ─────────────────────────────────────────
        // EVAPORACIJA FEROMONA
        // ─────────────────────────────────────────
        FOR all i, j:
            τ[i][j] = (1 - ρ) * τ[i][j]
        
        // ─────────────────────────────────────────
        // DEPONIRANJE FEROMONA
        // ─────────────────────────────────────────
        FOR ant from 1 to numAnts:
            tour = tours[ant]
            cost = costs[ant]
            Δ = Q / cost  // Bolji mravi ostavljaju više
            
            FOR i from 0 to n - 1:
                from = tour[i]
                to = tour[(i + 1) mod n]
                τ[from][to] += Δ
                τ[to][from] += Δ  // Simetrično
    
    RETURN (bestCost, best)

// ─────────────────────────────────────────────────
// Konstrukcija ture jednog mrava
// ─────────────────────────────────────────────────
CONSTRUCT_TOUR(τ, α, β):
    tour = [0]  // Počni od čvora 0
    visited = {0}
    current = 0
    
    WHILE |visited| < n:
        next = SELECT_NEXT_CITY(current, visited, τ, α, β)
        tour.add(next)
        visited.add(next)
        current = next
    
    RETURN tour

// ─────────────────────────────────────────────────
// Probabilistički odabir sljedećeg grada
// ─────────────────────────────────────────────────
SELECT_NEXT_CITY(current, visited, τ, α, β):
    // Izračunaj vjerojatnosti za sve neposjećene
    probabilities = []
    totalSum = 0
    
    FOR each j not in visited:
        // Feromonska komponenta
        pheromone = τ[current][j]^α
        
        // Heuristička komponenta (obrnuto proporcionalno udaljenosti)
        heuristic = (1 / dist[current][j])^β
        
        // Kombinirana privlačnost
        attractiveness = pheromone * heuristic
        probabilities[j] = attractiveness
        totalSum += attractiveness
    
    // Normaliziraj u vjerojatnosti
    FOR each j:
        probabilities[j] /= totalSum
    
    // Roulette wheel selekcija
    r = random(0, 1)
    cumulative = 0
    
    FOR each j not in visited:
        cumulative += probabilities[j]
        IF cumulative >= r:
            RETURN j
    
    // Fallback
    RETURN first unvisited

// ─────────────────────────────────────────────────
// Evaluacija ture
// ─────────────────────────────────────────────────
EVALUATE_TOUR(tour):
    cost = 0
    FOR i from 0 to |tour| - 1:
        from = tour[i]
        to = tour[(i + 1) mod |tour|]
        cost += minDist[from][to]
    RETURN cost
```

## 💡 Ključne ideje

### 1. Feromonska formula
```
p(i→j) = [τ(i,j)^α × η(i,j)^β] / Σ[τ(i,k)^α × η(i,k)^β]

gdje:
  τ(i,j) = količina feromona na bridu (i,j)
  η(i,j) = heuristička poželjnost = 1/dist(i,j)
  α = važnost feromona
  β = važnost heuristike
```

### 2. Uloga parametara
```
α (alpha) - utjecaj feromona:
  α = 0: Ignoriraj feromone, čista heuristika
  α visok: Jako prati tragove prethodnih mrava

β (beta) - utjecaj heuristike:
  β = 0: Ignoriraj udaljenost, samo feromoni
  β visok: Preferira kratke bridove

ρ (rho) - stopa evaporacije:
  ρ blizu 0: Feromoni se dugo zadržavaju
  ρ blizu 1: Feromoni brzo nestaju
  
Q - konstanta deponiranja:
  Δτ = Q / tourCost
  Veći Q = jači feromoni
```

### 3. Balans eksploracija/eksploatacija
```
Visoki feromoni → Eksploatacija (svi mravi slijede isti put)
Evaporacija → Eksploracija (zaboravlja stare puteve)

Bez evaporacije: Konvergencija ka suboptimumu
Previše evaporacije: Nema učenja
```

## 📊 Tipični parametri

| Parametar | Oznaka | Tipična vrijednost |
|-----------|--------|-------------------|
| Broj mrava | numAnts | 10-50 |
| Iteracije | iterations | 100-500 |
| α | alpha | 1.0 |
| β | beta | 2.0-5.0 |
| Evaporacija | ρ | 0.1-0.5 |
| Q | Q | 100 |

## 📈 Konvergencija

```
Tipična evolucija:
Iter 1:   Feromoni uniformni, mravi random
Iter 10:  Počinju se formirati "autoceste"
Iter 50:  Jasni preferentni putevi
Iter 100: Konvergencija, većina mrava slijedi iste rute
```

## 🔧 Varijante

### Elitist AS
```
// Samo najbolji globalni mrav deponira
τ[i][j] += (1-ρ) * τ[i][j] + Δ_best
```

### Max-Min AS
```
// Ograniči feromone
τ_min ≤ τ[i][j] ≤ τ_max
// Sprječava preuranjenu konvergenciju
```

### Ant Colony System (ACS)
```
// Lokalno pravilo ažuriranja
τ[i][j] = (1-ξ) * τ[i][j] + ξ * τ_0
// Potiče eksploraciju
```

## ⚠️ Zašto ACO radi dobro za MCW/TSP?

1. **Grafni problem**: ACO je dizajniran za grafove
2. **Konstrukcija rješenja**: Prirodno gradi puteve
3. **Implicitna memorija**: Feromoni pamte dobre odluke
4. **Paralelizam**: Svi mravi rade simultano

## 🎓 Uloga u diplomskom radu
Jedan od najboljih algoritama za MCW - pokazuje moć swarm intelligence pristupa za probleme rutiranja.
