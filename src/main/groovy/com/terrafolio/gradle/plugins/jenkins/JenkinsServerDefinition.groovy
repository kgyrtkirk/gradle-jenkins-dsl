package com.terrafolio.gradle.plugins.jenkins

import groovyx.net.http.URIBuilder

class JenkinsServerDefinition {
	def name
	def url
	def username
	def password
	def secure = true
	def console
	
	JenkinsServerDefinition(String name) {
		this.name = name
	}
	
	def uri(String uri) {
		URI u=new URI(uri)
		if(u.userInfo != null && u.userInfo.length() > 0){
			def parts=u.userInfo.split(":")
			this.username=parts[0]
			this.password=parts[1]
		}else{
			secure=false
		}
		URIBuilder urib=new URIBuilder(u)
		urib.userInfo=null
		this.url = urib.toString()
	}

	def url(String url) {
		this.url = url
	}
	
	def username(String username) {
		this.username = username
	}
	
	def password(String password) {
		this.password = password
	}
	
	def secure(Boolean secure) {
		this.secure = secure
	}
	
	def void checkDefinitionValues() {
		if (console == null) {
			 console = ConsoleFactory.getConsole()
		}
		
		if (url == null) {
			if (console != null) {
				url = console.readLine("\nEnter the URL for server \"${name}\": ", null)
			} else {
				throw new JenkinsConfigurationException("No URL defined for server \"${name}\" and no console available for input.")
			}
		}
		
		if (secure) {
			if (username == null) {
				if (console != null) {
					username = console.readLine("\nEnter the username for server \"${name}\": ", null)
				} else {
					throw new JenkinsConfigurationException("No username defined for server \"${name}\" and no console available for input.")
				}
			}
			
			if (password == null) {
				if (console != null) {
					password = new String(console.readPassword("\nEnter the password for server \"${name}\": ", null))
				} else {
					throw new JenkinsConfigurationException("No password defined for server \"${name}\" and no console available for input.")
				}
			}
		}
	}
	
	String toString(){
		"username: ${username}, password:${password}, url:${url}, secure:${secure}"
	}
}
