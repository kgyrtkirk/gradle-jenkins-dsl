in this fork:

[X] merged functionality of original gradle-jenkins-plugin + jenkins/job-dsl-core
[ ] push plugin into some repository - you have to build it yourself

by using this plugin enables the following:

```groovy
apply plugin: 'jenkins'

jenkins {
	servers {
		testing {
			url 'http://localhost:8081'
			secure false
			//username "testuser" // optional
			//password "testpass" // optional
		}
	}

	defaultServer servers.testing // optional
	jobs {
		test {
			server servers.testing
			steps { shell("ls -l") }
		}

		test2 {
			steps { shell("ls") }
		}
	}
}
```

jenkins-job-dsl home:
https://github.com/jenkinsci/job-dsl-plugin/wiki	


original plugin's readme continues:

gradle-jenkins-plugin
=====================

Gradle plugin to programmatically configure Jenkins jobs.  This plugin allows you to maintain jenkins job configurations in source control and apply them to the server via gradle.  Jobs can be stored as straight xml files, xml strings, or as markup builder closures.  Job templates can be defined that can then be manipulated in Groovy XmlSlurper fashion such that multiple jobs can be generated off of a single template definition.

See https://github.com/ghale/gradle-jenkins-plugin/wiki for details on usage.

              
