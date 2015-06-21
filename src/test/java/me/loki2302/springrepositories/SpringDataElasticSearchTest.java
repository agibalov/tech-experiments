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

import static org.junit.Assert.assertNotNull;

@ContextConfiguration(classes = SpringDataElasticSearchTest.Config.class)
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class SpringDataElasticSearchTest {
    @Autowired
    private BookRepository bookRepository;

    @Test
    public void dummy() {
        Book book = new Book();
        book.setTitle("hello");
        book = bookRepository.save(book);
        assertNotNull(book.getId());
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
