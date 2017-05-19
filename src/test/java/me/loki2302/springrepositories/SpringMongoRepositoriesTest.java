package me.loki2302.springrepositories;

import com.mongodb.MongoClient;
import me.loki2302.AbstractMongoTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.UnknownHostException;
import java.util.Arrays;

import static org.junit.Assert.*;

@SpringBootTest
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

    @Test
    public void canFindPersonByName() {
        personRepository.save(Arrays.asList(
                makePerson("loki2302"),
                makePerson("john"),
                makePerson("andrey")));

        assertEquals("loki2302", personRepository.findByName("loki2302").get(0).getName());
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
        public MongoClient mongoClient() throws UnknownHostException {
            return new MongoClient(MONGO_HOST, MONGO_PORT);
        }

        @Bean
        public MongoTemplate mongoTemplate() throws UnknownHostException {
            return new MongoTemplate(new SimpleMongoDbFactory(mongoClient(), MONGO_DB));
        }
    }
}
