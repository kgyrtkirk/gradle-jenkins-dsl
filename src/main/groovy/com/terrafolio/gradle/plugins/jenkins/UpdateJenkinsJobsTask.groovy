package com.terrafolio.gradle.plugins.jenkins

import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class UpdateJenkinsJobsTask extends AbstractJenkinsTask {
	def jobsToUpdate = []
	def viewsToUpdate = []
	
	def void doExecute() {
		jobsToUpdate.each { JenkinsJob job ->
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
		viewsToUpdate.each { JenkinsView view ->
			eachServer(view) { JenkinsServerDefinition server, JenkinsService service ->
				def existing = service.getViewConfiguration(view.name, view.serviceOverrides.get)
				if (existing == null) {
					logger.warn('Creating new job ' + view.name + ' on ' + server.url)
					service.createView(view.name, view.getServerSpecificDefinition(server).xml, view.serviceOverrides.create)
				} else {
					XMLUnit.setIgnoreWhitespace(true)
					def Diff xmlDiff = new Diff(view.xml, existing)
					if ((! xmlDiff.similar()) || (project.hasProperty('forceJenkinsJobsUpdate') && Boolean.valueOf(project.forceJenkinsJobsUpdate))) {
						logger.warn('Updating job ' + view.name + ' on ' + server.url)
						service.updateViewConfiguration(view.name, view.getServerSpecificDefinition(server).xml, view.serviceOverrides.update)
					} else {
						logger.warn('Jenkins job ' + view.name + ' has no changes to the existing job on ' + server.url)
					}
				}
			}
		}
	}
	
	def void update(JenkinsJob job) {
		jobsToUpdate += job
	}
}
