package com.example.promptlib.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MockAiService implements AiService {

    private static final Logger log = LoggerFactory.getLogger(MockAiService.class);

    @Override
    public String generate(String category, String prompt) {
        log.info("Generating Mock response for category: {} with prompt length: {}", category, prompt.length());
        
        // Let's add a short delay to simulate AI response time
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String lowerPrompt = prompt.toLowerCase();

        switch (category.toLowerCase()) {
            case "code":
                if (lowerPrompt.contains("rest") || lowerPrompt.contains("controller")) {
                    return getMockRestControllerCode();
                } else if (lowerPrompt.contains("service") || lowerPrompt.contains("business")) {
                    return getMockServiceCode();
                } else {
                    return getMockGenericCode();
                }
            case "documentation":
                if (lowerPrompt.contains("api") || lowerPrompt.contains("swagger")) {
                    return getMockApiDoc();
                } else {
                    return getMockReadmeDoc();
                }
            case "testing":
                if (lowerPrompt.contains("unit") || lowerPrompt.contains("mockito")) {
                    return getMockUnitTestCode();
                } else {
                    return getMockIntegrationTestCode();
                }
            case "design":
                if (lowerPrompt.contains("system") || lowerPrompt.contains("architecture")) {
                    return getMockSystemDesign();
                } else {
                    return getMockDbSchemaDesign();
                }
            default:
                return "Mock AI Output:\nReceived prompt: \"" + prompt + "\"\nCategory: " + category + "\n\nThis is a mock response. Configure spring.ai.openai.api-key to enable real OpenAI interactions.";
        }
    }

    private String getMockRestControllerCode() {
        return """
package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product savedProduct = productService.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return productService.update(id, product)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
""";
    }

    private String getMockServiceCode() {
        return """
package com.example.demo.service;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public Optional<Product> update(Long id, Product productDetails) {
        return productRepository.findById(id).map(existingProduct -> {
            existingProduct.setName(productDetails.getName());
            existingProduct.setPrice(productDetails.getPrice());
            existingProduct.setDescription(productDetails.getDescription());
            return productRepository.save(existingProduct);
        });
    }

    public boolean deleteById(Long id) {
        return productRepository.findById(id).map(product -> {
            productRepository.delete(product);
            return true;
        }).orElse(false);
    }
}
""";
    }

    private String getMockGenericCode() {
        return """
public class Calculator {

    public int add(int a, int b) {
        return a + b;
    }

    public int subtract(int a, int b) {
        return a - b;
    }

    public int multiply(int a, int b) {
        return a * b;
    }

    public double divide(int a, int b) {
        if (b == 0) {
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        return (double) a / b;
    }
}
""";
    }

    private String getMockApiDoc() {
        return """
# Product API Specification

The Product API allows clients to manage the inventory products, perform CRUD operations, and search for items.

## Base URL
`/api/products`

## Endpoints

### 1. Retrieve All Products
* **URL:** `/api/products`
* **Method:** `GET`
* **Headers:** `Accept: application/json`
* **Response Status:** `200 OK`
* **Response Body:**
```json
[
  {
    "id": 1,
    "name": "Mechanical Keyboard",
    "price": 99.99,
    "description": "RGB mechanical keyboard with red switches"
  }
]
```

### 2. Create Product
* **URL:** `/api/products`
* **Method:** `POST`
* **Headers:** `Content-Type: application/json`
* **Request Body:**
```json
{
  "name": "Wireless Mouse",
  "price": 49.99,
  "description": "Ergonomic wireless mouse"
}
```
* **Response Status:** `201 Created`
* **Response Body:**
```json
{
  "id": 2,
  "name": "Wireless Mouse",
  "price": 49.99,
  "description": "Ergonomic wireless mouse"
}
```
""";
    }

    private String getMockReadmeDoc() {
        return """
# AI Prompt Library App

A Spring Boot & Angular based application that acts as a prompt hub, utilizing Spring AI to generate, evaluate, version, and compare code structures.

## Installation
1. Clone the repository.
2. Configure your properties in `application.properties`.
3. Run the Backend using Maven: `./mvnw spring-boot:run`.
4. Navigate to `frontend/` and run `npm install` and `npm start`.

## Features
- **Prompt Library**: Pre-built templates for generating code, unit tests, database migrations, and markdown documentation.
- **Side-by-Side Comparison**: Review and compare multiple version outputs generated from the same prompt execution context.
- **Review Notes Logging**: Highlight code quality, bugs, security issues, and suggest improvements.
""";
    }

    private String getMockUnitTestCode() {
        return """
package com.example.demo.service;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById_Success() {
        Product mockProduct = new Product(1L, "Widget", 19.99, "A cool widget");
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        Optional<Product> result = productService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Widget", result.get().getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_NotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Product> result = productService.findById(99L);

        assertFalse(result.isPresent());
        verify(productRepository, times(1)).findById(99L);
    }
}
""";
    }

    private String getMockIntegrationTestCode() {
        return """
package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void testCreateAndGetProduct() throws Exception {
        Product product = new Product(null, "Laptop", 1200.0, "High end gaming laptop");

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }
}
""";
    }

    private String getMockSystemDesign() {
        return """
# E-commerce Architecture System Design

## Overview
A scalable, microservice-based architecture deployment system utilizing Spring Boot services, an API Gateway, Eureka discovery, Kafka messaging bus, and PostgreSQL/Redis storage solutions.

## Architecture Diagram
```
   [ Angular SPA ]
          |  HTTP/REST
          v
   [ Spring Cloud Gateway ]
          |
    +-----+-----+
    |           |
    v           v
[Product]   [Order Service] ---> (Kafka Broker) ---> [Notification Service]
    |           |                                            |
 (Postgres)  (Postgres)                                    (Email)
    ^           ^
    |           |
 [Redis Cache] [Redis Cache]
```

## Scaling Strategy
1. **Caching**: Redis caches hot database hits for products (10m TTL).
2. **Asynchronous Processing**: Order notifications, invoice PDF generators, and transaction alerts are sent via Kafka topics.
3. **Database Sharding**: Partition databases by Customer ID or Region for order processing logs.
""";
    }

    private String getMockDbSchemaDesign() {
        return """
# DB Schema Design: E-Commerce Catalog

## Entity Relationship Diagram
```
   +---------------+          +-------------------+
   |   PRODUCTS    |          |    CATEGORIES     |
   +---------------+          +-------------------+
   | id (PK)       |<-------->| id (PK)           |
   | category_id   | (Many-1) | name              |
   | name          |          | slug              |
   | description   |          +-------------------+
   | price         |
   | stock_qty     |
   | created_at    |
   +---------------+
          | (1-Many)
          v
   +---------------+
   | PRODUCT_IMGS  |
   +---------------+
   | id (PK)       |
   | product_id    | (FK)
   | image_url     |
   | display_order |
   +---------------+
```

## DDL Queries
```sql
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT REFERENCES categories(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock_qty INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```
""";
    }
}
