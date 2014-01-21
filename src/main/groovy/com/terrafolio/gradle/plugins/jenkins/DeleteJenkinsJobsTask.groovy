package com.terrafolio.gradle.plugins.jenkins

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class DeleteJenkinsJobsTask extends AbstractJenkinsTask {
	def jobsToDelete = []

	def void doExecute() {
		jobsToDelete.each { job ->
			eachServer(job) { JenkinsServerDefinition server, JenkinsService service ->
				def existing = service.getJobConfiguration(job.name, job.serviceOverrides.get)
				if (existing != null) {
					logger.warn('Deleting job ' + job.name + ' on ' + server.url)
					service.deleteJob(job.name, job.serviceOverrides.delete)
				} else {
					logger.warn('Jenkins job ' + job.name + ' does not exist on ' + server.url)
				}
			}
		}
	}
	
	def void delete(JavaPosseJenkinsJob job) {
		jobsToDelete += job
	}
	
	def void delete(JenkinsServerDefinition server, String jobName) {
		def job = new JavaPosseJenkinsJob(jobName)
		job.server server
		delete(job)
	}
}
