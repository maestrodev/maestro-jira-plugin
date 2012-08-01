
package com.maestrodev.jira.create;

import java.util.List;

public class Worklog{
   	private String started;
   	private String timeSpent;

 	public String getStarted(){
		return this.started;
	}
	public void setStarted(String started){
		this.started = started;
	}
 	public String getTimeSpent(){
		return this.timeSpent;
	}
	public void setTimeSpent(String timeSpent){
		this.timeSpent = timeSpent;
	}
}
