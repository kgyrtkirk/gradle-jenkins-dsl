package com.terrafolio.gradle.plugins.jenkins.test

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;

import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin;

import spock.lang.Specification
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual

class GradleDeclarationDialectTest extends Specification{

	def 'use job(name) dialect'() {
		setup:
		Project project = ProjectBuilder.builder().build()
		project.apply plugin: JenkinsPlugin

		when:
		project.jenkins{
			job("xxx"){
			}
		}
		then:
		project.jenkins.jobs.xxx != null
	}

	def 'set server by uri'() {
		setup:
		Project project = ProjectBuilder.builder().build()
		project.apply plugin: JenkinsPlugin

		when:
		project.jenkins{
			server("s1"){
				uri "https://username:password@host"
			}
		}
		then:
		project.jenkins.servers.s1.username == 'username'
		project.jenkins.servers.s1.password == 'password'
		project.jenkins.servers.s1.url == 'https://host'
	}

}
