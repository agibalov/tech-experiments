package me.loki2302.springrepositoriesrelations;

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

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class SpringMongoRepositoriesRelationsTest extends AbstractMongoTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Test
    public void dummy() {
        String postId;
        {
            User user = userRepository.save(makeUser("loki2302"));
            Post post = postRepository.save(makePost("hello", user));
            postId = post.id;
        }

        Post post = postRepository.findOne(postId);
        assertEquals("loki2302", post.user.name);
    }

    private static User makeUser(String name) {
        User user = new User();
        user.name = name;
        return user;
    }

    private static Post makePost(String text, User user) {
        Post post = new Post();
        post.text = text;
        post.user = user;
        return post;
    }

    @Configuration
    @EnableMongoRepositories(basePackageClasses = UserRepository.class)
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
