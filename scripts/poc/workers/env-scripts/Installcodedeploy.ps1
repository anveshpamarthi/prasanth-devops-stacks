param([string]$awsregion = "ohio")
New-Item -Path c:\temp -ItemType "direc$yy=tory" -Force
$S3Location="aws-codedeploy-$awsregion"
powershell.exe -Command Read-S3Object -BucketName $S3Location/latest -Key codedeploy-agent.msi -File c:\temp\codedeploy-agent.msi
Start-Process -Wait -FilePath c:\temp\codedeploy-agent.msi -WindowStyle Hidden
