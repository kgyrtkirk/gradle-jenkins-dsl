package com.terrafolio.gradle.plugins.jenkins

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.util.ConfigureUtil

class JenkinsConfiguration {
	private final NamedDomainObjectContainer<JenkinsJob> jobs
	private final NamedDomainObjectContainer<JenkinsView> views
	private final NamedDomainObjectContainer<JenkinsServerDefinition> servers
	private final NamedDomainObjectContainer<JenkinsJobDefinition> templates
	
	def defaultServer
	
	public JenkinsConfiguration(NamedDomainObjectContainer<JenkinsJob> jobs,NamedDomainObjectContainer<JenkinsJob> views, NamedDomainObjectContainer<JenkinsJobDefinition> templates, NamedDomainObjectContainer<JenkinsServerDefinition> servers) {
		this.jobs = jobs
		this.servers = servers
		this.templates = templates
		this.views = views
	}
	
	def job(String name,Closure closure) {
		JenkinsJob job=jobs.maybeCreate(name)
		ConfigureUtil.configure(closure, job)
		job
	}
	
	def view(String name,Closure closure) {
		JenkinsView view=views.maybeCreate(name)
		ConfigureUtil.configure(closure, view)
		view
	}
	
	def server(String name,Closure closure) {
		JenkinsServerDefinition server=servers.create(name)
		ConfigureUtil.configure(closure, server)
		server
	}
	
	def defaultServer(JenkinsServerDefinition server) {
		this.defaultServer = server
	} 
	
	@Deprecated
	def templates(Closure closure) {
		templates.configure(closure)
	}
	
}
