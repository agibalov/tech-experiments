package io.agibalov;

import com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.testcontainers.containers.MySQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Exchanger;

import static org.junit.Assert.*;

public class LocksTest {
    @ClassRule
    public final static MySQLContainer MYSQL_CONTAINER = new MySQLContainer();

    @Before
    public void init() throws SQLException {
        try(Connection connection = makeConnection(null)) {
            JdbcTemplate jdbcTemplate = makeJdbcTemplate(connection);
            jdbcTemplate.update("drop table if exists aaa");
            jdbcTemplate.update("create table aaa(id int)");
        }
    }

    @Test
    public void canGetLockWaitTimeoutExceeded() throws SQLException {
        try(Connection connection = makeConnection(null)) {
            JdbcTemplate jdbcTemplate = makeJdbcTemplate(connection);
            jdbcTemplate.execute("insert into aaa(id) values(123)");
            connection.commit();
        }

        try(Connection connectionA = makeConnection(Connection.TRANSACTION_READ_UNCOMMITTED);
            Connection connectionB = makeConnection(Connection.TRANSACTION_READ_UNCOMMITTED)) {

            JdbcTemplate jdbcTemplateA = makeJdbcTemplate(connectionA);
            JdbcTemplate jdbcTemplateB = makeJdbcTemplate(connectionB);

            jdbcTemplateA.execute("set innodb_lock_wait_timeout = 3;");
            jdbcTemplateB.execute("set innodb_lock_wait_timeout = 3;");

            jdbcTemplateA.execute("update aaa set id = 111 where id = 123");

            try {
                jdbcTemplateB.execute("delete from aaa where id = 123");
                fail();
            } catch (CannotAcquireLockException e) {
                Throwable cause = e.getCause();
                assertTrue(cause instanceof MySQLTransactionRollbackException);
                assertTrue(cause.getMessage().contains("Lock wait timeout exceeded; try restarting transaction"));
            }

            connectionA.commit();
        }

        try(Connection connection = makeConnection(null)) {
            JdbcTemplate jdbcTemplate = makeJdbcTemplate(connection);
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("select id from aaa");
            assertEquals(Arrays.asList(new HashMap<String, Object>() {{ put("id", 111); }}), rows);
        }
    }

    @Test
    public void canGetDeadlock() throws SQLException, InterruptedException {
        try(Connection connection = makeConnection(null)) {
            JdbcTemplate jdbcTemplate = makeJdbcTemplate(connection);
            jdbcTemplate.execute("insert into aaa(id) values(1)");
            connection.commit();
        }

        try(Connection connectionA = makeConnection(Connection.TRANSACTION_SERIALIZABLE);
            Connection connectionB = makeConnection(Connection.TRANSACTION_SERIALIZABLE)) {

            JdbcTemplate jdbcTemplateA = makeJdbcTemplate(connectionA);
            JdbcTemplate jdbcTemplateB = makeJdbcTemplate(connectionB);

            jdbcTemplateA.queryForList("select * from aaa where id = 1");

            Exchanger<Throwable> jdbcTemplateBExceptionExchanger = new Exchanger<>();
            Thread thread = new Thread(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    try {
                        //jdbcTemplateB.execute("delete from aaa where id = 1");
                        jdbcTemplateB.execute("update aaa set id = 2 where id = 1");
                    } catch (Throwable t) {
                        jdbcTemplateBExceptionExchanger.exchange(t);
                    }
                }
            });
            thread.start();
            Thread.sleep(1000);

            //jdbcTemplateA.execute("delete from aaa where id = 1");
            jdbcTemplateA.execute("update aaa set id = 3 where id = 1");

            Throwable jdbcTemplateBException = jdbcTemplateBExceptionExchanger.exchange(null);
            thread.join();

            assertTrue(jdbcTemplateBException instanceof ConcurrencyFailureException);
            Throwable cause = jdbcTemplateBException.getCause();
            assertTrue(cause instanceof MySQLTransactionRollbackException);
            assertTrue(cause.getMessage().contains("Deadlock found when trying to get lock; try restarting transaction"));

            connectionA.commit();
        }

        try(Connection connection = makeConnection(null)) {
            JdbcTemplate jdbcTemplate = makeJdbcTemplate(connection);
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("select id from aaa");
            assertEquals(Arrays.asList(new HashMap<String, Object>() {{ put("id", 3); }}), rows);
        }
    }

    private static Connection makeConnection(Integer isolation) throws SQLException {
        Connection connection = DriverManager.getConnection(
                MYSQL_CONTAINER.getJdbcUrl(),
                MYSQL_CONTAINER.getUsername(),
                MYSQL_CONTAINER.getPassword());

        if (isolation != null) {
            connection.setTransactionIsolation(isolation);
        }

        connection.setAutoCommit(false);

        return connection;
    }

    private static JdbcTemplate makeJdbcTemplate(Connection connection) {
        return new JdbcTemplate(new SingleConnectionDataSource(connection, true));
    }
}
