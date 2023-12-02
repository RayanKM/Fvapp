package com.amplifyframework.datastore.generated.model;


import androidx.core.util.ObjectsCompat;

import java.util.Objects;
import java.util.List;

/** This is an auto generated class representing the Person type in your schema. */
public final class Person {
  private final String name;
  private final String news;
  public String getName() {
      return name;
  }
  
  public String getNews() {
      return news;
  }
  
  private Person(String name, String news) {
    this.name = name;
    this.news = news;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Person person = (Person) obj;
      return ObjectsCompat.equals(getName(), person.getName()) &&
              ObjectsCompat.equals(getNews(), person.getNews());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getName())
      .append(getNews())
      .toString()
      .hashCode();
  }
  
  public static NameStep builder() {
      return new Builder();
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(name,
      news);
  }
  public interface NameStep {
    NewsStep name(String name);
  }
  

  public interface NewsStep {
    BuildStep news(String news);
  }
  

  public interface BuildStep {
    Person build();
  }
  

  public static class Builder implements NameStep, NewsStep, BuildStep {
    private String name;
    private String news;
    public Builder() {
      
    }
    
    private Builder(String name, String news) {
      this.name = name;
      this.news = news;
    }
    
    @Override
     public Person build() {
        
        return new Person(
          name,
          news);
    }
    
    @Override
     public NewsStep name(String name) {
        Objects.requireNonNull(name);
        this.name = name;
        return this;
    }
    
    @Override
     public BuildStep news(String news) {
        Objects.requireNonNull(news);
        this.news = news;
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String name, String news) {
      super(name, news);
      Objects.requireNonNull(name);
      Objects.requireNonNull(news);
    }
    
    @Override
     public CopyOfBuilder name(String name) {
      return (CopyOfBuilder) super.name(name);
    }
    
    @Override
     public CopyOfBuilder news(String news) {
      return (CopyOfBuilder) super.news(news);
    }
  }
  
}
