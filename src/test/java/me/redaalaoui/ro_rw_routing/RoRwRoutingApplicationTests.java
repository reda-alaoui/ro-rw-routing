package me.redaalaoui.ro_rw_routing;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import me.redaalaoui.ro_rw_routing.data_source.DataSourceTypes;
import me.redaalaoui.ro_rw_routing.data_source.RoutingDataSource;
import me.redaalaoui.ro_rw_routing.post.Post;
import me.redaalaoui.ro_rw_routing.post.PostComment;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class RoRwRoutingApplicationTests {

  @Autowired private PlatformTransactionManager transactionManager;

  @PersistenceContext private EntityManager entityManager;

  private TransactionTemplate transactionTemplate;

  @BeforeEach
  void beforeEach() {
    transactionTemplate = new TransactionTemplate(transactionManager);
  }

  @AfterEach
  void afterEach() {
    RoutingDataSource.CURRENT_DATASOURCE.remove();
  }

  @Test
  void test() {

    Long postId =
        transactionTemplate.execute(
            status -> {
              Post post = new Post();
              post.addComment(new PostComment("Nice"));
              entityManager.persist(post);
              return post.getId();
            });

    transactionTemplate.executeWithoutResult(
        status -> {
          Post post = entityManager.find(Post.class, postId);
          assertThat(post).isNotNull();
          assertThat(post.getComments())
              .hasSize(1)
              .extracting(PostComment::getReview)
              .contains("Nice");
        });

    Integer cacheEntriesCount =
        CacheManager.ALL_CACHE_MANAGERS.stream()
            .map(cacheManager -> cacheManager.getCache(Post.class.getCanonicalName()))
            .map(Cache::getSize)
            .reduce(Integer::sum)
            .orElse(0);
    assertThat(cacheEntriesCount).isEqualTo(1);

    RoutingDataSource.CURRENT_DATASOURCE.set(DataSourceTypes.REPLICA);
    transactionTemplate.executeWithoutResult(
        status -> {
          Post post = entityManager.find(Post.class, postId);
          assertThat(post).isNotNull();
          assertThat(post.getComments())
              .hasSize(1)
              .extracting(PostComment::getReview)
              .contains("Nice");
        });
  }
}
