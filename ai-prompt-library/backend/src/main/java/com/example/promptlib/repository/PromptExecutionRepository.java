package com.example.promptlib.repository;

import com.example.promptlib.model.PromptExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromptExecutionRepository extends JpaRepository<PromptExecution, Long> {

    List<PromptExecution> findAllByOrderByCreatedAtDesc();

    List<PromptExecution> findByCategoryOrderByCreatedAtDesc(String category);

    List<PromptExecution> findByPromptKeyOrderByVersionDesc(String promptKey);

    @Query("SELECT COALESCE(MAX(pe.version), 0) FROM PromptExecution pe WHERE pe.promptKey = :promptKey")
    int findMaxVersionByPromptKey(@Param("promptKey") String promptKey);

    List<PromptExecution> findByPromptKey(String promptKey);
}
