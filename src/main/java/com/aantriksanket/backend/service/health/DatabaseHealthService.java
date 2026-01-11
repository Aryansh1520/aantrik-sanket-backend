package com.aantriksanket.backend.service.health;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Service
public class DatabaseHealthService {

    private final DataSource dataSource;

    public DatabaseHealthService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, Object> check() {
        Map<String, Object> result = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            result.put("status", "UP");
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
        }

        return result;
    }
}
