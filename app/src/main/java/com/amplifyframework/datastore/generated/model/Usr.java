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

/** This is an auto generated class representing the Usr type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Usrs", authRules = {
  @AuthRule(allow = AuthStrategy.PUBLIC, provider = "apiKey", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE, ModelOperation.DELETE })
})
public final class Usr implements Model {
  public static final QueryField ID = field("Usr", "id");
  public static final QueryField USERNAME = field("Usr", "username");
  public static final QueryField FULLNAME = field("Usr", "fullname");
  public static final QueryField EMAIL = field("Usr", "email");
  public static final QueryField PFP = field("Usr", "pfp");
  public static final QueryField BIO = field("Usr", "bio");
  public static final QueryField FOLLOWERS = field("Usr", "followers");
  public static final QueryField FOLLOWING = field("Usr", "following");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String", isRequired = true) String username;
  private final @ModelField(targetType="String", isRequired = true) String fullname;
  private final @ModelField(targetType="String", isRequired = true) String email;
  private final @ModelField(targetType="String") String pfp;
  private final @ModelField(targetType="String") String bio;
  private final @ModelField(targetType="String") List<String> followers;
  private final @ModelField(targetType="String") List<String> following;
  public String getId() {
      return id;
  }
  
  public String getUsername() {
      return username;
  }
  
  public String getFullname() {
      return fullname;
  }
  
  public String getEmail() {
      return email;
  }
  
  public String getPfp() {
      return pfp;
  }
  
  public String getBio() {
      return bio;
  }
  
  public List<String> getFollowers() {
      return followers;
  }
  
  public List<String> getFollowing() {
      return following;
  }
  
  private Usr(String id, String username, String fullname, String email, String pfp, String bio, List<String> followers, List<String> following) {
    this.id = id;
    this.username = username;
    this.fullname = fullname;
    this.email = email;
    this.pfp = pfp;
    this.bio = bio;
    this.followers = followers;
    this.following = following;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Usr usr = (Usr) obj;
      return ObjectsCompat.equals(getId(), usr.getId()) &&
              ObjectsCompat.equals(getUsername(), usr.getUsername()) &&
              ObjectsCompat.equals(getFullname(), usr.getFullname()) &&
              ObjectsCompat.equals(getEmail(), usr.getEmail()) &&
              ObjectsCompat.equals(getPfp(), usr.getPfp()) &&
              ObjectsCompat.equals(getBio(), usr.getBio()) &&
              ObjectsCompat.equals(getFollowers(), usr.getFollowers()) &&
              ObjectsCompat.equals(getFollowing(), usr.getFollowing());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getUsername())
      .append(getFullname())
      .append(getEmail())
      .append(getPfp())
      .append(getBio())
      .append(getFollowers())
      .append(getFollowing())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Usr {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("username=" + String.valueOf(getUsername()) + ", ")
      .append("fullname=" + String.valueOf(getFullname()) + ", ")
      .append("email=" + String.valueOf(getEmail()) + ", ")
      .append("pfp=" + String.valueOf(getPfp()) + ", ")
      .append("bio=" + String.valueOf(getBio()) + ", ")
      .append("followers=" + String.valueOf(getFollowers()) + ", ")
      .append("following=" + String.valueOf(getFollowing()))
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
  public static Usr justId(String id) {
    return new Usr(
      id,
      null,
      null,
      null,
      null,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      username,
      fullname,
      email,
      pfp,
      bio,
      followers,
      following);
  }
  public interface UsernameStep {
    FullnameStep username(String username);
  }
  

  public interface FullnameStep {
    EmailStep fullname(String fullname);
  }
  

  public interface EmailStep {
    BuildStep email(String email);
  }
  

  public interface BuildStep {
    Usr build();
    BuildStep id(String id);
    BuildStep pfp(String pfp);
    BuildStep bio(String bio);
    BuildStep followers(List<String> followers);
    BuildStep following(List<String> following);
  }
  

  public static class Builder implements UsernameStep, FullnameStep, EmailStep, BuildStep {
    private String id;
    private String username;
    private String fullname;
    private String email;
    private String pfp;
    private String bio;
    private List<String> followers;
    private List<String> following;
    @Override
     public Usr build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Usr(
          id,
          username,
          fullname,
          email,
          pfp,
          bio,
          followers,
          following);
    }
    
    @Override
     public FullnameStep username(String username) {
        Objects.requireNonNull(username);
        this.username = username;
        return this;
    }
    
    @Override
     public EmailStep fullname(String fullname) {
        Objects.requireNonNull(fullname);
        this.fullname = fullname;
        return this;
    }
    
    @Override
     public BuildStep email(String email) {
        Objects.requireNonNull(email);
        this.email = email;
        return this;
    }
    
    @Override
     public BuildStep pfp(String pfp) {
        this.pfp = pfp;
        return this;
    }
    
    @Override
     public BuildStep bio(String bio) {
        this.bio = bio;
        return this;
    }
    
    @Override
     public BuildStep followers(List<String> followers) {
        this.followers = followers;
        return this;
    }
    
    @Override
     public BuildStep following(List<String> following) {
        this.following = following;
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
    private CopyOfBuilder(String id, String username, String fullname, String email, String pfp, String bio, List<String> followers, List<String> following) {
      super.id(id);
      super.username(username)
        .fullname(fullname)
        .email(email)
        .pfp(pfp)
        .bio(bio)
        .followers(followers)
        .following(following);
    }
    
    @Override
     public CopyOfBuilder username(String username) {
      return (CopyOfBuilder) super.username(username);
    }
    
    @Override
     public CopyOfBuilder fullname(String fullname) {
      return (CopyOfBuilder) super.fullname(fullname);
    }
    
    @Override
     public CopyOfBuilder email(String email) {
      return (CopyOfBuilder) super.email(email);
    }
    
    @Override
     public CopyOfBuilder pfp(String pfp) {
      return (CopyOfBuilder) super.pfp(pfp);
    }
    
    @Override
     public CopyOfBuilder bio(String bio) {
      return (CopyOfBuilder) super.bio(bio);
    }
    
    @Override
     public CopyOfBuilder followers(List<String> followers) {
      return (CopyOfBuilder) super.followers(followers);
    }
    
    @Override
     public CopyOfBuilder following(List<String> following) {
      return (CopyOfBuilder) super.following(following);
    }
  }
  
}
