package me.loki2302;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ListTest {
    @Rule
    public CassandraSessionRule cassandraSessionRule = new CassandraSessionRule();

    // TODO: can I select by 'list contains'?
    // TODO: append an item
    // TODO: remove an item
    // TODO: get item by index
    // TODO: can I only fetch a part of the list
    @Test
    public void canUseAList() {
        Session session = cassandraSessionRule.getSession();
        session.execute("create table notes(id int primary key, content text, tags list<text>)");
        session.execute("insert into notes(id, content, tags) values(1, 'hello', ['one', 'two'])");

        ResultSet resultSet = session.execute("select * from notes");
        List<Row> rows = resultSet.all();
        List<String> note1Tags = rows.get(0).getList("tags", String.class);
        assertEquals(2, note1Tags.size());
        assertEquals("one", note1Tags.get(0));
        assertEquals("two", note1Tags.get(1));
    }
}
