package io.agibalov;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.testcontainers.containers.MySQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class IsolationLevelsTest {
    @ClassRule
    public final static MySQLContainer MYSQL_CONTAINER = new MySQLContainer();

    @Before
    public void init() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(new DriverManagerDataSource(
                MYSQL_CONTAINER.getJdbcUrl(),
                MYSQL_CONTAINER.getUsername(),
                MYSQL_CONTAINER.getPassword()));
        jdbcTemplate.update("drop table if exists notes");
        jdbcTemplate.update("create table notes(id int)");
    }

    @Test
    public void repeatableReadTest() throws SQLException {
        try(Connection readerAConnection = makeConnection(Connection.TRANSACTION_REPEATABLE_READ);
            Connection readerBConnection = makeConnection(Connection.TRANSACTION_REPEATABLE_READ);
            Connection writerConnection = makeConnection(Connection.TRANSACTION_REPEATABLE_READ)) {

            JdbcTemplate readerAJdbcTemplate = makeJdbcTemplate(readerAConnection);
            JdbcTemplate readerBJdbcTemplate = makeJdbcTemplate(readerBConnection);
            JdbcTemplate writerJdbcTemplate = makeJdbcTemplate(writerConnection);

            // readerA's first read returns 0 records
            assertEquals(0, (int)readerAJdbcTemplate.queryForObject("select count(*) from notes", Integer.class));

            writerJdbcTemplate.update("insert into notes values(1)");
            writerConnection.commit();

            // readerA doesn't see the new record, it still has 0 records
            assertEquals(0, (int)readerAJdbcTemplate.queryForObject("select count(*) from notes", Integer.class));

            // readerB's first read returns 1 record
            assertEquals(1, (int)readerBJdbcTemplate.queryForObject("select count(*) from notes", Integer.class));

            writerJdbcTemplate.update("delete from notes");
            writerConnection.commit();

            // readerA still has 0 records
            assertEquals(0, (int)readerAJdbcTemplate.queryForObject("select count(*) from notes", Integer.class));

            // readerB's still has 1 record
            assertEquals(1, (int)readerBJdbcTemplate.queryForObject("select count(*) from notes", Integer.class));
        }
    }

    @Test
    public void readCommittedTest() throws SQLException {
        try(Connection readerConnection = makeConnection(Connection.TRANSACTION_READ_COMMITTED);
            Connection writerConnection = makeConnection(Connection.TRANSACTION_REPEATABLE_READ)) {

            JdbcTemplate readerJdbcTemplate = makeJdbcTemplate(readerConnection);
            JdbcTemplate writerJdbcTemplate = makeJdbcTemplate(writerConnection);

            assertEquals(0, (int)readerJdbcTemplate.queryForObject("select count(*) from notes", Integer.class));

            writerJdbcTemplate.update("insert into notes values(1)");

            assertEquals(0, (int)readerJdbcTemplate.queryForObject("select count(*) from notes", Integer.class));

            writerConnection.commit();

            assertEquals(1, (int)readerJdbcTemplate.queryForObject("select count(*) from notes", Integer.class));

            writerJdbcTemplate.update("delete from notes");

            assertEquals(1, (int)readerJdbcTemplate.queryForObject("select count(*) from notes", Integer.class));

            writerConnection.commit();

            assertEquals(0, (int)readerJdbcTemplate.queryForObject("select count(*) from notes", Integer.class));
        }
    }

    @Test
    public void readUncommittedTest() throws SQLException {
        try(Connection readerConnection = makeConnection(Connection.TRANSACTION_READ_UNCOMMITTED);
            Connection writerConnection = makeConnection(Connection.TRANSACTION_REPEATABLE_READ)) {

            JdbcTemplate readerJdbcTemplate = makeJdbcTemplate(readerConnection);
            JdbcTemplate writerJdbcTemplate = makeJdbcTemplate(writerConnection);

            assertEquals(0, (int)readerJdbcTemplate.queryForObject("select count(*) from notes", Integer.class));

            writerJdbcTemplate.update("insert into notes values(1)");

            assertEquals(1, (int)readerJdbcTemplate.queryForObject("select count(*) from notes", Integer.class));

            writerJdbcTemplate.update("delete from notes");

            assertEquals(0, (int)readerJdbcTemplate.queryForObject("select count(*) from notes", Integer.class));
        }
    }

    @Test
    public void serializableTest() throws SQLException {
        try(Connection writerConnection = makeConnection(Connection.TRANSACTION_REPEATABLE_READ)) {
            JdbcTemplate writerJdbcTemplate = makeJdbcTemplate(writerConnection);

            // give it only 1 second to acquire the write lock (default is 50 seconds)
            writerJdbcTemplate.update("set innodb_lock_wait_timeout = 1");

            try(Connection readerConnection = makeConnection(Connection.TRANSACTION_SERIALIZABLE)) {
                JdbcTemplate readerJdbcTemplate = makeJdbcTemplate(readerConnection);

                // reader acquires the lock implicitly
                readerJdbcTemplate.queryForObject("select count(*) from notes", Integer.class);

                // writer fails to acquire the lock, because reader still retains it (connection is still open)
                try {
                    writerJdbcTemplate.update("insert into notes values(1)");
                    fail();
                } catch (CannotAcquireLockException e) {
                    // expected
                }
            }

            // now that the reader has released the lock, writer can insert
            writerJdbcTemplate.update("insert into notes values(1)");
        }
    }

    private static Connection makeConnection(int isolation) throws SQLException {
        Connection connection = DriverManager.getConnection(
                MYSQL_CONTAINER.getJdbcUrl(),
                MYSQL_CONTAINER.getUsername(),
                MYSQL_CONTAINER.getPassword());

        connection.setTransactionIsolation(isolation);
        connection.setAutoCommit(false);

        return connection;
    }

    private static JdbcTemplate makeJdbcTemplate(Connection connection) {
        return new JdbcTemplate(new SingleConnectionDataSource(connection, true));
    }
}
