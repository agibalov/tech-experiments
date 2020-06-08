package io.agibalov;

import com.fasterxml.jackson.databind.SequenceWriter;
import lombok.SneakyThrows;

import java.util.Map;

public class CsvFileTestDataWriter implements TestDataWriter {
    private final TableName targetTableName;
    private final SequenceWriter sequenceWriter;
    private int rowCount = 0;

    public CsvFileTestDataWriter(TableName targetTableName, SequenceWriter sequenceWriter) {
        this.targetTableName = targetTableName;
        this.sequenceWriter = sequenceWriter;
    }

    @Override
    @SneakyThrows
    public void write(TableName tableName, Map<String, Object> row) {
        if (!tableName.equals(targetTableName)) {
            return;
        }

        sequenceWriter.write(row);
        ++rowCount;
    }

    public int getRowCount() {
        return rowCount;
    }
}
