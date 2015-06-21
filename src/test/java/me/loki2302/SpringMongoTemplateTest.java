package me.loki2302;

import com.mongodb.Mongo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.UnknownHostException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@ContextConfiguration(classes = SpringMongoTemplateTest.Config.class)
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class SpringMongoTemplateTest extends AbstractMongoTest {
    @Autowired
    private MongoOperations mongoOperations;

    @Test
    public void canInsertAndGetPerson() {
        String id;
        {
            Person p = makePerson("loki2302");
            mongoOperations.insert(p);
            id = p.getId();
        }

        assertNotNull(id);

        Person p = mongoOperations.findById(id, Person.class);
        assertEquals("loki2302", p.getName());
    }

    @Test
    public void cantGetPersonIfItDoesNotExist() {
        Person p = mongoOperations.findById("1", Person.class);
        assertNull(p);
    }

    @Test
    public void canInsertAndCountAFewPeople() {
        mongoOperations.insertAll(Arrays.asList(
                makePerson("loki2302"),
                makePerson("john"),
                makePerson("andrey")));
        assertEquals(3, mongoOperations.count(new Query(), Person.class));
    }

    private static Person makePerson(String name) {
        Person person = new Person();
        person.setName(name);
        return person;
    }

    @Configuration
    public static class Config {
        @Bean
        public Mongo mongo() throws UnknownHostException {
            return new Mongo(MONGO_HOST, MONGO_PORT);
        }

        @Bean
        public MongoOperations mongoOperations() throws UnknownHostException {
            return new MongoTemplate(new SimpleMongoDbFactory(mongo(), MONGO_DB));
        }
    }

    public static class Person {
        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return String.format("Person(%s,%s)", id, name);
        }
    }
}
