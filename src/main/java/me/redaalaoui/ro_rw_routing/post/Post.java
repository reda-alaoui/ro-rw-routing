package me.redaalaoui.ro_rw_routing.post;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Post {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "post", orphanRemoval = true)
  private List<PostComment> comments = new ArrayList<>();

  public Post() {}

  public Post(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }

  public Post setId(long id) {
    this.id = id;
    return this;
  }

  public List<PostComment> getComments() {
    return comments;
  }

  public Post addComment(PostComment comment) {
    comments.add(comment);
    comment.setPost(this);

    return this;
  }
}
