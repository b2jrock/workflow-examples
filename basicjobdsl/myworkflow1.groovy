import groovy.json.JsonOutput

// global scope on these since we're only doing one checkout.
def git_commit=''
def short_commit=''
def git_url=''

stage 'build-package'
node('master') {

    checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'src'], [$class: 'SubmoduleOption', disableSubmodules: false, recursiveSubmodules: true, trackingSubmodules: false]], submoduleCfg: [], userRemoteConfigs: [[url: 'ssh://git@some-atlassian-stash/thing1/thing1.git']]])
    // I just like having my workspace. Not _exactly_ the same, but close enough.
    workspace = pwd()
    sh('cd src && git rev-parse HEAD > GIT_COMMIT')
    git_commit=readFile('src/GIT_COMMIT')

    sh('cd src && git config --get remote.origin.url > GIT_URL')
    git_url=readFile('src/GIT_URL')
    // short SHA, possibly better for chat notifications, etc.
    short_commit=git_commit.take(6)
    def packageShell = """\
     |echo "Maybe do something more real to create a package."
     |touch dist/*.rpm
     """.stripIndent().stripMargin()

    sh "${packageShell}"
    step([$class: 'ArtifactArchiver', artifacts: 'dist/*.rpm', fingerprint: true])
}


stage 'publish-qa'
node('master') {
    unarchive mapping: ['dist/*.rpm': '.']
    // do some things to get the rpm into a qa facing yum repo, for example
    notifySlack("thing1 published to qa ${short_commit} ${env.BUILD_URL}", "#thing1-development")
}

stage 'deploy-to-qa'
node('master') {
    // call your favorite deploy tool... or just ssh in and yum clean all && yum install thing1
    notifySlack("thing1 published to staging ${short_commit} ${env.BUILD_URL}", "#thing1-development")
}

