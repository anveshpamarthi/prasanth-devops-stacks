pipeline
{
    agent any
    stages{
        stage('Hello'){
            steps{
                script{
                    def name="Prasanth"
                    echo "Hello from pipeline"
                    echo "welcome to jenkins..$name"
                    echo "Environment is $env.Env"
                }
            }
        }
        stage('Build'){
            steps{
                script{
                    sleep 1.5
                    echo "Build $env.BuildName is completed"
                }
            }
        }
        stage('PushToTestEnv'){
            steps{
                build job: 'pushtotest', propagate: true, wait: true,parameters: [[$class: 'StringParameterValue', name: 'testval', value: "$env.BuildName"]]
            }
        }
        
    }
    post{
        always{
            echo "$env.JOB_NAME is run and results will be emailed"
            echo "Please rerun manually as $env.JOB_URL"
        }
        
    }

}
