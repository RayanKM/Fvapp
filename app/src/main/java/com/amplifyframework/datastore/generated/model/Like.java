package com.amplifyframework.datastore.generated.model;


import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.AuthStrategy;
import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.ModelOperation;
import com.amplifyframework.core.model.annotations.AuthRule;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the Like type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Likes", authRules = {
  @AuthRule(allow = AuthStrategy.PUBLIC, provider = "apiKey", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE, ModelOperation.DELETE })
})
public final class Like implements Model {
  public static final QueryField ID = field("Like", "id");
  public static final QueryField USERNAME = field("Like", "username");
  public static final QueryField POST_ID = field("Like", "postID");
  public static final QueryField TO = field("Like", "to");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String", isRequired = true) String username;
  private final @ModelField(targetType="String", isRequired = true) String postID;
  private final @ModelField(targetType="String", isRequired = true) String to;
  public String getId() {
      return id;
  }
  
  public String getUsername() {
      return username;
  }
  
  public String getPostId() {
      return postID;
  }
  
  public String getTo() {
      return to;
  }
  
  private Like(String id, String username, String postID, String to) {
    this.id = id;
    this.username = username;
    this.postID = postID;
    this.to = to;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Like like = (Like) obj;
      return ObjectsCompat.equals(getId(), like.getId()) &&
              ObjectsCompat.equals(getUsername(), like.getUsername()) &&
              ObjectsCompat.equals(getPostId(), like.getPostId()) &&
              ObjectsCompat.equals(getTo(), like.getTo());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getUsername())
      .append(getPostId())
      .append(getTo())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Like {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("username=" + String.valueOf(getUsername()) + ", ")
      .append("postID=" + String.valueOf(getPostId()) + ", ")
      .append("to=" + String.valueOf(getTo()))
      .append("}")
      .toString();
  }
  
  public static UsernameStep builder() {
      return new Builder();
  }
  
  /**
   * WARNING: This method should not be used to build an instance of this object for a CREATE mutation.
   * This is a convenience method to return an instance of the object with only its ID populated
   * to be used in the context of a parameter in a delete mutation or referencing a foreign key
   * in a relationship.
   * @param id the id of the existing item this instance will represent
   * @return an instance of this model with only ID populated
   */
  public static Like justId(String id) {
    return new Like(
      id,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      username,
      postID,
      to);
  }
  public interface UsernameStep {
    PostIdStep username(String username);
  }
  

  public interface PostIdStep {
    ToStep postId(String postId);
  }
  

  public interface ToStep {
    BuildStep to(String to);
  }
  

  public interface BuildStep {
    Like build();
    BuildStep id(String id);
  }
  

  public static class Builder implements UsernameStep, PostIdStep, ToStep, BuildStep {
    private String id;
    private String username;
    private String postID;
    private String to;
    public Builder() {
      
    }
    
    private Builder(String id, String username, String postID, String to) {
      this.id = id;
      this.username = username;
      this.postID = postID;
      this.to = to;
    }
    
    @Override
     public Like build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Like(
          id,
          username,
          postID,
          to);
    }
    
    @Override
     public PostIdStep username(String username) {
        Objects.requireNonNull(username);
        this.username = username;
        return this;
    }
    
    @Override
     public ToStep postId(String postId) {
        Objects.requireNonNull(postId);
        this.postID = postId;
        return this;
    }
    
    @Override
     public BuildStep to(String to) {
        Objects.requireNonNull(to);
        this.to = to;
        return this;
    }
    
    /**
     * @param id id
     * @return Current Builder instance, for fluent method chaining
     */
    public BuildStep id(String id) {
        this.id = id;
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String id, String username, String postId, String to) {
      super(id, username, postID, to);
      Objects.requireNonNull(username);
      Objects.requireNonNull(postID);
      Objects.requireNonNull(to);
    }
    
    @Override
     public CopyOfBuilder username(String username) {
      return (CopyOfBuilder) super.username(username);
    }
    
    @Override
     public CopyOfBuilder postId(String postId) {
      return (CopyOfBuilder) super.postId(postId);
    }
    
    @Override
     public CopyOfBuilder to(String to) {
      return (CopyOfBuilder) super.to(to);
    }
  }
  
}
