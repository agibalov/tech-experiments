package me.loki2302;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import java.util.List;

public class App {
    // TODO: make it a test
    // TODO: try http://docs.datastax.com/en/developer/java-driver/2.1/java-driver/reference/crudOperations.html
    public static void main(String[] args) {
        Cluster cluster = Cluster.builder()
                .addContactPoint("127.0.0.1")
                .build();
        try {
            if(true) {
                Session session = cluster.connect();
                try {
                    session.execute("drop keyspace if exists dummy");
                    session.execute("create keyspace dummy " +
                            "with replication = {'class':'SimpleStrategy', 'replication_factor':1}");
                    session.execute("use dummy");
                    session.execute("create table notes(note_id int primary key, content text)");
                    session.execute("insert into notes(note_id, content) values(111, 'hello there')");
                    session.execute("insert into notes(note_id, content) values(222, 'second note')");
                } finally {
                    session.close();
                }
            }

            Session session = cluster.connect("dummy");
            try {
                ResultSet resultSet = session.execute("select * from notes");
                List<Row> rows = resultSet.all();
                System.out.printf("rows=%d\n", rows.size());
                for (Row row : rows) {
                    System.out.printf("%d, '%s'\n", row.getInt("note_id"), row.getString("content"));
                }
            } finally {
                session.close();
            }
        } finally {
            cluster.close();
        }
    }
}
