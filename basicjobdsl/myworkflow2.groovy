import groovy.json.JsonOutput

// global scope on these since we're only doing one checkout.
def git_commit=''
def short_commit=''
def git_url=''

stage 'build-package'
node('master') {

    checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'src'], [$class: 'SubmoduleOption', disableSubmodules: false, recursiveSubmodules: true, trackingSubmodules: false]], submoduleCfg: [], userRemoteConfigs: [[url: 'ssh://git@some-atlassian-stash/thing2/thing2.git']]])
    // I just like having my workspace. Not _exactly_ the same, but close enough.
    workspace = pwd()
    sh('cd src && git rev-parse HEAD > GIT_COMMIT')
    git_commit=readFile('src/GIT_COMMIT')

    sh('cd src && git config --get remote.origin.url > GIT_URL')
    git_url=readFile('src/GIT_URL')
    // short SHA, possibly better for chat notifications, etc. 
    short_commit=git_commit.take(6)
    def packageShell = """\
      |echo "multiline script that you would use to execute your local build/package procedure"
      |rm -rf ${workspace}/build
      |mkdir -p ${workspace}/build/opt/thing2
      |mkdir -p ${workspace}/dist
      |cd ${workspace}/src
      |ls -l ${workspace}
      |echo "You might for example.. execute the venerable fpm tool, immortalized as a docker container"
      |docker run -v ${workspace}:${workspace} docker.mycorp.com:5000/fpm -s dir -t rpm -n thing2 --url ${git_url} \\
      |     --description "Commit: ${git_commit}" -v 1.0 --iteration ${env.BUILD_NUMBER} \\
      |     -p ${workspace}/dist -C ${workspace}/build opt/thing2
      |ls -l ${workspace}/dist
      |echo "or you can hijack that same container for it's chown abilities, since you already have it handy"
      |docker run -v ${workspace}:${workspace} --entrypoint "/bin/chown" docker.mycorp.com:5000/fpm -R 1000:1000 ${workspace}/dist
      """.stripIndent().stripMargin()
    sh "${packageShell}"
    step([$class: 'ArtifactArchiver', artifacts: 'dist/*.rpm', fingerprint: true])
}


stage 'publish-qa'
node('master') {
    unarchive mapping: ['dist/*.rpm': '.']
    // do some things to get the rpm into a qa facing yum repo, for example
    notifySlack("thing2 published to qa ${short_commit} ${env.BUILD_URL}", "#thing2-development")
}
// here you might have several QA environments you optionally want to deploy to.
// If you have a single qa with very good automated QA, you could capture that here.
// it would be very similar to staging deploy below.

// manual promotion, in a way
stage 'promote-to-stage'
input 'Continue to staging?'

stage 'publish-to-staging'
node('master') {
    unarchive mapping: ['dist/*.rpm': '.']
    // get package on staging repo by whatever means makes sense
}

stage 'deploy-to-staging'
node('master') {
    notifySlack("thing2 published to staging ${short_commit} ${env.BUILD_URL}", "#thing2-development")
}

// promotion again
stage 'promote-to-prod'
input 'Continue to prod?'

stage 'publish-to-prod'
node('master') {
    // do things to make package available in prod facing yum repo if necessary.
}

stage 'deploy-to-prod'
node('master') {
    // install package on prod nodes, notify monitoring, etc.
    notifySlack("thing2 published to staging ${short_commit} ${env.BUILD_URL}", "#thing2-operations")
}



def notifySlack(text, channel) {
    def slackURL = 'https://hooks.slack.com/services/xxxxxx/yyyyyyy/zzzzzz'
    def payload = JsonOutput.toJson([text      : text,
                                     channel   : channel,
                                     username  : "jenkins",
                                     icon_emoji: ":jenkins:"])
    sh "curl -X POST --data-urlencode \'payload=${payload}\' ${slackURL}"
}

