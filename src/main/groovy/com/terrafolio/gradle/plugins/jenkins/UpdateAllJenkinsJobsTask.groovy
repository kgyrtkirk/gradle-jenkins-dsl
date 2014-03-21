package com.terrafolio.gradle.plugins.jenkins


class UpdateAllJenkinsJobsTask extends UpdateJenkinsJobsTask {
	@Override
	def void doExecute() {
		jobsToUpdate = getJobs()
		viewsToUpdate = getViews()
		super.doExecute()
	}
}
