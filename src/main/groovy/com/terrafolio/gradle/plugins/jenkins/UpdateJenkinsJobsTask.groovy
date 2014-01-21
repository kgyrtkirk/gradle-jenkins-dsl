package com.terrafolio.gradle.plugins.jenkins

import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class UpdateJenkinsJobsTask extends AbstractJenkinsTask {
	def jobsToUpdate = []
	
	def void doExecute() {
		jobsToUpdate.each { job ->
			eachServer(job) { JenkinsServerDefinition server, JenkinsService service ->
				def existing = service.getJobConfiguration(job.name, job.serviceOverrides.get)
				if (existing == null) {
					logger.warn('Creating new job ' + job.name + ' on ' + server.url)
					service.createJob(job.name, job.getServerSpecificDefinition(server).xml, job.serviceOverrides.create)
				} else {
					XMLUnit.setIgnoreWhitespace(true)
					def Diff xmlDiff = new Diff(job.xml, existing)
					if ((! xmlDiff.similar()) || (project.hasProperty('forceJenkinsJobsUpdate') && Boolean.valueOf(project.forceJenkinsJobsUpdate))) {
						logger.warn('Updating job ' + job.name + ' on ' + server.url)
						service.updateJobConfiguration(job.name, job.getServerSpecificDefinition(server).xml, job.serviceOverrides.update)
					} else {
						logger.warn('Jenkins job ' + job.name + ' has no changes to the existing job on ' + server.url)
					}
				}
			}
		}
	}
	
	def void update(JavaPosseJenkinsJob job) {
		jobsToUpdate += job
	}
}
