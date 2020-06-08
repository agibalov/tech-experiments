package io.agibalov;

import java.util.Map;

public interface TestDataWriter {
    void write(TableName tableName, Map<String, Object> row);
}
