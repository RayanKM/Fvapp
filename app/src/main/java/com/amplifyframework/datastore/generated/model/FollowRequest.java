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

/** This is an auto generated class representing the FollowRequest type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "FollowRequests", authRules = {
  @AuthRule(allow = AuthStrategy.PUBLIC, provider = "apiKey", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE, ModelOperation.DELETE })
})
public final class FollowRequest implements Model {
  public static final QueryField ID = field("FollowRequest", "id");
  public static final QueryField FROM_USER = field("FollowRequest", "fromUser");
  public static final QueryField TO_USER = field("FollowRequest", "toUser");
  public static final QueryField STATUS = field("FollowRequest", "status");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String", isRequired = true) String fromUser;
  private final @ModelField(targetType="String", isRequired = true) String toUser;
  private final @ModelField(targetType="FollowRequestStatus", isRequired = true) FollowRequestStatus status;
  public String getId() {
      return id;
  }
  
  public String getFromUser() {
      return fromUser;
  }
  
  public String getToUser() {
      return toUser;
  }
  
  public FollowRequestStatus getStatus() {
      return status;
  }
  
  private FollowRequest(String id, String fromUser, String toUser, FollowRequestStatus status) {
    this.id = id;
    this.fromUser = fromUser;
    this.toUser = toUser;
    this.status = status;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      FollowRequest followRequest = (FollowRequest) obj;
      return ObjectsCompat.equals(getId(), followRequest.getId()) &&
              ObjectsCompat.equals(getFromUser(), followRequest.getFromUser()) &&
              ObjectsCompat.equals(getToUser(), followRequest.getToUser()) &&
              ObjectsCompat.equals(getStatus(), followRequest.getStatus());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getFromUser())
      .append(getToUser())
      .append(getStatus())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("FollowRequest {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("fromUser=" + String.valueOf(getFromUser()) + ", ")
      .append("toUser=" + String.valueOf(getToUser()) + ", ")
      .append("status=" + String.valueOf(getStatus()))
      .append("}")
      .toString();
  }
  
  public static FromUserStep builder() {
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
  public static FollowRequest justId(String id) {
    return new FollowRequest(
      id,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      fromUser,
      toUser,
      status);
  }
  public interface FromUserStep {
    ToUserStep fromUser(String fromUser);
  }
  

  public interface ToUserStep {
    StatusStep toUser(String toUser);
  }
  

  public interface StatusStep {
    BuildStep status(FollowRequestStatus status);
  }
  

  public interface BuildStep {
    FollowRequest build();
    BuildStep id(String id);
  }
  

  public static class Builder implements FromUserStep, ToUserStep, StatusStep, BuildStep {
    private String id;
    private String fromUser;
    private String toUser;
    private FollowRequestStatus status;
    @Override
     public FollowRequest build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new FollowRequest(
          id,
          fromUser,
          toUser,
          status);
    }
    
    @Override
     public ToUserStep fromUser(String fromUser) {
        Objects.requireNonNull(fromUser);
        this.fromUser = fromUser;
        return this;
    }
    
    @Override
     public StatusStep toUser(String toUser) {
        Objects.requireNonNull(toUser);
        this.toUser = toUser;
        return this;
    }
    
    @Override
     public BuildStep status(FollowRequestStatus status) {
        Objects.requireNonNull(status);
        this.status = status;
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
    private CopyOfBuilder(String id, String fromUser, String toUser, FollowRequestStatus status) {
      super.id(id);
      super.fromUser(fromUser)
        .toUser(toUser)
        .status(status);
    }
    
    @Override
     public CopyOfBuilder fromUser(String fromUser) {
      return (CopyOfBuilder) super.fromUser(fromUser);
    }
    
    @Override
     public CopyOfBuilder toUser(String toUser) {
      return (CopyOfBuilder) super.toUser(toUser);
    }
    
    @Override
     public CopyOfBuilder status(FollowRequestStatus status) {
      return (CopyOfBuilder) super.status(status);
    }
  }
  
}
