
package com.maestrodev.jira.create;

import java.util.List;
import java.util.Map;

/**
 *
 * @author kelly
 */
public class JiraCreateResponse {
  private String id;
  private String key;
  private String self;
  private List<String> errorMessages;
  private Map errors;

  public Map getErrors() {
    return errors;
  }

  public void setErrors(Map errors) {
    this.errors = errors;
  }
  
  public List<String> getErrorMessages() {
      return errorMessages;
  }

  public void setErrorMessages(List<String> errorMessages) {
      this.errorMessages = errorMessages;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getSelf() {
    return self;
  }

  public void setSelf(String self) {
    this.self = self;
  }
}
