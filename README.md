in this fork:

* merged functionality of original gradle-jenkins-plugin + jenkins/job-dsl-core
* plugin available in mavencentral along with job-dsl-core
* gradle declaration dialect for servers/jobs have been changed in 0.6
	* servers { a {B} ; b {B} } -> server('a') {B} ; server('b') {B} 
	* template support has been removed temporarily
	* 'uri' declaration for servers
* versioning scheme changed to start with base job-dsl-core version...from now on: 1.22.1
* views are supported


by using this plugin enables the following:

```groovy
apply plugin: 'jenkins'

buildscript {
	repositories { mavenCentral() }
	dependencies {
		classpath('hu.rxd:gradle-jenkins-dsl:1.22.+')
	}
}

jenkins {
	server('testing') {
		url 'http://localhost:8081'
		secure false
		//username "testuser" // optional
		//password "testpass" // optional
	}
	server('t1'){
		uri 'https://user:pass@example.com'
	}

	defaultServer servers.testing // optional

	job('test'){
		server servers.testing
		steps { shell("ls -l") }
	}

	job('test2') {
		steps { shell("ls") }
	}
	
	view('view1') {
	   
	}
}
```

jenkins-job-dsl home:
https://github.com/jenkinsci/job-dsl-plugin/wiki


This plugin started as a fork of ghale's gradle-jenkins-plugin (https://github.com/ghale/gradle-jenkins-plugin/wiki)

thank you for creating 
original plugin's :

gradle-jenkins-plugin
=====================

Gradle plugin to programmatically configure Jenkins jobs.  This plugin allows you to maintain jenkins job configurations in source control and apply them to the server via gradle.  Jobs can be stored as straight xml files, xml strings, or as markup builder closures.  Job templates can be defined that can then be manipulated in Groovy XmlSlurper fashion such that multiple jobs can be generated off of a single template definition.

See https://github.com/ghale/gradle-jenkins-plugin/wiki for details on usage.

              
