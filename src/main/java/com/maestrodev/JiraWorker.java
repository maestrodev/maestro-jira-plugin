package com.maestrodev;

import com.maestrodev.jira.create.Project;
import com.maestrodev.jira.create.Fields;
import com.maestrodev.jira.create.JiraCreateResponse;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.Transition;
import com.atlassian.jira.rest.client.domain.input.TransitionInput;

import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;

import com.maestrodev.jira.create.Issuetype;
import com.maestrodev.jira.create.JiraCreate;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.org.apache.xml.internal.security.utils.Base64;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import javax.naming.AuthenticationException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JiraWorker
        extends MaestroWorker {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final String CREATE_ISSUE_PATH = "rest/api/2/issue/";

  private String getWebPath() {
    if (getField("web_path") == null) {
      return "/";
    }
    return "/" + getField("web_path").replaceAll("\\/$", "").replaceAll("^\\/", "");
  }

  private JiraCreate getJiraIssue() {
    JiraCreate jiraCreate = new JiraCreate();
    Fields fields = new Fields();
    Project project = new Project();
    project.setKey(getField("project_key"));
    fields.setSummary(getField("summary"));
    fields.setDescription(getField("description"));
    Issuetype issuetype = new Issuetype();
    issuetype.setName(getField("issue_type_name"));
    fields.setIssuetype(issuetype);
    fields.setProject(project);
    jiraCreate.setFields(fields);

    return jiraCreate;
  }

  /**
   * update an issue in Jira
   * 
   * @throws IOException
   * @throws JsonMappingException
   * @throws JsonGenerationException
   * @throws URISyntaxException
   */
  public void createIssue() throws JsonGenerationException, JsonMappingException, IOException {
    logger.info("Creating Issue In JIRA");
    writeOutput("Creating An Issue In JIRA\n");

    URI jiraServerUri;
    try {
      jiraServerUri = buildUri(CREATE_ISSUE_PATH);
    } catch (URISyntaxException e) {
      setError("URI is not valid: " + buildUrl(CREATE_ISSUE_PATH) );
      return;
    }


    String auth = String.valueOf(Base64.encode((this.getField("username")
            + ":" + this.getField("password")).getBytes()));
    ObjectMapper mapper = new ObjectMapper();



    String data = mapper.writeValueAsString(getJiraIssue());
    Client client = Client.create();
    WebResource webResource = client.resource(jiraServerUri.toString());
    ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json").accept("application/json").post(ClientResponse.class, data);
    int statusCode = response.getStatus();

    String body = response.getEntity(String.class);
    JiraCreateResponse createResponse = mapper.readValue(body, JiraCreateResponse.class);
    handleCreateResponse(statusCode, createResponse, jiraServerUri.getScheme().contains("https"), mapper);
  }

  private URI buildUri(String path) throws URISyntaxException {
    boolean useSsl = Boolean.parseBoolean(this.getField("use_ssl"));

    return (new URI(
            ("http" + (useSsl ? "s" : "")),
            null,
            this.getField("host"),
            Integer.parseInt(this.getField("port")),
            this.getWebPath() + path,
            null,
            null));
  }

  private String buildUrl(String path) {
    boolean useSsl = Boolean.parseBoolean(this.getField("use_ssl"));
    return "http" + (useSsl ? "s" : "") + "://" + this.getField("host") + ":" + this.getField("port") + "/" + this.getWebPath() + path;
  }

  private void handleCreateResponse(int statusCode, JiraCreateResponse createResponse, boolean useSsl, ObjectMapper mapper) {
    if (statusCode == 401) {
      setError("Invalid Username or Password");
    } else if (statusCode == 404) {
      setError("Rest Endpoint Not Found, Make Sure Jira Is "
              + "At Least Version 5 And Accept Remote API Calls Is Enabled");
    } else if (statusCode == 201) {
      writeOutput("Successfully Created An Issue In Jira\n");
      writeOutput("Issue Key :: " + createResponse.getKey() + "\n");

      String link;
      try {
        link = (buildUri("/browse/" + createResponse.getKey())).toASCIIString();
      } catch (URISyntaxException e) {
        setError("URI is not valid: " + buildUrl("/browse/" + createResponse.getKey()));
        return;
      }
      writeOutput("Link :: " + link + "\n");
      addLink("Issue " + createResponse.getKey(), link);

      setField("jira", mapper.convertValue(createResponse, Map.class));
    } else {
      String errorMessage = "";
      if (createResponse.getErrorMessages() != null) {
        for (String message : createResponse.getErrorMessages()) {
          errorMessage += message + "\n";
        }

      }

      if (createResponse.getErrors() != null) {
        for (Object key : createResponse.getErrors().keySet()) {
          errorMessage += key + " :: " + createResponse.getErrors().get(key) + "\n";
        }
      }

      setError(errorMessage);
    }
  }

  /**
   * update an issue in Jira
   */
  public void transitionIssues() {
    logger.info("Transitioning Issue In JIRA");
    writeOutput("Transitioning An Issue In JIRA\n");


    JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
    URI jiraServerUri;
    try {
      jiraServerUri = buildUri("");
    } catch (URISyntaxException e) {
      setError("URI is not valid: " + buildUrl("") );
      return;
    }

    JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri,
            this.getField("username"),
            this.getField("password"));
    NullProgressMonitor pm = new NullProgressMonitor();

    if (getFields().get("issue_keys") == null || ((List) getFields().get("issue_keys")).isEmpty()) {
      throw new RuntimeException("Null Or Empty Issue Key List");
    }
    List<String> issueKeys = (List<String>) getFields().get("issue_keys");

    for (String issueKey : issueKeys) {
      Issue issue = restClient.getIssueClient().getIssue(issueKey, pm);
      if (issue == null) {
        setError("Issue Not Found: " + issueKey);
        return;
      }

      final Iterable<Transition> transitions = restClient.getIssueClient().getTransitions(issue, pm);
      String transitionName = getField("transition_name");
      Transition transition = getTransitionByName(transitions, transitionName);
      if (transition == null) {
        setError("Transition Not Found: " + transitionName);
        return;
      }

      transitionIssueToTransitionName(restClient, issue, transition, pm);
    }
  }

  private void transitionIssueToTransitionName(JiraRestClient restClient, Issue issue, Transition transition, NullProgressMonitor pm) {
    TransitionInput transitionInput = new TransitionInput(transition.getId());
    restClient.getIssueClient().transition(issue, transitionInput, pm);

    writeOutput("Successfully Transitioned Issue " + issue.getKey() + " To " + transition.getName() + "\n");
  }

  private static Transition getTransitionByName(Iterable<Transition> transitions, String transitionName) {
    for (Transition transition : transitions) {
      if (transition.getName().equals(transitionName)) {
        return transition;
      }
    }
    return null;
  }
}
