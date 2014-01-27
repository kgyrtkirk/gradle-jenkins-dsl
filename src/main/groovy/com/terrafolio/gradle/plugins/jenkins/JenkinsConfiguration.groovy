package com.terrafolio.gradle.plugins.jenkins

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.util.Configurable
import org.gradle.util.ConfigureUtil;

class JenkinsConfiguration {
	private final NamedDomainObjectContainer<JavaPosseJenkinsJob> jobs
	private final NamedDomainObjectContainer<JenkinsServerDefinition> servers
	private final NamedDomainObjectContainer<JenkinsJobDefinition> templates
	
	def defaultServer
	
	public JenkinsConfiguration(NamedDomainObjectContainer<JavaPosseJenkinsJob> jobs, NamedDomainObjectContainer<JenkinsJobDefinition> templates, NamedDomainObjectContainer<JenkinsServerDefinition> servers) {
		this.jobs = jobs
		this.servers = servers
		this.templates = templates
	}
	
	def job(String name,Closure closure) {
		JavaPosseJenkinsJob job=jobs.maybeCreate(name)
		ConfigureUtil.configure(closure, job)
		job
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
