package me.loki2302.springrepositories;

import com.mongodb.Mongo;
import me.loki2302.AbstractMongoTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.UnknownHostException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@ContextConfiguration(classes = SpringMongoRepositoriesTest.Config.class)
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class SpringMongoRepositoriesTest extends AbstractMongoTest {
    @Autowired
    private PersonRepository personRepository;

    @Test
    public void canInsertAndGetPerson() {
        String id = personRepository.save(makePerson("loki2302")).getId();
        assertNotNull(id);

        Person p = personRepository.findOne(id);
        assertEquals("loki2302", p.getName());
    }

    @Test
    public void cantGetPersonIfItDoesNotExist() {
        Person p = personRepository.findOne("1");
        assertNull(p);
    }

    @Test
    public void canInsertAndCountAFewPeople() {
        personRepository.save(Arrays.asList(
                makePerson("loki2302"),
                makePerson("john"),
                makePerson("andrey")));
        assertEquals(3, personRepository.count());
    }

    private static Person makePerson(String name) {
        Person person = new Person();
        person.setName(name);
        return person;
    }

    @Configuration
    @EnableMongoRepositories(basePackageClasses = PersonRepository.class)
    public static class Config {
        @Bean
        public Mongo mongo() throws UnknownHostException {
            return new Mongo(MONGO_HOST, MONGO_PORT);
        }

        @Bean
        public MongoTemplate mongoTemplate() throws UnknownHostException {
            return new MongoTemplate(new SimpleMongoDbFactory(mongo(), MONGO_DB));
        }
    }
}
