
package com.maestrodev.jira.create;

import java.util.List;

public class TimeTracking{
   	private String originalEstimate;
   	private String remainingEstimate;

 	public String getOriginalEstimate(){
		return this.originalEstimate;
	}
	public void setOriginalEstimate(String originalEstimate){
		this.originalEstimate = originalEstimate;
	}
 	public String getRemainingEstimate(){
		return this.remainingEstimate;
	}
	public void setRemainingEstimate(String remainingEstimate){
		this.remainingEstimate = remainingEstimate;
	}
}
