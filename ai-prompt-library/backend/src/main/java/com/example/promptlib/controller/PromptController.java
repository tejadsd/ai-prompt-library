package com.example.promptlib.controller;

import com.example.promptlib.dto.PromptRequest;
import com.example.promptlib.dto.PromptResponse;
import com.example.promptlib.dto.ReviewRequest;
import com.example.promptlib.model.PromptExecution;
import com.example.promptlib.repository.PromptExecutionRepository;
import com.example.promptlib.service.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow frontend access
public class PromptController {

    private static final Logger log = LoggerFactory.getLogger(PromptController.class);

    private final AiService aiService;
    private final PromptExecutionRepository repository;

    public PromptController(AiService aiService, PromptExecutionRepository repository) {
        this.aiService = aiService;
        this.repository = repository;
    }

    @PostMapping("/generateCode")
    public ResponseEntity<PromptResponse> generateCode(@RequestBody PromptRequest request) {
        return processPromptGeneration("Code", request);
    }

    @PostMapping("/generateDoc")
    public ResponseEntity<PromptResponse> generateDoc(@RequestBody PromptRequest request) {
        return processPromptGeneration("Documentation", request);
    }

    @PostMapping("/generateTestCases")
    public ResponseEntity<PromptResponse> generateTestCases(@RequestBody PromptRequest request) {
        return processPromptGeneration("Testing", request);
    }

    // Generic endpoint in case they send other categories like "Design"
    @PostMapping("/generate")
    public ResponseEntity<PromptResponse> generateGeneric(@RequestBody PromptRequest request) {
        return processPromptGeneration(request.getCategory(), request);
    }

    @GetMapping("/history")
    public ResponseEntity<List<PromptResponse>> getHistory(@RequestParam(required = false) String category) {
        List<PromptExecution> executions;
        if (category != null && !category.trim().isEmpty()) {
            executions = repository.findByCategoryOrderByCreatedAtDesc(category);
        } else {
            executions = repository.findAllByOrderByCreatedAtDesc();
        }
        return ResponseEntity.ok(executions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/history/{id}")
    public ResponseEntity<PromptResponse> getExecutionById(@PathVariable Long id) {
        return repository.findById(id)
                .map(pe -> ResponseEntity.ok(mapToResponse(pe)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/history/{id}/review")
    public ResponseEntity<PromptResponse> updateReview(
            @PathVariable Long id, 
            @RequestBody ReviewRequest request) {
        return repository.findById(id).map(pe -> {
            pe.setReviewNotes(request.getReviewNotes());
            if (request.getRating() != null) {
                pe.setRating(request.getRating());
            }
            PromptExecution saved = repository.save(pe);
            log.info("Updated review notes for execution ID: {}", id);
            return ResponseEntity.ok(mapToResponse(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/history/compare")
    public ResponseEntity<List<PromptResponse>> getVersionsForComparison(@RequestParam String promptKey) {
        List<PromptExecution> executions = repository.findByPromptKeyOrderByVersionDesc(promptKey);
        return ResponseEntity.ok(executions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/templates")
    public ResponseEntity<List<Map<String, String>>> getTemplates() {
        List<Map<String, String>> templates = new ArrayList<>();
        
        templates.add(createTemplateMap("Code", "Generate REST API", 
                "Create a Spring Boot REST API controller for Product Inventory. Include endpoints to find all products, find a product by ID, create a product, update a product, and delete a product. Use standard Spring annotations."));
        
        templates.add(createTemplateMap("Code", "JPA Entity & Repository", 
                "Write a Java JPA Entity class for a 'User' with fields for id, username, email, role, and registration date. Also provide the Spring Data JPA Repository interface."));
        
        templates.add(createTemplateMap("Documentation", "API Documentation", 
                "Write clear markdown API documentation for a '/api/products' endpoint showing request payload, success, and error response examples."));
        
        templates.add(createTemplateMap("Documentation", "Project README", 
                "Generate a README.md file for a Java and Angular based AI Prompt Library project, listing overview, technical stack, installation steps, and configuration."));
        
        templates.add(createTemplateMap("Testing", "Mockito Unit Tests", 
                "Write JUnit 5 unit tests using Mockito to test a ProductService class. Test the findProductById method for both success (product found) and failure (product not found) cases."));
        
        templates.add(createTemplateMap("Testing", "Integration Testing", 
                "Write a Spring Boot MockMvc integration test for ProductController, verifying that a POST request to '/api/products' successfully stores a product and returns a 201 Created status."));
        
        templates.add(createTemplateMap("Design", "System Architecture", 
                "Describe the system design and architecture for a high-availability, microservice-based e-commerce platform. Detail caching, database selection, and communication between services."));
        
        templates.add(createTemplateMap("Design", "DB Catalog Schema", 
                "Provide a database schema design for an e-commerce Catalog and Shopping Cart system. Show the relational tables, data types, foreign keys, and write the SQL DDL statements."));
        
        return ResponseEntity.ok(templates);
    }

    private ResponseEntity<PromptResponse> processPromptGeneration(String category, PromptRequest request) {
        log.info("Processing generation request. Category: '{}', Template: '{}'", category, request.getPromptTemplate());
        
        String inputPrompt = request.getInputPrompt();
        if (inputPrompt == null || inputPrompt.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Generate or clean promptKey
        String promptKey = request.getPromptKey();
        if (promptKey == null || promptKey.trim().isEmpty()) {
            // Group similar prompts by their hash to count versions
            promptKey = "pk_" + Math.abs(inputPrompt.hashCode());
        }

        // Generate response from AI model (or Mock)
        String aiOutput = aiService.generate(category, inputPrompt);

        // Fetch version history count
        int maxVersion = repository.findMaxVersionByPromptKey(promptKey);
        int currentVersion = maxVersion + 1;

        // Logging AI output for review/debugging
        log.info("[AI OUTPUT REVIEW LOG]");
        log.info("Execution ID Group Key: {}", promptKey);
        log.info("Category: {}", category);
        log.info("Version: {}", currentVersion);
        log.info("Input Prompt: \n{}", inputPrompt);
        log.info("AI Output Response: \n{}", aiOutput);
        log.info("[END AI OUTPUT REVIEW LOG]");

        // Save execution to DB
        PromptExecution execution = PromptExecution.builder()
                .category(category)
                .promptTemplate(request.getPromptTemplate() != null ? request.getPromptTemplate() : "Custom Prompt")
                .inputPrompt(inputPrompt)
                .outputResponse(aiOutput)
                .promptKey(promptKey)
                .version(currentVersion)
                .build();

        PromptExecution saved = repository.save(execution);

        return ResponseEntity.ok(mapToResponse(saved));
    }

    private PromptResponse mapToResponse(PromptExecution pe) {
        return PromptResponse.builder()
                .id(pe.getId())
                .category(pe.getCategory())
                .promptTemplate(pe.getPromptTemplate())
                .inputPrompt(pe.getInputPrompt())
                .outputResponse(pe.getOutputResponse())
                .reviewNotes(pe.getReviewNotes())
                .version(pe.getVersion())
                .promptKey(pe.getPromptKey())
                .rating(pe.getRating())
                .createdAt(pe.getCreatedAt())
                .build();
    }

    private Map<String, String> createTemplateMap(String category, String title, String prompt) {
        Map<String, String> map = new HashMap<>();
        map.put("category", category);
        map.put("title", title);
        map.put("prompt", prompt);
        return map;
    }
}
