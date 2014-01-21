package com.terrafolio.gradle.plugins.jenkins

import org.gradle.util.ConfigureUtil;

import groovy.lang.Closure;
import javaposse.jobdsl.dsl.Job

class JavaPosseJenkinsJob extends Job {

	JavaPosseJenkinsJob(String jobName){
		super(null)
		name=jobName
	}
	
	def serverDefinitions = []
	def serverSpecificConfiguration = [:]
//	def definition
	def serviceOverrides = new JenkinsOverrides()
//	JenkinsJob(String name) {
//		this.name = name
//	}
	
//	def definition(JenkinsJobDefinition definition) {
//		this.definition = definition
//	}
	
//	def definition(Closure closure) {
//		if (this.definition == null) {
//			this.definition = new JenkinsJobDefinition(name)
//		}
//		ConfigureUtil.configure(closure, definition)
//	}
	
	def serviceOverrides(JenkinsOverrides overrides) {
		this.serviceOverrides = overrides
	}
	
	def serviceOverrides(Closure closure) {
		if (this.serviceOverrides == null) {
			this.serviceOverrides = new JenkinsOverrides()
		}
		ConfigureUtil.configure(closure, serviceOverrides)
	}
	
	def server(JenkinsServerDefinition server) {
		if (! serverDefinitions.contains(server)) {
			serverDefinitions += server
		}
	}
	
	def server(JenkinsServerDefinition server, Closure closure) {
		this.server(server)
		if (! serverSpecificConfiguration.containsKey(server)) {
			serverSpecificConfiguration[server] = []
		}
		serverSpecificConfiguration[server] += closure
	}
	
	def getServerSpecificDefinition(JenkinsServerDefinition server) {
		if (serverSpecificConfiguration.containsKey(server)) {
			def newDefinition = new JenkinsJobDefinition(name, xml)
			
			serverSpecificConfiguration[server].each { closure ->
				ConfigureUtil.configure(closure, newDefinition)
			}
			
			return newDefinition
		} else {
			return this
		}
	}

}
