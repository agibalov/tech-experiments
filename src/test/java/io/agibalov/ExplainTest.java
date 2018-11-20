package io.agibalov;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.MySQLContainer;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;

public class ExplainTest {
    @ClassRule
    public final static MySQLContainer MYSQL_CONTAINER = new MySQLContainer();

    @Test
    public void canUseExplainToCompareQueries() throws IOException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(new DriverManagerDataSource(
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

        System.out.println("WITHOUT INDEX");
        double costWithoutIndex = explain(jdbcTemplate, "select * from notes where authorId = 5");

        jdbcTemplate.update("create index NotesAuthorId on notes (authorId)");

        System.out.println("WITH INDEX");
        double costWithIndex = explain(jdbcTemplate, "select * from notes where authorId = 5");

        assertTrue(costWithoutIndex > 5 * costWithIndex);
    }

    private static double explain(JdbcTemplate jdbcTemplate, String query) throws IOException {
        String explanation = jdbcTemplate.queryForObject(String.format("explain format=json %s", query), String.class);
        System.out.printf("explanation:\n%s\n", explanation);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode tree = objectMapper.readTree(explanation);
        return tree.get("query_block").get("cost_info").get("query_cost").asDouble();
    }
}
