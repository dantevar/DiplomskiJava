# MCW Algorithm Visualizer - Backend

Spring Boot REST API for Minimum Closed Walk (MCW) problem solving and visualization.

## API Endpoints

### Graph Management

#### Generate Random Graph
```http
POST /api/graph/generate
Content-Type: application/json

{
  "n": 10,
  "maxWeight": 100
}

Response:
{
  "id": "uuid",
  "n": 10,
  "distances": [[...]]
}
```

#### Upload Custom Graph
```http
POST /api/graph/upload
Content-Type: application/json

{
  "n": 5,
  "distances": [[0, 10, 15, ...], [...]]
}
```

#### Get Graph by ID
```http
GET /api/graph/{id}
```

#### List All Graphs
```http
GET /api/graph/list
```

### Algorithm Execution

#### Solve with Single Algorithm
```http
POST /api/algorithm/solve
Content-Type: application/json

{
  "graphId": "uuid",
  "algorithm": "GA",
  "parameters": {
    "populationSize": 50,
    "generations": 20,
    "crossoverRate": 0.8,
    "mutationRate": 0.2
  }
}

Response:
{
  "algorithm": "GA",
  "cost": 245.67,
  "tour": [0, 3, 1, 4, 2, 0],
  "durationMs": 1234
}
```

#### Solve with All Algorithms (Comparison)
```http
POST /api/algorithm/solve-all
Content-Type: application/json

{
  "graphId": "uuid"
}

Response:
{
  "graphId": "uuid",
  "optimalCost": 240.5,
  "solutions": {
    "HeldKarp": { "cost": 240.5, "tour": [...], "durationMs": 5000 },
    "Greedy": { "cost": 265.3, "tour": [...], "durationMs": 10 },
    "GA": { "cost": 245.67, "tour": [...], "durationMs": 1234 },
    "ACO": { "cost": 243.2, "tour": [...], "durationMs": 2345 },
    "AttentionRL": { "cost": 250.1, "tour": [...], "durationMs": 3456 }
  }
}
```

#### Get Available Algorithms
```http
GET /api/algorithm/algorithms

Response:
{
  "algorithms": [
    {
      "id": "HeldKarp",
      "name": "Held-Karp (Exact)",
      "description": "Dynamic programming - guaranteed optimal solution",
      "defaultParameters": {}
    },
    {
      "id": "GA",
      "name": "Genetic Algorithm",
      "description": "Evolutionary metaheuristic",
      "defaultParameters": {
        "populationSize": 50,
        "generations": 20,
        "crossoverRate": 0.8,
        "mutationRate": 0.2
      }
    },
    ...
  ]
}
```

### Real-time Visualization (Server-Sent Events)

#### Start Visualization Session
```http
POST /api/visualization/start
Content-Type: application/json

{
  "graphId": "uuid",
  "algorithm": "Greedy",
  "parameters": {}
}

Response:
{
  "sessionId": "session-uuid",
  "graphId": "uuid",
  "algorithm": "Greedy"
}
```

#### Stream Visualization Steps (SSE)
```http
GET /api/visualization/stream/{sessionId}
Accept: text/event-stream

Server sends events:
data: {
  "sessionId": "session-uuid",
  "stepNumber": 0,
  "currentTour": [0],
  "visited": [0],
  "currentNode": 0,
  "message": "Starting from node 0",
  "currentCost": 0.0
}

data: {
  "sessionId": "session-uuid",
  "stepNumber": 1,
  "currentTour": [0, 3],
  "visited": [0, 3],
  "currentNode": 3,
  "message": "Selected nearest neighbor: node 3 (cost: 15.5)",
  "currentCost": 15.5
}

...

data: {
  "sessionId": "session-uuid",
  "completed": true,
  "finalCost": 245.67,
  "finalTour": [0, 3, 1, 4, 2, 0]
}
```

#### Stop Visualization
```http
POST /api/visualization/stop/{sessionId}
```

## Available Algorithms

1. **HeldKarp** - Exact dynamic programming solution (O(2^n × n²))
2. **Greedy** - Nearest neighbor heuristic (O(n²))
3. **GA** - Genetic Algorithm with Order Crossover
4. **ACO** - Ant Colony Optimization
5. **AttentionRL** - Reinforcement Learning with Attention mechanism

## Running the Server

### Using Maven
```bash
mvn spring-boot:run
```

### Using Java
```bash
mvn clean package
java -jar target/fer-0.0.1-SNAPSHOT.jar
```

Server will start on `http://localhost:8080`

## CORS Configuration

The server accepts requests from:
- `http://localhost:3000` (React default)
- `http://localhost:5173` (Vite default)

## React Integration Example

```javascript
// Generate graph
const response = await fetch('http://localhost:8080/api/graph/generate', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ n: 10, maxWeight: 100 })
});
const graph = await response.json();

// Solve with GA
const solution = await fetch('http://localhost:8080/api/algorithm/solve', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    graphId: graph.id,
    algorithm: 'GA',
    parameters: { populationSize: 50, generations: 20 }
  })
});
const result = await solution.json();

// Real-time visualization
const visSession = await fetch('http://localhost:8080/api/visualization/start', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    graphId: graph.id,
    algorithm: 'Greedy'
  })
});
const { sessionId } = await visSession.json();

// Listen to SSE stream
const eventSource = new EventSource(
  `http://localhost:8080/api/visualization/stream/${sessionId}`
);

eventSource.onmessage = (event) => {
  const step = JSON.parse(event.data);
  if (step.completed) {
    console.log('Final solution:', step.finalTour, step.finalCost);
    eventSource.close();
  } else {
    console.log('Step', step.stepNumber, ':', step.message);
    // Update UI with step.currentTour, step.visited, etc.
  }
};
```

## Project Structure

```
server/
├── MCWServerApplication.java          # Main Spring Boot app
├── controller/
│   ├── GraphController.java           # Graph CRUD endpoints
│   ├── AlgorithmController.java       # Algorithm execution endpoints
│   └── VisualizationController.java   # SSE streaming endpoints
├── service/
│   ├── GraphService.java              # Graph management logic
│   ├── AlgorithmService.java          # Algorithm execution
│   └── VisualizationService.java      # Real-time streaming
└── dto/
    ├── GraphDTO.java
    ├── SolutionDTO.java
    ├── ComparisonDTO.java
    └── VisualizationStepDTO.java
```
