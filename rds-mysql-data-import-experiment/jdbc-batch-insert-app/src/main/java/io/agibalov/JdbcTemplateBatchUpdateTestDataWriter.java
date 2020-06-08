package io.agibalov;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class JdbcTemplateBatchUpdateTestDataWriter implements TestDataWriter {
    private final TableName targetTableName;
    private final String insertSqlStatement;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final int batchSize;

    private List<Map<String, Object>> batchRows = new ArrayList<>();
    private int rowCount = 0;

    public JdbcTemplateBatchUpdateTestDataWriter(
            TableName targetTableName,
            String insertSqlStatement,
            NamedParameterJdbcTemplate jdbcTemplate,
            int batchSize) {

        this.targetTableName = targetTableName;
        this.insertSqlStatement = insertSqlStatement;
        this.jdbcTemplate = jdbcTemplate;
        this.batchSize = batchSize;
    }

    @Override
    public void write(TableName tableName, Map<String, Object> row) {
        if (!tableName.equals(targetTableName)) {
            return;
        }

        batchRows.add(row);

        if (batchRows.size() == batchSize) {
            flush();
        }
    }

    public void flush() {
        if (batchRows.isEmpty()) {
            return;
        }

        long batchStartTime = System.currentTimeMillis();
        int batchRowCount = Arrays.stream(
                jdbcTemplate.batchUpdate(
                        insertSqlStatement,
                        (Map<String, ?>[]) batchRows.stream().toArray(Map[]::new))
        ).sum();

        float batchElapsedTime = (System.currentTimeMillis() - batchStartTime) / 1000.f;
        float rowsPerSecond = batchRowCount / batchElapsedTime;
        log.info("{} batch: {} rows in {} seconds ({} rows per second)",
                insertSqlStatement,
                batchRowCount,
                String.format("%.3f", batchElapsedTime),
                String.format("%.0f", rowsPerSecond));

        rowCount += batchRowCount;
        batchRows.clear();
    }

    public int getRowCount() {
        return rowCount;
    }
}
