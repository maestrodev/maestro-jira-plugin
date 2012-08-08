maestro-jira-plugin
=============

Maestro plugin providing integration for JIRA. This
plugin is a Java-based deployable that gets delivered as a Zip file.

<http://www.atlassian.com/software/jira/overview>

In order to test the project you will need to update the test resources with the
correct options for your environment.

Manifest:

* src/main/java/../JiraWorker.java
* pom.xml
* README.md (this file)

## The Create Issue Task
The create issue task allows you to create issues in Jira against a specified
project.  It has several inputs.


* **host** name of server the JIRA is running on
* **port** port JIRA is listening to
* **use_ssl** Use ssl encryption
* **username** username to create issue as,
* **password**
* **web_path** context path for the JIRA webapp
* **project_key** token for referencing the project
* **summary** summary description for new issue
* **issue_type_name** type of issue to create
* **description** longer description for new issue


## The Transition Issue Task
The transition issue task will move a set ok issue keys to a named transition.


* **host** name of server the JIRA is running on
* **port** port JIRA is listening to
* **use_ssl** Use ssl encryption
* **username** username to create issue as,
* **password**
* **web_path** context path for the JIRA webapp
* **comment** description for issue transition
* **transition_name** name of the issue transition
* **issue_keys** list of issue keys to be transitioned

## License
Apache 2.0 License: <http://www.apache.org/licenses/LICENSE-2.0.html>