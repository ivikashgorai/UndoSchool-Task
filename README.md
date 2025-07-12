# UndoSchool - Spring Boot + Elasticsearch Course Search

This project is a Spring Boot application that indexes and searches educational course data using **Elasticsearch**. It fulfills a multi-part assignment that includes search, filtering, sorting, autocomplete, and fuzzy search.

---

##  Project Structure

```
UndoSchool/
├── src/
│   ├── main/
│   │   ├── java/com/elasticsearch/spring/UndoSchool/
│   │   │   ├── config/            # Configuration classes (e.g., ElasticsearchConfig)
│   │   │   ├── controllers/       # REST endpoints
│   │   │   ├── dto/               # Filter parameters
│   │   │   ├── entity/            # CourseDocument with mappings
│   │   │   ├── repositories/      # Elasticsearch client integration
│   │   │   ├── service/           # Search & suggestion logic
│   │   │   └── startup/           # Data loader on startup
│   │   └── resources/
│   │       └── sample-courses.json   # Sample data (50+ courses)
│
│   └── test/
│       └── java/com/elasticsearch/spring/UndoSchool/integration/
│           └── CourseSearchIntegrationTest.java   # Integration test example
│
├── docker-compose.yml   # Elasticsearch container setup
└── README.md            # Project instructions
```

---

##  How to Run the Project

### ⚙️ Prerequisites

Make sure the following are installed on your machine:

* **Docker**: [https://www.docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop)
  (Ensure Docker Desktop is running before you continue)
* **Java 21**
* **Maven** (or use `./mvnw` wrapper)

> 💡 If `docker-compose up` fails, it likely means Docker is not installed or running.

---

###  Step 1: Start Elasticsearch Container

Start Elasticsearch using Docker Compose:

```bash
docker-compose up -d
```

Verify Elasticsearch is running:

```bash
curl -X GET "http://localhost:9200"
```

---

### Step 2: Run Spring Boot Application

Run the Spring Boot application using Maven Wrapper:

```bash
./mvnw spring-boot:run
```

This will:

* Load and index `sample-courses.json`
* Expose API endpoints at: [http://localhost:8080/api/search](http://localhost:8080/api/search)

---

##  Assignment A: Search Courses

###  Endpoint

```http
GET /api/search
```

###  Parameters Supported

| Parameter   | Description                                                          |
| ----------- | -------------------------------------------------------------------- |
| `q`         | Keyword for title + description search                               |
| `category`  | Exact match (e.g., `"Science"`)                                      |
| `type`      | Exact match (e.g., `COURSE`, `CLUB`, `ONE_TIME`)                     |
| `minAge`    | Minimum age range filter                                             |
| `maxAge`    | Maximum age range filter                                             |
| `minPrice`  | Minimum price                                                        |
| `maxPrice`  | Maximum price                                                        |
| `startDate` | Filter courses on/after ISO-8601 date                                |
| `sort`      | Sort by `upcoming`, `priceAsc`, or `priceDesc` (default: `upcoming`) |
| `page`      | Page number (default: `0`)                                           |
| `size`      | Page size (default: `10`)                                            |

###  Sample Calls

```bash
curl "http://localhost:8080/api/search?type=COURSE&minAge=8&maxAge=12&size=1"
```

```bash
curl "http://localhost:8080/api/search?sort=priceAsc&page=0&size=5"
```

###  Expected Response Format

```json
{
  "courses": [
    {
      "id": "course-22",
      "title": "Astronomy Nights",
      "description": "This is a detailed course about Astronomy Nights.",
      "category": "Technology",
      "type": "COURSE",
      "gradeRange": "4th–6th",
      "minAge": 8,
      "maxAge": 11,
      "price": 72.08,
      "nextSessionDate": "2025-08-06T00:00:00Z",
      "suggest": {
        "input": ["Astronomy Nights"],
        "contexts": null,
        "weight": null
      }
    }
  ],
  "total": 1
}
```

---

##  Assignment B (Bonus): Autocomplete & Fuzzy Search

###  Autocomplete Feature

**Endpoint:**

```http
GET /api/search/suggest?q={partialTitle}
```

**Example:**

```bash
curl "http://localhost:8080/api/search/suggest?q=mat"
```

**Response:**

```json
[
  "Math Explorers",
  "Math Olympiad Prep"
]
```

---

###  Fuzzy Matching

**Example:**

```bash
curl "http://localhost:8080/api/search?q=Spanes"
```

- Matches course titles and descriptions using autocomplete and fuzzy search.

**Response:**

```json
{
  "courses": [
    {
      "id": "course-10",
      "title": "Space Camp",
      "description": "This is a detailed course about Space Camp.",
      "category": "Technology",
      "type": "COURSE",
      "gradeRange": "2nd–4th",
      "minAge": 10,
      "maxAge": 14,
      "price": 190.54,
      "nextSessionDate": "2025-09-11T00:00:00Z",
      "suggest": {
        "input": ["Space Camp"],
        "contexts": null,
        "weight": null
      }
    },
    {
      "id": "course-47",
      "title": "Spanish Starter",
      "description": "This is a detailed course about Spanish Starter.",
      "category": "Sports",
      "type": "ONE_TIME",
      "gradeRange": "3rd–5th",
      "minAge": 8,
      "maxAge": 11,
      "price": 155.24,
      "nextSessionDate": "2025-10-27T00:00:00Z",
      "suggest": {
        "input": ["Spanish Starter"],
        "contexts": null,
        "weight": null
      }
    }
  ],
  "total": 2
}
```

---

##  Integration Tests

Integration tests are provided using **Testcontainers**.

To run the tests:

```bash
./mvnw test
```

---

##  Technologies Used

* **Spring Boot** 3.5
* **Elasticsearch** 8.13
* **Java** 21
* **Jackson** + JSR310
* **Testcontainers** (for integration tests)
* **Docker Compose** (for Elasticsearch runtime)
