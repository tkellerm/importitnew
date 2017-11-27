@Library('esdk-jenkins-lib@master') _

def version = ""
timestamps {
	ansiColor('xterm') {
		node {
			try {
				properties([parameters([
						string(name: 'ERP_VERSION', defaultValue: '2016r4n13', description: 'abas Essentials version')
				])
				])
				stage('Setup') {
					checkout scm
					sh "git reset --hard origin/$BRANCH_NAME"
					sh "git clean -fd"
					prepareEnv()
					rmDirInMavenLocal '​de/abas/esdk'
					currentBuild.description = "ERP Version: ${params.ERP_VERSION}"
					initGradleProps()
					showGradleProps()
				}
				stage('Preparation') { // for display purposes
					version = readVersion()
					withCredentials([usernamePassword(credentialsId: '82305355-11d8-400f-93ce-a33beb534089',
							passwordVariable: 'MAVENPASSWORD', usernameVariable: 'MAVENUSER')]) {
						shDocker('login intra.registry.abas.sh -u $MAVENUSER -p $MAVENPASSWORD')
					}
					withEnv(["ERP_VERSION=${params.ERP_VERSION}"]) {
						shDockerComposeUp()
					}
					sleep 30
				}
				stage('Installation') {
					shGradle("clean")
					shGradle("checkPreconditions -x importKeys")
					shGradle("publishHomeDirJars")
					shGradle("fullInstall")
					shGradle("createAppJar")
				}
				stage('Verify') {
					shGradle("cobertura")
				}
				stage('IntegTest') {
					// esdk-installer test
					withCredentials([usernamePassword(credentialsId: '82305355-11d8-400f-93ce-a33beb534089',
							passwordVariable: 'MAVENPASSWORD', usernameVariable: 'MAVENUSER')]) {
						sh('curl -u $MAVENUSER:$MAVENPASSWORD https://registry.abas.sh/repository/abas.esdk.releases/de/abas/esdk/installer/0.7.1/installer-0.7.1.zip > installer.zip')
					}
					shDocker("cp installer.zip erp:/abas/erp1")
					shDocker("cp build/libs/*-standalone-app.jar erp:/abas/erp1/importitnew-standalone-app.jar")
					shDocker("exec --user root -t erp unzip -o /abas/erp1/installer.zip -d /abas/erp1")
					shDocker("exec --user root -t erp chown -R s3 /abas/erp1/esdk-installer-0.7.1")
					shDocker("exec -t erp sh -c 'cd /abas/erp1 && eval \$(sh denv.sh) && cd /abas/erp1/esdk-installer-0.7.1/bin && ./esdk-installer -a /abas/erp1/importitnew-standalone-app.jar -p sy -s'")
					shDocker("cp erp:/abas/erp1/esdk-installations/porti/$version/installation.log installation.log")
				}
				onMaster {
					stage('Publish') {
						shGradle("publish")
						stash name: 'gradleProps', includes: 'gradle.properties'
						stash name: 'build', includes: 'build/**/*'
					}
				}
				currentBuild.description = currentBuild.description + " => successful"
			} catch (any) {
				any.printStackTrace()
				currentBuild.result = 'FAILURE'
				currentBuild.description = currentBuild.description + " => failed"
				throw any
			} finally {
				shDockerComposeCleanUp()

				junit allowEmptyResults: true, testResults: 'build/test-results/**/*.xml'
				archiveArtifacts 'build/reports/**'
				archiveArtifacts 'installation.log'

				slackNotify(currentBuild.result)
			}

		}

	}
}


