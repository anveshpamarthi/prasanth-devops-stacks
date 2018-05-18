import org.apache.commons.lang.RandomStringUtils
pipeline
{
    agent any
	environment{
		def bktname="${env.s3bucketname}"
		def awsregname="${env.AWSRegionName}"
		def stkname="${env.StackName}"
		AWSParStackName=""
	}
    stages{
        stage('Verify AWS-S3-Bucket'){
            steps{
				sh '''
					echo "you provided bucket name as : $bktname"
					s3bucket=`aws s3 ls --profile=vang | grep -i $bktname | awk '{print $3}'`
					if [ $s3bucket == $bktname ]
					then
					echo "Bucket Exists"
					else
					echo "Invalid Bucket. Please use valid bucketname with the given access"
					exit 1
					fi
				'''
			}	
		}
        stage('AWS-S3-Upload'){
            steps{
                withAWS(credentials: "vang", region: "$awsregname"){
                    s3Upload bucket: "$bktname", file: 'ec2instance.yaml', path: 'AS07/ec2instance.yaml'
                    s3Upload bucket: "$bktname", file: 'NLB-Internal.yaml', path: 'AS07/NLB-Internal.yaml'
                    s3Upload bucket: "$bktname", file: 'Master-golden-v7.yaml', path: 'AS07/Master-golden-v7.yaml'
					s3Upload bucket: "$bktname", file: 'a.yaml', path: 'AS07/a.yaml'
					s3Upload bucket: "$bktname", file: 'b.yaml', path: 'AS07/b.yaml'
					s3Upload bucket: "$bktname", file: 'c.yaml', path: 'AS07/c.yaml'
				}	
            }
        }
        stage('Verify-AWS-CF-Templates'){
            steps{
                withAWS(credentials: "vang", region: "$awsregname"){
                    cfnValidate(file: 'ec2instance.yaml')
					cfnValidate(file: 'NLB-Internal.yaml')
					cfnValidate(file: 'Master-golden-v7.yaml')
				}	
            }
        }		
        stage('Deploy-AWS-CF-Templates'){
            steps{
                withAWS(credentials: "vang", region: "$awsregname"){
					cfnUpdate(stack: "$env.StackName", create: true, file: 'Master-golden-v7.yaml', timeoutInMinutes: 15)
				}	
            }
        }
        stage('AWS-CFTemplate-Outputs'){
            steps{
                withAWS(credentials: "vang", region: "$awsregname"){
                    script{
						def output = cfnDescribe("$env.StackName")
						println "http://$output.ArgusServicesWorkspaceApi"
						println "http://$output.ArgusServicesModelOrganizationApi"
						println "http://$output.ArgusServicesDataAccessRightsApi"
						println "http://$output.ArgusServicesProvisionDataStoreApi"
						println "http://$output.ArgusServicesWorkspaceReportingApi"
                    }
                }
            }
        }
        stage('AWS ParameterStore'){
            steps{
				script{
					withAWSParameterStore(credentialsId: 'vang', naming: 'basename', path: '/qa/argusServices', regionName: "us-west-2"){
						echo "$env.CFSTACKNAME"
						y="${env.CFSTACKNAME}"
						echo "y is :$y"
						if ( env.StackName == env.CFSTACKNAME ){
							println "Stackname from ParamterStore and Jenkins are same. No update is required"
						}
						else{
							println "Stackname from ParameterStore and Jenkins are not same. Update is required"
							sh '''
								aws ssm put-parameter --name /qa/argusServices/CFStackName --type "String" --value "$stkname"  --overwrite --profile=vang
								echo "Updated!!!"
								'''
						}				
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
