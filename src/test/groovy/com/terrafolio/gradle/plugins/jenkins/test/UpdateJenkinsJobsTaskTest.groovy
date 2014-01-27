package com.terrafolio.gradle.plugins.jenkins.test;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test
import org.junit.Before
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.tasks.TaskExecutionException

import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin
import com.terrafolio.gradle.plugins.jenkins.UpdateJenkinsJobsTask
import com.terrafolio.gradle.plugins.jenkins.JenkinsRESTServiceImpl
import com.terrafolio.gradle.plugins.jenkins.JenkinsServiceException
import com.terrafolio.gradle.plugins.jenkins.JenkinsConfigurationException
import com.terrafolio.gradle.plugins.jenkins.ConsoleFactory

import groovy.mock.interceptor.MockFor

class UpdateJenkinsJobsTaskTest {
	def private final Project project = ProjectBuilder.builder().withProjectDir(new File('build/tmp/test')).build()
	def private final JenkinsPlugin plugin = new JenkinsPlugin()
	def MockFor mockJenkinsRESTService
	
	@Before
	def void setupProject() {
		plugin.apply(project)
		
		project.ext.branches = [
			master: [ parents: [ ] ],
			develop: [ parents: [ 'master' ] ],
			releaseX: []
		]
		
		project.jenkins {
			server('test1'){
					url 'test1'
					username 'test1'
					password 'test1'
				}
			server('test2'){
					url 'test2'
					username 'test2'
					password 'test2'
				}
			project.branches.eachWithIndex { branchName, map, index ->
				job("compile_${branchName}") {
					server servers.test1 
				}
			}
		}
		
		mockJenkinsRESTService = new MockFor(JenkinsRESTServiceImpl.class)
	}
	
	@Test
	def void execute_updatesOneJob() {
		mockJenkinsRESTService.demand.with {
			createJob(0) { String jobName, String configXML -> }
			
			getJobConfiguration() { String jobName, Map overrides ->
				"<project><actions></actions><description>difference</description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
			}
			
			updateJobConfiguration() { String jobName, String configXML, Map overrides -> 
				assert jobName == project.jenkins.jobs.compile_master.name
			}
			
		}
		
		project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
			update(project.jenkins.jobs.compile_master)
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateOneJob.execute()
		}
	}
	
	@Test
	def void execute_skipsUpdateOnNoChange() {
		mockJenkinsRESTService.demand.with {
			createJob(0) { String jobName, String configXML -> }
			
			getJobConfiguration() { String jobName, Map overrides ->
				"<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
			}
			
			updateJobConfiguration(0) { String jobName, String configXML, Map overrides -> }
			
		}
		
		project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
			update(project.jenkins.jobs.compile_master)
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateOneJob.execute()
		}
	}
	
	@Test
	def void execute_updatesOnForceUpdateString() {
		mockJenkinsRESTService.demand.with {
			createJob(0) { String jobName, String configXML -> }
			
			getJobConfiguration() { String jobName, Map overrides ->
				"<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
			}
			
			updateJobConfiguration() { String jobName, String configXML, Map overrides ->
				assert jobName == project.jenkins.jobs.compile_master.name
			}
			
		}
		
		project.ext.forceJenkinsJobsUpdate = 'true'
		project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
			update(project.jenkins.jobs.compile_master)
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateOneJob.execute()
		}
	}
	
	@Test
	def void execute_updatesOnForceUpdateBoolean() {
		mockJenkinsRESTService.demand.with {
			createJob(0) { String jobName, String configXML -> }
			
			getJobConfiguration() { String jobName, Map overrides ->
				"<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
			}
			
			updateJobConfiguration() { String jobName, String configXML, Map overrides ->
				assert jobName == project.jenkins.jobs.compile_master.name
			}
			
		}
		
		project.ext.forceJenkinsJobsUpdate = true
		project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
			update(project.jenkins.jobs.compile_master)
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateOneJob.execute()
		}
	}
	
	@Test
	def void execute_callsCreateOnMissingJob() {
		mockJenkinsRESTService.demand.with {
			updateJobConfiguration(0) { String jobName, String configXML -> }
			
			getJobConfiguration() { String jobName, Map overrides ->
				return null
			}
			
			createJob() { String jobName, String configXML, Map overrides ->
				assert jobName == project.jenkins.jobs.compile_master.name
			}
			
		}
		
		project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
			update(project.jenkins.jobs.compile_master)
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateOneJob.execute()
		}
	}
	
	@Test
	def void execute_callsCreateOnMissingJobWithOverrides() {
		mockJenkinsRESTService.demand.with {
			updateJobConfiguration(0) { String jobName, String configXML -> }
			
			getJobConfiguration() { String jobName, Map overrides ->
				return null
			}
			
			createJob() { String jobName, String configXML, Map overrides ->
				assert jobName == project.jenkins.jobs.compile_master.name
				assert overrides.uri == "testUri"
			}
			
		}
		
		project.jenkins.jobs.compile_master {
			serviceOverrides {
				create = [ uri: "testUri" ]
			}
		}
		
		project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
			update(project.jenkins.jobs.compile_master)
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateOneJob.execute()
		}
	}
	
	@Test
	def void execute_updatesMultipleJobs() {
		mockJenkinsRESTService.demand.with {
			createJob(0) { String jobName, String configXML -> }
			
			getJobConfiguration() { String jobName, Map overrides ->
				"<project><actions></actions><description>difference</description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
			}
			
			updateJobConfiguration() { String jobName, String configXML, Map overrides ->
				assert jobName == project.jenkins.jobs.compile_master.name
			}
			
			getJobConfiguration() { String jobName, Map Overrides ->
				"<project><actions></actions><description>difference</description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
			}
			
			updateJobConfiguration() { String jobName, String configXML, Map overrides ->
				assert jobName == project.jenkins.jobs.compile_releaseX.name
			}
		}
		
		
		project.task('updateMultipleJobs', type: UpdateJenkinsJobsTask) {
			update(project.jenkins.jobs.compile_master)
			update(project.jenkins.jobs.compile_releaseX)
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateMultipleJobs.execute()
		}
	}
	
	@Test
	def void execute_updatesWithOverride() {
		mockJenkinsRESTService.demand.with {
			createJob(0) { String jobName, String configXML -> }
			
			getJobConfiguration() { String jobName, Map overrides ->
				assert overrides.uri == "anotherUri"
				"<project><actions></actions><description>difference</description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
			}
			
			updateJobConfiguration() { String jobName, String configXML, Map overrides ->
				assert jobName == project.jenkins.jobs.compile_master.name
				assert overrides.uri == "testUri"
				assert overrides.params.name == "test"
			}
			
		}
		
		project.jenkins.jobs.compile_master {
			serviceOverrides {
				update = [ uri: "testUri", params: [ name: "test" ] ]
				get = [ uri: "anotherUri" ]
			}
		}
		
		project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
			update(project.jenkins.jobs.compile_master)
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateOneJob.execute()
		}
	}
	 
}
