package me.loki2302.springrepositories;

import me.loki2302.EmbeddedElasticSearch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@ContextConfiguration(classes = SpringDataElasticSearchTest.Config.class)
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class SpringDataElasticSearchTest {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Test
    public void canCreateABookAndGetAnId() {
        Book book = makeBook("hello");
        book = bookRepository.save(book);
        assertNotNull(book.getId());
    }

    @Test
    public void canGetBookById() {
        String id;
        {
            Book book = makeBook("hello");
            book = bookRepository.save(book);
            id = book.getId();
        }

        Book book = bookRepository.findOne(id);
        assertEquals(id, book.getId());
        assertEquals("hello", book.getTitle());
    }

    @Test
    public void canUpdateBook() {
        String id;
        {
            Book book = makeBook("hello");
            book = bookRepository.save(book);
            id = book.getId();
        }

        Book book = bookRepository.findOne(id);
        book.setTitle("omg");
        bookRepository.save(book);

        book = bookRepository.findOne(id);
        assertEquals("omg", book.getTitle());
    }

    @Test
    public void canDeleteBook() {
        String id;
        {
            Book book = makeBook("hello");
            book = bookRepository.save(book);
            id = book.getId();
        }

        bookRepository.delete(id);

        assertNull(bookRepository.findOne(id));
    }

    private static Book makeBook(String title) {
        Book book = new Book();
        book.setTitle(title);
        return book;
    }

    @Configuration
    @EnableElasticsearchRepositories(basePackageClasses = SpringDataElasticSearchTest.class)
    public static class Config {
        @Bean
        public ElasticsearchTemplate elasticsearchTemplate() {
            return new ElasticsearchTemplate(embeddedElasticSearch().client());
        }

        @Bean(initMethod = "start", destroyMethod = "stop")
        public EmbeddedElasticSearch embeddedElasticSearch() {
            return new EmbeddedElasticSearch();
        }
    }
}
