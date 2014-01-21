package com.terrafolio.gradle.plugins.jenkins.test;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test
import org.junit.Before
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.tasks.TaskExecutionException

import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin
import com.terrafolio.gradle.plugins.jenkins.JenkinsRESTServiceImpl
import com.terrafolio.gradle.plugins.jenkins.JenkinsServiceException
import com.terrafolio.gradle.plugins.jenkins.JenkinsConfigurationException
import com.terrafolio.gradle.plugins.jenkins.ConsoleFactory

import groovy.mock.interceptor.MockFor

class JenkinsServerConfigurationTest {
	def private final Project project = ProjectBuilder.builder().withProjectDir(new File('build/tmp/test')).build()
	def private final JenkinsPlugin plugin = new JenkinsPlugin()
	def MockFor mockJenkinsRESTService
	
	@Before
	def void setupProject() {
		plugin.apply(project)
		
		project.ext.branches = [
			master: [ parents: [ ] ],
			develop: [ parents: [ 'master' ] ]
		]
		
		project.jenkins {
			servers {
				test1 {
					url 'test1'
					username 'test1'
					password 'test1'
				}
				test2 {
					url 'test2'
					username 'test2'
					password 'test2'
				}
			}
			templates {
				compile {
					xml "<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
				}
			}
			jobs {
				project.branches.eachWithIndex { branchName, map, index ->
					"compile_${branchName}" {
						server servers.test1
					}
				}
			}
		}
		
		mockJenkinsRESTService = new MockFor(JenkinsRESTServiceImpl.class)
	}
	
	@Test
	def void execute_usesDefaultServer() {
		mockJenkinsRESTService.demand.with {
			updateJobConfiguration(0) { String jobName, String configXML -> }
			
			2.times {
				getJobConfiguration() { String jobName, Map overrides ->
					null
				}
				
				createJob() { String jobName, String configXML, Map overrides ->
					if (! project.jenkins.jobs.collect { it.name }.contains(jobName)) {
						throw new Exception('createJob called with: ' + jobName + ' but no job definition exists with that name!')
					}
				}
			
			}
		}
		
		project.jenkins {
			defaultServer project.jenkins.servers.test1
		}
		
		project.jenkins.jobs.each { job ->
			job.serverDefinitions = []
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateJenkinsJobs.execute()
		}
	}
	
	@Test(expected = JenkinsConfigurationException)
	def void execute_throwsExceptionOnMissingServer() {
		project.jenkins.jobs.each { job ->
			job.serverDefinitions = []
		}
		
		mockJenkinsRESTService.use {
			try {
				project.tasks.updateJenkinsJobs.execute()
			} catch (TaskExecutionException e) {
				throw e.cause
			} 
		}
	}
	
	@Test(expected = JenkinsConfigurationException)
	def void execute_throwsExceptionForMissingURL() {
		mockJenkinsRESTService.demand.with {
			updateJobConfiguration(0) { String jobName, String configXML -> }
			
			2.times {
				getJobConfiguration() { String jobName ->
					null
				}
				
				createJob() { String jobName, String configXML ->
					if (! project.jenkins.jobs.collect { it.name }.contains(jobName)) {
						throw new Exception('createJob called with: ' + jobName + ' but no job definition exists with that name!')
					}
				}
			
			}
		}
		
		project.jenkins.servers.each { server ->
			server.url = null
		}
		
		mockJenkinsRESTService.use {
			try {
				project.tasks.updateJenkinsJobs.execute()
			} catch (TaskExecutionException e) {
				throw e.cause
			} 
		}
	}
	
	@Test(expected = JenkinsConfigurationException)
	def void execute_throwsExceptionForMissingUsername() {
		mockJenkinsRESTService.demand.with {
			updateJobConfiguration(0) { String jobName, String configXML -> }
			
			2.times {
				getJobConfiguration() { String jobName ->
					null
				}
				
				createJob() { String jobName, String configXML ->
					if (! project.jenkins.jobs.collect { it.name }.contains(jobName)) {
						throw new Exception('createJob called with: ' + jobName + ' but no job definition exists with that name!')
					}
				}
			
			}
		}
		
		project.jenkins.servers.each { server ->
			server.username = null
		}
		
		mockJenkinsRESTService.use {
			try {
				project.tasks.updateJenkinsJobs.execute()
			} catch (TaskExecutionException e) {
				throw e.cause
			}
		}
	}
	
	@Test(expected = JenkinsConfigurationException)
	def void execute_throwsExceptionForMissingPassword() {
		mockJenkinsRESTService.demand.with {
			updateJobConfiguration(0) { String jobName, String configXML -> }
			
			2.times {
				getJobConfiguration() { String jobName ->
					null
				}
				
				createJob() { String jobName, String configXML ->
					if (! project.jenkins.jobs.collect { it.name }.contains(jobName)) {
						throw new Exception('createJob called with: ' + jobName + ' but no job definition exists with that name!')
					}
				}
			
			}
		}
		
		project.jenkins.servers.each { server ->
			server.password = null
		}
		
		mockJenkinsRESTService.use {
			try {
				project.tasks.updateJenkinsJobs.execute()
			} catch (TaskExecutionException e) {
				throw e.cause
			}
		}
	}
	
