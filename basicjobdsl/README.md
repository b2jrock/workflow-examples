# Synopsis
Jenkins jobs DSL and Jenkins Workflow jobs both have their own, but different groovy DSL to define a jenkins job.
This example shows how to get up and running with creating your workflow jobs with jobs-dsl. Given just this example,
you'd still have to create the seed job by hand. It's possible to capture that step in code as well with
jenkins groovy boot hooks, but that's beyond the scope of this example.

# Background
Why would you want to do this? Workflow doesn't yet control certain phases of the job, such as scm polling.
This method allows you to keep all job definitions captured in code, and easily update many jobs, which is a strength
of jobs-dsl, while still benefiting from the new features that workflow has to offer.

# Layout
Your git directory would contain both the job-dsl plugin seed and the workflow seeds. There seems to be a way
to specify a git location instead that would allow you to put the workflow code in a separate repo now from 
the workflow side, but the job-dsl plugin hasn't caught up yet, at least in documentation.

myjob-seed.groovy - this is the job dsl code for creating some workflow jobs
myworkflow1.groovy - simple workflow example
myworkflow2.groovy - Another workflow example to show how multiple workflows appear in the seed groovy. 
                     Added bonus: it shows some more complex shell steps and non-standard docker usage in
                     contrast to the workflow tutorial.

# Extra
You'll notice me favoring shell commands over the workflow.inside('image') syntax. This is because I am
very familiar with docker, and just getting familiar with workflow. I'm more comfortable with overriding
docker command and entrypoint as necessary, and my examples reflect this philosophy. It does seem that
workflow lacks the flexibility to override various docker bits to achieve the desired outcome.
The myworkflow2 shows my attempts to overcome this (perceived at least) flexibility gap.
