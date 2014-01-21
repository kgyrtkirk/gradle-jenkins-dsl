package com.terrafolio.gradle.plugins.jenkins.test

import static org.junit.Assert.*
import groovy.lang.GroovyObject;
import groovy.mock.interceptor.MockFor
import groovy.xml.StreamingMarkupBuilder

import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.After
import org.junit.Test

import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin
import com.terrafolio.gradle.plugins.jenkins.ConsoleFactory

class DumpJenkinsJobsTaskTest {
	def private final Project project = ProjectBuilder.builder().withProjectDir(new File('build/tmp/test')).build()
	def private final JenkinsPlugin plugin = new JenkinsPlugin()

	@Before
	def void setupProject() {
		plugin.apply(project)

		project.jenkins {
			servers {
				test1 {
					url 'test1'
					username 'test1'
					password 'test1'
				}
			}
			
			templates {
				compile { xml "<?xml version='1.0' encoding='UTF-8'?><project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>" }
				compile2 { xml "<?xml version='1.0' encoding='UTF-8'?><project><actions></actions><description></description><keepDependencies>true</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>" }
			}
			
			jobs {
				job1 {
					server servers.test1
				}

				job2 {
					server servers.test1
				}
			}
		}
	}
	
	@After
	def void cleanUp() {
		def File dumpDir = new File('build/tmp/test/build/jobs')
		dumpDir.deleteDir()
	}

	@Test
	def void execute_dumpsJobsToFiles() {
		project.tasks.dumpJenkinsJobs.execute()
		
		def dumpDir = new File('build/tmp/test/build/jobs')
		project.jenkins.jobs.each { job ->
			def jobFile = new File(dumpDir, "${job.name}-config-test1.xml")
			assert jobFile.exists()
			
			XMLUnit.setIgnoreWhitespace(true)
			def xmlDiff = new Diff(job.xml, jobFile.getText())
			assert xmlDiff.similar()
		}
	}
	
	@Test
	def void execute_observesJobFilter() {
		project.ext.jenkinsJobFilter = 'job1'
		project.tasks.dumpJenkinsJobs.execute()
		
		def dumpDir = new File('build/tmp/test/build/jobs')
		
		assert new File(dumpDir, "job1-config-test1.xml").exists()
		assert ! (new File(dumpDir, "job2-config-test1.xml").exists())
	}
	
	@Test
	def void execute_doesNotPromptForCredentials() {
		def mockConsoleFactory = new MockFor(ConsoleFactory.class)
		mockConsoleFactory.demand.with {
			getConsole(0)
		}
		
		project.jenkins.servers.each { server ->
			server.username = null
			server.password = null
		}
		
		mockConsoleFactory.use {
			project.tasks.dumpJenkinsJobs.execute()
		}
	}
	
	@Test
	def void execute_dumpsRawJobsToFile() {
		project.tasks.dumpJenkinsJobs.prettyPrint = false
		project.tasks.dumpJenkinsJobs.execute()
		
		def dumpDir = new File('build/tmp/test/build/jobs')
		project.jenkins.jobs.each { job ->
			def jobFile = new File(dumpDir, "${job.name}-config-test1.xml")
			assert jobFile.exists()
			assert jobFile.getText() == job.xml
		}
	}
	
	@Test
	def void execute_dumpsMultipleServerConfigs() {
		project.tasks.dumpJenkinsJobs.prettyPrint = false
		project.jenkins.servers {
			test3 {
				url 'http://test3'
			}
		}
		project.jenkins.jobs.job1.server(
			project.jenkins.servers.test3,
			{
				xml project.jenkins.templates.compile2.xml
			}
		)
		
		project.tasks.dumpJenkinsJobs.execute()
		
		def dumpDir = new File('build/tmp/test/build/jobs')
		
		def jobFile = new File(dumpDir, "job1-config-test1.xml")
		assert jobFile.exists()
		assert jobFile.getText() == project.jenkins.jobs.job1.xml
		
		jobFile = new File(dumpDir, "job1-config-test3.xml")
		assert jobFile.exists()
		assert jobFile.getText() == project.jenkins.templates.compile2.xml
	}
}
