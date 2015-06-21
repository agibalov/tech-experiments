package me.loki2302.springrepositoriesrelations;

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
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;

@ContextConfiguration(classes = SpringMongoRepositoriesRelationsTest.Config.class)
@IntegrationTest
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
            postId = post.getId();
        }

        Post post = postRepository.findOne(postId);
        assertEquals("loki2302", post.getUser().getName());
    }

    private static User makeUser(String name) {
        User user = new User();
        user.setName(name);
        return user;
    }

    private static Post makePost(String text, User user) {
        Post post = new Post();
        post.setText(text);
        post.setUser(user);
        return post;
    }



    @Configuration
    @EnableMongoRepositories(basePackageClasses = UserRepository.class)
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
