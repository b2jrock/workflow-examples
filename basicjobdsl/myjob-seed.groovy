workflowJob("myworkflow1") {

    // run the seed on master
    label("master")

    // use timestamps to show timings
    wrappers{
        timestamps()
    }
    // Poll SCM every 5 minutes
    triggers {
      scm('H/5 * * * *')
    }
    // start workflow def
    definition {
        cps {
            script(readFileFromWorkspace('myworkflow1.groovy'))
            // Optionally use groovy sandbox
            //sandbox()
        }
    }

}
workflowJob("myworkflow2") {

    label("master")

    wrappers{
        timestamps()
    }
    triggers {
      scm('H/5 * * * *')
    }
    definition {
        cps {
            script(readFileFromWorkspace('myworkflow2.groovy'))
            // Optionally use groovy sandbox
            //sandbox()
        }
    }

}
