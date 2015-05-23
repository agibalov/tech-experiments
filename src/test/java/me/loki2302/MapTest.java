package me.loki2302;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MapTest {
    @Rule
    public CassandraSessionRule cassandraSessionRule = new CassandraSessionRule();

    @Test
    public void canUseAMap() {
        Session session = cassandraSessionRule.getSession();
        session.execute("create table users(id int primary key, properties map<text, text>)");
        session.execute("insert into users(id, properties) values(1, {'name':'loki2302', 'url':'http://loki2302.me'})");

        ResultSet resultSet = session.execute("select * from users");
        List<Row> rows = resultSet.all();
        Map<String, String> propertyMap = rows.get(0).getMap("properties", String.class, String.class);
        assertEquals(2, propertyMap.size());
        assertEquals("loki2302", propertyMap.get("name"));
        assertEquals("http://loki2302.me", propertyMap.get("url"));
    }
}