	@Test
	def void execute_promptsForCredentialsBeforeExecution() {
		project.ext.credentialsGathered = false
		mockJenkinsRESTService.demand.with {
			updateJobConfiguration(0) { String jobName, String configXML, Map overrides -> 	
			}
			
			4.times {
				getJobConfiguration() { String jobName, Map overrides ->
					null
				}
				
				createJob() { String jobName, String configXML, Map overrides ->
					assert project.ext.credentialsGathered
					if (! project.jenkins.jobs.collect { it.name }.contains(jobName)) {
						throw new Exception('createJob called with: ' + jobName + ' but no job definition exists with that name!')
					}
				}
			
			}
		}
		
		def console = new GroovyObject() {
							def readLine_called = 0
							def readPassword_called = 0
							def shouldBeCalled = 2
							
							def String readLine(String message, Object nothing) {
								readLine_called++
								if (readLine_called >= shouldBeCalled && readPassword_called >= shouldBeCalled) project.ext.credentialsGathered = true
								return 'mockUser'
							}
							
							def String readPassword(String message, Object nothing) {
								readPassword_called++
								if (readLine_called >= shouldBeCalled && readPassword_called >= shouldBeCalled) project.ext.credentialsGathered = true
								return 'mockPass'
							}
						}
		def mockConsoleFactory = new MockFor(ConsoleFactory.class)
		mockConsoleFactory.demand.with {
			2.times {
				getConsole() {
					return console
				}
			}
			
		}
		
		project.jenkins.servers.each { server ->
			server.username = null
			server.password = null
		}
		
		project.jenkins.jobs.each { job ->
			job.server project.jenkins.servers.test2
		}
		
		mockConsoleFactory.use {
		mockJenkinsRESTService.use {
			try {
				project.tasks.updateJenkinsJobs.execute()
			} catch (TaskExecutionException e) {
				throw e.cause
			}
		}
		}
	}
	
	@Test
	def void execute_allowsMissingUsernameForInsecureServer() {
		mockJenkinsRESTService.demand.with {
			updateJobConfiguration(0) { String jobName, String configXML, Map overrides -> }
			
			2.times {
				getJobConfiguration() { String jobName, Map overrides ->
					null
				}
				
				createJob() { String jobName, String configXML, Map overrides ->
					if (! project.jenkins.jobs.collect { it.name }.contains(jobName)) {
						throw new Exception('createJob called with: ' + jobName + ' but no job definition exists with that name!')
					}
				}
			
			}
		}
		
		project.jenkins.servers.each { server ->
			server.secure = false
			server.username = null
		}
		
		mockJenkinsRESTService.use {
			try {
				project.tasks.updateJenkinsJobs.execute()
			} catch (TaskExecutionException e) {
				throw e.cause
			}
		}
	}
	
	@Test
	def void execute_allowsMissingPasswordForInsecureServer() {
		mockJenkinsRESTService.demand.with {
			updateJobConfiguration(0) { String jobName, String configXML, Map overrides -> }
			
			2.times {
				getJobConfiguration() { String jobName, Map overrides ->
					null
				}
				
				createJob() { String jobName, String configXML, Map overrides ->
					if (! project.jenkins.jobs.collect { it.name }.contains(jobName)) {
						throw new Exception('createJob called with: ' + jobName + ' but no job definition exists with that name!')
					}
				}
			
			}
		}
		
		project.jenkins.servers.each { server ->
			server.secure = false
			server.password = null
		}
		
		mockJenkinsRESTService.use {
			try {
				project.tasks.updateJenkinsJobs.execute()
			} catch (TaskExecutionException e) {
				throw e.cause
			}
		}
	}

	@Test void execute_configuresServerSpecificConfiguration() {
		mockJenkinsRESTService.demand.with {
			updateJobConfiguration(0) { String jobName, String configXML -> }
			
			4.times {
				getJobConfiguration() { String jobName, Map overrides ->
					null
				}
				
				createJob() { String jobName, String configXML, Map overrides ->
					if (! project.jenkins.jobs.collect { it.name }.contains(jobName)) {
						throw new Exception('createJob called with: ' + jobName + ' but no job definition exists with that name!')
					}
				}
			
			}
		}
		
		project.jenkins.jobs.each { job ->
			job.server project.jenkins.servers.test2, { 
				xml override { projectXml ->
					projectXml.description = 'This is for test2'
				}
			}
		}
		
		mockJenkinsRESTService.use {
			project.jenkins.jobs.each { job ->
				job.serverDefinitions.each { server ->
					def definition = job.getServerSpecificDefinition(server)
					if (server.name == 'test2') {
						assert definition.xml.contains("This is for test2")
					} else {
						assert !definition.xml.contains("This is for test2")
					}
				}
			}
			project.tasks.updateJenkinsJobs.execute()
		}
	} 
}
