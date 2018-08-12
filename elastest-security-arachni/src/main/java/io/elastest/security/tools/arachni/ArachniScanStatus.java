package io.elastest.security.tools.arachni;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ArachniScanStatus {
	
	public static class Statistics {
		
		@JsonProperty("browser_cluster")
		private BrowserCluster browserCluster;

		
		public BrowserCluster getBrowserCluster() {
			return browserCluster;
		}

		public void setBrowserCluster(BrowserCluster browserCluster) {
			this.browserCluster = browserCluster;
		}
		
	}
	
	public static class BrowserCluster {
		
		@JsonProperty("completed_job_count")
		private Integer jobsCompleted;
		
		@JsonProperty("queued_job_count")
		private Integer jobsQueued;

		
		public Integer getJobsCompleted() {
			return jobsCompleted;
		}

		public void setJobsCompleted(Integer jobsCompleted) {
			this.jobsCompleted = jobsCompleted;
		}

		public Integer getJobsQueued() {
			return jobsQueued;
		}

		public void setJobsQueued(Integer jobsQueued) {
			this.jobsQueued = jobsQueued;
		}
		
	}
	

	private String status;
	
	private Statistics statistics;

	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}	

	public Statistics getStatistics() {
		return statistics;
	}

	public void setStatistics(Statistics statistics) {
		this.statistics = statistics;
	}
	
	public String getProgress() {
		String progress = "";
		
		if ((statistics != null) && (statistics.getBrowserCluster() != null)) {
			BrowserCluster cluster = statistics.getBrowserCluster();
			if ((cluster.getJobsCompleted() != null) && (cluster.getJobsQueued() != null)) {
				progress += cluster.getJobsCompleted() + "/" + cluster.getJobsQueued();
			}
		}
		
		return progress;
	}

}
