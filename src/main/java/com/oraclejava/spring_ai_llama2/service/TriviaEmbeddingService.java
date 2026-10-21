package com.oraclejava.spring_ai_llama2.service;

import oracle.sql.VECTOR;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service
public class TriviaEmbeddingService {
    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;

    public TriviaEmbeddingService(EmbeddingModel embeddingModel, JdbcTemplate jdbcTemplate) {
        this.embeddingModel = embeddingModel;
        this.jdbcTemplate = jdbcTemplate;
    }

    public int generateEmbedings() {
        // 1. 아직 임베딩하지 않은 facts 조회
        String selectSql = """
            SELECT pk, facts
            FROM trivia_vec
            WHERE vec is null
              and facts is not null
            ORDER BY pk
        """;

        List<Map<String, Object>> rows =
                jdbcTemplate.queryForList(selectSql);

        int count = 0;

        for (Map<String, Object> row : rows) {
            int pk =  ((Number) row.get("pk")).intValue();
            String facts = row.get("facts").toString();

            // 2. Spring AI -> Ollama 임베딩
            float[] vector = embeddingModel.embed(facts);

            if (vector.length != 768) {
                throw new IllegalStateException("Invalid vector length: " + vector.length);
            }

            // 3. Oracle VECTOR 데이터 생성
            try {
                VECTOR oracleVector = VECTOR.ofFloat32Values(vector);

                // 4. Oracle 업데이트
                jdbcTemplate.update("UPDATE trivia_vec SET vec = ? WHERE pk = ?",
                        oracleVector,
                        pk);
                count++;
                System.out.println("Embedding PK: " + pk
                    + ", dimensions: " + vector.length);
            } catch (SQLException e) {
                throw new IllegalStateException(
                        "Vector conversion failed for PK: " + pk, e
                );
            }
        }

        return count;
    }

    public List<Map<String, Object>> searchTriviaEmbeddings(String query) {
        float[] vector = embeddingModel.embed(query);
        try {
            VECTOR oracleVector = VECTOR.ofFloat32Values(vector);

            String sql = """
                    SELECT pk, facts
                    FROM TRIVIA_VEC tv\s
                    ORDER BY vector_distance(vec, ?, cosine)
                    FETCH FIRST 4 ROWS ONLY
                    """;
            List<Map<String, Object>> results =
                    jdbcTemplate.queryForList(sql, oracleVector);
            return results;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
