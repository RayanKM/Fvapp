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

/** This is an auto generated class representing the Post type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Posts", authRules = {
  @AuthRule(allow = AuthStrategy.PUBLIC, provider = "apiKey", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE, ModelOperation.DELETE })
})
public final class Post implements Model {
  public static final QueryField ID = field("Post", "id");
  public static final QueryField CONTENT = field("Post", "content");
  public static final QueryField AUTHOR = field("Post", "author");
  public static final QueryField MEDIA = field("Post", "media");
  public static final QueryField TYP = field("Post", "typ");
  public static final QueryField CREATED_AT = field("Post", "createdAt");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String", isRequired = true) String content;
  private final @ModelField(targetType="String", isRequired = true) String author;
  private final @ModelField(targetType="String") String media;
  private final @ModelField(targetType="String") String typ;
  private final @ModelField(targetType="String") String createdAt;
  public String getId() {
      return id;
  }
  
  public String getContent() {
      return content;
  }
  
  public String getAuthor() {
      return author;
  }
  
  public String getMedia() {
      return media;
  }
  
  public String getTyp() {
      return typ;
  }
  
  public String getCreatedAt() {
      return createdAt;
  }
  
  private Post(String id, String content, String author, String media, String typ, String createdAt) {
    this.id = id;
    this.content = content;
    this.author = author;
    this.media = media;
    this.typ = typ;
    this.createdAt = createdAt;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Post post = (Post) obj;
      return ObjectsCompat.equals(getId(), post.getId()) &&
              ObjectsCompat.equals(getContent(), post.getContent()) &&
              ObjectsCompat.equals(getAuthor(), post.getAuthor()) &&
              ObjectsCompat.equals(getMedia(), post.getMedia()) &&
              ObjectsCompat.equals(getTyp(), post.getTyp()) &&
              ObjectsCompat.equals(getCreatedAt(), post.getCreatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getContent())
      .append(getAuthor())
      .append(getMedia())
      .append(getTyp())
      .append(getCreatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Post {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("content=" + String.valueOf(getContent()) + ", ")
      .append("author=" + String.valueOf(getAuthor()) + ", ")
      .append("media=" + String.valueOf(getMedia()) + ", ")
      .append("typ=" + String.valueOf(getTyp()) + ", ")
      .append("createdAt=" + String.valueOf(getCreatedAt()))
      .append("}")
      .toString();
  }
  
  public static ContentStep builder() {
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
  public static Post justId(String id) {
    return new Post(
      id,
      null,
      null,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      content,
      author,
      media,
      typ,
      createdAt);
  }
  public interface ContentStep {
    AuthorStep content(String content);
  }
  

  public interface AuthorStep {
    BuildStep author(String author);
  }
  

  public interface BuildStep {
    Post build();
    BuildStep id(String id);
    BuildStep media(String media);
    BuildStep typ(String typ);
    BuildStep createdAt(String createdAt);
  }
  

  public static class Builder implements ContentStep, AuthorStep, BuildStep {
    private String id;
    private String content;
    private String author;
    private String media;
    private String typ;
    private String createdAt;
    public Builder() {
      
    }
    
    private Builder(String id, String content, String author, String media, String typ, String createdAt) {
      this.id = id;
      this.content = content;
      this.author = author;
      this.media = media;
      this.typ = typ;
      this.createdAt = createdAt;
    }
    
    @Override
     public Post build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Post(
          id,
          content,
          author,
          media,
          typ,
          createdAt);
    }
    
    @Override
     public AuthorStep content(String content) {
        Objects.requireNonNull(content);
        this.content = content;
        return this;
    }
    
    @Override
     public BuildStep author(String author) {
        Objects.requireNonNull(author);
        this.author = author;
        return this;
    }
    
    @Override
     public BuildStep media(String media) {
        this.media = media;
        return this;
    }
    
    @Override
     public BuildStep typ(String typ) {
        this.typ = typ;
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
    private CopyOfBuilder(String id, String content, String author, String media, String typ, String createdAt) {
      super(id, content, author, media, typ, createdAt);
      Objects.requireNonNull(content);
      Objects.requireNonNull(author);
    }
    
    @Override
     public CopyOfBuilder content(String content) {
      return (CopyOfBuilder) super.content(content);
    }
    
    @Override
     public CopyOfBuilder author(String author) {
      return (CopyOfBuilder) super.author(author);
    }
    
    @Override
     public CopyOfBuilder media(String media) {
      return (CopyOfBuilder) super.media(media);
    }
    
    @Override
     public CopyOfBuilder typ(String typ) {
      return (CopyOfBuilder) super.typ(typ);
    }
    
    @Override
     public CopyOfBuilder createdAt(String createdAt) {
      return (CopyOfBuilder) super.createdAt(createdAt);
    }
  }
  
}
