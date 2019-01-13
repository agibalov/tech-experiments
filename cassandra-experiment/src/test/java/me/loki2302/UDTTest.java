package me.loki2302;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.UDTValue;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class UDTTest {
    @Rule
    public CassandraSessionRule cassandraSessionRule = new CassandraSessionRule();

    @Test
    public void canUseUDT() {
        Session session = cassandraSessionRule.getSession();
        session.execute("create type user(id int, name text)");
        session.execute("create table notes(id int primary key, content text, author frozen<user>)");
        session.execute("insert into notes(id, content, author) values(1, 'hello', {id: 2302, name: 'loki2302'})");

        ResultSet resultSet = session.execute("select * from notes");
        List<Row> rows = resultSet.all();
        Row firstRow = rows.get(0);
        UDTValue author = firstRow.getUDTValue("author");
        assertEquals("user", author.getType().getTypeName());
        assertEquals(2302, author.getInt("id"));
        assertEquals("loki2302", author.getString("name"));
    }
}
