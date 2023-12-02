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

/** This is an auto generated class representing the Comment type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Comments", authRules = {
  @AuthRule(allow = AuthStrategy.PUBLIC, provider = "apiKey", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE, ModelOperation.DELETE })
})
public final class Comment implements Model {
  public static final QueryField ID = field("Comment", "id");
  public static final QueryField AUTHOR = field("Comment", "author");
  public static final QueryField POST_ID = field("Comment", "postID");
  public static final QueryField CONTENT = field("Comment", "content");
  public static final QueryField CREATED_AT = field("Comment", "createdAt");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String", isRequired = true) String author;
  private final @ModelField(targetType="String", isRequired = true) String postID;
  private final @ModelField(targetType="String", isRequired = true) String content;
  private final @ModelField(targetType="String") String createdAt;
  public String getId() {
      return id;
  }
  
  public String getAuthor() {
      return author;
  }
  
  public String getPostId() {
      return postID;
  }
  
  public String getContent() {
      return content;
  }
  
  public String getCreatedAt() {
      return createdAt;
  }
  
  private Comment(String id, String author, String postID, String content, String createdAt) {
    this.id = id;
    this.author = author;
    this.postID = postID;
    this.content = content;
    this.createdAt = createdAt;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Comment comment = (Comment) obj;
      return ObjectsCompat.equals(getId(), comment.getId()) &&
              ObjectsCompat.equals(getAuthor(), comment.getAuthor()) &&
              ObjectsCompat.equals(getPostId(), comment.getPostId()) &&
              ObjectsCompat.equals(getContent(), comment.getContent()) &&
              ObjectsCompat.equals(getCreatedAt(), comment.getCreatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getAuthor())
      .append(getPostId())
      .append(getContent())
      .append(getCreatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Comment {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("author=" + String.valueOf(getAuthor()) + ", ")
      .append("postID=" + String.valueOf(getPostId()) + ", ")
      .append("content=" + String.valueOf(getContent()) + ", ")
      .append("createdAt=" + String.valueOf(getCreatedAt()))
      .append("}")
      .toString();
  }
  
  public static AuthorStep builder() {
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
  public static Comment justId(String id) {
    return new Comment(
      id,
      null,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      author,
      postID,
      content,
      createdAt);
  }
  public interface AuthorStep {
    PostIdStep author(String author);
  }
  

  public interface PostIdStep {
    ContentStep postId(String postId);
  }
  

  public interface ContentStep {
    BuildStep content(String content);
  }
  

  public interface BuildStep {
    Comment build();
    BuildStep id(String id);
    BuildStep createdAt(String createdAt);
  }
  

  public static class Builder implements AuthorStep, PostIdStep, ContentStep, BuildStep {
    private String id;
    private String author;
    private String postID;
    private String content;
    private String createdAt;
    public Builder() {
      
    }
    
    private Builder(String id, String author, String postID, String content, String createdAt) {
      this.id = id;
      this.author = author;
      this.postID = postID;
      this.content = content;
      this.createdAt = createdAt;
    }
    
    @Override
     public Comment build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Comment(
          id,
          author,
          postID,
          content,
          createdAt);
    }
    
    @Override
     public PostIdStep author(String author) {
        Objects.requireNonNull(author);
        this.author = author;
        return this;
    }
    
    @Override
     public ContentStep postId(String postId) {
        Objects.requireNonNull(postId);
        this.postID = postId;
        return this;
    }
    
    @Override
     public BuildStep content(String content) {
        Objects.requireNonNull(content);
        this.content = content;
        return this;
    }
    
    @Override
     public BuildStep createdAt(String createdAt) {
        this.createdAt = createdAt;
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
    private CopyOfBuilder(String id, String author, String postId, String content, String createdAt) {
      super(id, author, postID, content, createdAt);
      Objects.requireNonNull(author);
      Objects.requireNonNull(postID);
      Objects.requireNonNull(content);
    }
    
    @Override
     public CopyOfBuilder author(String author) {
      return (CopyOfBuilder) super.author(author);
    }
    
    @Override
     public CopyOfBuilder postId(String postId) {
      return (CopyOfBuilder) super.postId(postId);
    }
    
    @Override
     public CopyOfBuilder content(String content) {
      return (CopyOfBuilder) super.content(content);
    }
    
    @Override
     public CopyOfBuilder createdAt(String createdAt) {
      return (CopyOfBuilder) super.createdAt(createdAt);
    }
  }
  
}
