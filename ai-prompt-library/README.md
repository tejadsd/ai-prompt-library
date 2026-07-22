# AI Prompt Library & Studio

A premium, full-stack developer workspace for generating, reviewing, versioning, and comparing AI outputs (code, documentation, test cases, and system designs) using **Java + Spring Boot + Spring AI** on the backend and **Angular** on the frontend.

---

## 🚀 Overview

**AI Prompt Studio** is a self-contained hub designed to manage AI prompt interactions. Developers can pick pre-configured templates for common tasks (like generating controllers, unit tests, database schemas, or README docs), execute them via a backend connected to OpenAI or Ollama, write custom review notes, rate outputs, and save executions.

### Key Features Showcase:
1. **Prompt Templates**: Predefined prompts for Code, Documentation, Testing, and Design categories.
2. **Version History & Control**: Save multiple outputs for the same prompt template, incrementing version numbers.
3. **Side-by-Side Comparison**: Load and compare different version outputs of the same prompt in a split-editor view to easily track modifications.
4. **Developer Review System**: Rate output utility (Useful 👍 / Needs Work 👎) and log specific code quality improvements or bugs to the database.
5. **Offline Mock Mode**: Runs out of the box without requiring any API keys by using a mock generative service, enabling instant UI testing.

---

## 🛠️ Tech Stack

### Backend:
* **Java 17**
* **Spring Boot 3.3.2** (Parent BOM)
* **Spring AI 1.0.0-M1** (supporting generic `ChatModel` bindings)
* **Spring Data JPA**
* **H2 Database** (File-based persistence, storing data locally inside the `./db` project folder)
* **Lombok**

### Frontend:
* **Angular 19+** (Standalone Components architecture)
* **Angular Signals** (reactive state management)
* **Vanilla CSS** (sleek, dark glassmorphism design system)
* **HTTP Client Module** (communicating with REST endpoints)

---

## 📋 Prompt Examples

Here are some sample prompt templates included in the studio:

### 1. Code Generation
* **Prompt**: *"Create a Spring Boot REST API controller for Product Inventory. Include endpoints to find all products, find a product by ID, create a product, update a product, and delete a product. Use standard Spring annotations."*
* **Response Output**: A fully-functional REST Controller class with dependency injection and CRUD mapping annotations.

### 2. Unit Testing
* **Prompt**: *"Write JUnit 5 unit tests using Mockito to test a ProductService class. Test the findProductById method for both success (product found) and failure (product not found) cases."*
* **Response Output**: A mock-driven test class utilizing `@Mock` and `@InjectMocks` with assertion checks.

---

## 📝 My Review Notes (Bugs & Improvements Implemented)

During the creation of this project, several architectural improvements and bug fixes were introduced:
1. **API Fallback Design (The "No API Key" issue)**:
   * *Problem*: If a user starts the Spring Boot backend without setting `spring.ai.openai.api-key` in `application.properties`, the application fails to start up or crashes during execution.
   * *Solution*: Created a dual-implementation strategy using `MockAiService` and `SpringAiService`. The `SpringAiService` accepts `ChatModel` as an optional autowired dependency. If the bean is not present (or crashes due to bad credentials), the service gracefully falls back to mock responses, enabling seamless offline evaluation.
2. **Persistent H2 Storage**:
   * *Problem*: Standard H2 setups use in-memory databases (`jdbc:h2:mem:testdb`), losing all prompt history, rating states, and review notes when the JVM restarts.
   * *Solution*: Configured file-based H2 database pathing (`jdbc:h2:file:./db/promptlib`) so that prompt histories are preserved across developer sessions.
3. **Automated MD5 Version Grouping**:
   * *Problem*: Requiring users to manage prompt links manually to show versioning is tedious.
   * *Solution*: Implemented a hash-keying method on prompt inputs. Sending the same prompt automatically retrieves the max version count and increments it (e.g. `V1 -> V2`), automatically clustering them under the same group key for comparison.

---

## 🚀 How to Run the Project

### Prerequisites
* Java 17 or higher installed (`java -version`)
* Node.js v20.x / npm v10.x or higher installed (`node -v`)

### Step 1: Run the Backend
1. Open a terminal in the `./backend` directory.
2. Build and run the project using the Maven Wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
3. The server will launch on port `8080`.
4. *Optional*: Inspect the H2 Database console at `http://localhost:8080/h2-console` using:
   * **JDBC URL**: `jdbc:h2:file:./db/promptlib`
   * **Username**: `sa`
   * **Password**: *(Leave blank)*

### Step 2: Configure Real AI Models (Optional)
To connect the application to a real AI model, open `backend/src/main/resources/application.properties`, uncomment, and provide values for either:

**OpenAI**:
```properties
spring.ai.openai.api-key=your-actual-api-key-here
spring.ai.openai.chat.options.model=gpt-4o
```

**Ollama (Local LLM)**:
```properties
spring.ai.ollama.chat.options.model=llama3
spring.ai.ollama.base-url=http://localhost:11434
```

### Step 3: Run the Frontend
1. Open a terminal in the `./frontend` directory.
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the Angular development server:
   ```bash
   npm start
   ```
4. Navigate to `http://localhost:4200` in your web browser.

---

## 🔮 Future Enhancements

1. **Kafka real-time streaming**: Stream tokens back to the Angular client chunk-by-chunk for a smoother ChatGPT-style typing effect rather than waiting for the entire output.
2. **MongoDB integration**: Transition from relational schema to a document store for storing prompt templates and metadata dynamically.
3. **Multiple Models Compare**: Compare outputs generated by different models (e.g., Llama 3 vs. GPT-4o) side-by-side.
