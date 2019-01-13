package io.agibalov;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.MySQLContainer;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class ExplainTest {
    @ClassRule
    public final static MySQLContainer MYSQL_CONTAINER = new MySQLContainer();

    private JdbcTemplate jdbcTemplate;

    @Before
    public void init() {
        jdbcTemplate = new JdbcTemplate(new DriverManagerDataSource(
                MYSQL_CONTAINER.getJdbcUrl(),
                MYSQL_CONTAINER.getUsername(),
                MYSQL_CONTAINER.getPassword()));
        jdbcTemplate.execute("drop table if exists notes");
        jdbcTemplate.execute("create table notes(id int, authorId int not null, primary key (id))");

        jdbcTemplate.batchUpdate("insert into notes(id, authorId) values(?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, i);
                ps.setInt(2, i % 7);
            }

            @Override
            public int getBatchSize() {
                return 100;
            }
        });

        jdbcTemplate.execute("optimize table notes");
    }

    @Test
    public void filterWithIndexIsFasterThanFilterWithFullScan() {
        System.out.println("WITHOUT INDEX");
        Explanation withoutIndex = explain(jdbcTemplate, "select * from notes where authorId = 5");
        assertEquals("ALL", withoutIndex.getAccessType());
        assertNull(withoutIndex.getKey());

        jdbcTemplate.update("create index NotesAuthorId on notes (authorId)");

        System.out.println("WITH INDEX");
        Explanation withIndex = explain(jdbcTemplate, "select * from notes where authorId = 5");
        assertEquals("ref", withIndex.getAccessType());
        assertEquals("NotesAuthorId", withIndex.getKey());

        assertTrue(withoutIndex.getQueryCost() > 5 * withIndex.getQueryCost());
    }

    @Test
    public void lookUpByPrimaryKeyIsFast() {
        Explanation byPrimaryKey = explain(jdbcTemplate, "select * from notes where id = 13");
        assertEquals("const", byPrimaryKey.getAccessType());
        assertEquals("PRIMARY", byPrimaryKey.getKey());
    }

    @SneakyThrows
    private static Explanation explain(JdbcTemplate jdbcTemplate, String query) {
        String explanation = jdbcTemplate.queryForObject(String.format("explain format=json %s", query), String.class);
        System.out.printf("explanation:\n%s\n", explanation);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode tree = objectMapper.readTree(explanation);

        return Explanation.builder()
                .queryCost(tree.get("query_block").get("cost_info").get("query_cost").asDouble())
                .accessType(tree.get("query_block").get("table").get("access_type").asText())
                .key(tree.get("query_block").get("table").path("key").asText(null))
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Explanation {
        private double queryCost;
        private String accessType;
        private String key;
    }
}
