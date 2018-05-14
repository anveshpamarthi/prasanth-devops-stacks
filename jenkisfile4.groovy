import org.apache.commons.lang.RandomStringUtils
pipeline
{
    agent any
    stages{
        stage('AWS-S3-Upload'){
            steps{
                withAWS(credentials: "altuspoc", region: "us-east-2"){
                    s3Upload bucket: 'cfscripts-altus-as', file: 'awssqs.yaml', path: 'AS06/sqs.yaml'
                }
            }
        }
		stage('Validate-Run-CF-Templates'){
			steps{
				withAWS(credentials: "altuspoc", region: "us-east-2"){
					cfnValidate(file: 'Master2.yaml')
					cfnUpdate(stack: "$env.StackName", create: true, file: 'Master2.yaml', timeoutInMinutes: 5)
				}
			}
		}
    stages{
        stage('VerifyOutputs'){
            steps{
                withAWS(credentials: "altuspoc", region: "us-east-2"){
                                        script{
                     def output = cfnDescribe("$env.StackName")
                     println "It works! output is $output.SQSUrl"
                    }
                }
            }
        }		
    }
    post{
        always{
            echo "Deployment is run for $env.Environment"
        }
        failure{
            echo "Error occured"
        }
        success{
            echo "Succesfully deployed to $env.Environment"
        }
    }
}
