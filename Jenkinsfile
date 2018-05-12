pipeline{
agent any 
  stages{
    stage('Build')
    {
      steps
      {
        println "Building Appliaction for ${env.Environment}"
        script{
        if ( env.Environment == 'Dev' )
        { println "Environment is Dev . Deploying to Ohio"}
        else
        { println "Environment is QA/UAT. Depl to Tokyo"}
        }
      }
    }
    stage('Deploy')
    {
      steps
      {
        println "Deploying Appliaction for${env.Environment}"
      }
    }
  }
}
