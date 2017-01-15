package me.loki2302.dummy;

import me.loki2302.ElasticsearchIntegrationTestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@SpringBootTest(classes = SpringDataElasticSearchTest.Config.class)
@RunWith(SpringRunner.class)
public class SpringDataElasticSearchTest {
    @Autowired
    private ElasticsearchIntegrationTestUtils elasticsearchIntegrationTestUtils;

    @Autowired
    private BookRepository bookRepository;

    @Before
    public void init() {
        elasticsearchIntegrationTestUtils.reset();
    }

    @Test
    public void canCreateABookAndGetAnId() {
        Book book = makeBook("hello");
        book = bookRepository.save(book);
        assertNotNull(book.id);
    }

    @Test
    public void canGetBookById() {
        String id;
        {
            Book book = makeBook("hello");
            book = bookRepository.save(book);
            id = book.id;
        }

        Book book = bookRepository.findOne(id);
        assertEquals(id, book.id);
        assertEquals("hello", book.title);
    }

    @Test
    public void canUpdateBook() {
        String id;
        {
            Book book = makeBook("hello");
            book = bookRepository.save(book);
            id = book.id;
        }

        Book book = bookRepository.findOne(id);
        book.title = "omg";
        bookRepository.save(book);

        book = bookRepository.findOne(id);
        assertEquals("omg", book.title);
    }

    @Test
    public void canDeleteBook() {
        String id;
        {
            Book book = makeBook("hello");
            book = bookRepository.save(book);
            id = book.id;
        }

        bookRepository.delete(id);

        assertNull(bookRepository.findOne(id));
    }

    private static Book makeBook(String title) {
        Book book = new Book();
        book.title = title;
        return book;
    }

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan
    public static class Config {
        @Bean
        public ElasticsearchIntegrationTestUtils elasticsearchIntegrationTestUtils() {
            return new ElasticsearchIntegrationTestUtils();
        }
    }
}
