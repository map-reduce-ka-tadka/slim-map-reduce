#!/bin/bash
# Authors: Deepen Mehta, Abhijeet Sharma, Afan Khan, Akshay Raje
# Start/stop EC2 instances to run Distributed Sort
# Requires the aws package locally -- sudo apt-get install awscli
# Requires Java and Maven installed
#
# usage: ./automate.sh start {No. of slaves} (spin up EC2 instances, install required software, execute job and download output)
#        ./automate.sh stop (terminate all EC2 instances to save money)
#        ./automate.sh sortData (start Distributed Sort remotely and let the nodes coordinate)

# CHANGE THE PARAMETERS BELOW

imageid="ami-fce3c696" # this is an Ubuntu AMI, but you can change it to whatever you want
master_instance_type="t2.large"
node_instance_type="m4.xlarge"
key_name="awsec2" # your keypair name -- http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-key-pairs.html
security_group="mrlite" # your security group -- http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-network-security.html
key_location="/home/amraje/.aws/awsec2.pem" # your private key -- http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-key-pairs.html#having-ec2-create-your-key-pair
user="ubuntu" # the EC2 linux user name
secretfolder="hideme"
jarName="DistributedSort.jar"
port="9090"
inputBucketName="cs6240sp16"
inputFolder="climate"
outputBucketName="a9try"
outputFolder="output"
count=$2 # Number of slave nodes excluding master

build ()
{
	mvn clean package
	cp target/$jarName .
}

start () 
{
	echo "Cleaning old files and temp directories..."
	rm -rf *.txt
	rm -rf tempfolder
	rm -rf output
	rm -rf $jarName
	rm -rf finish
	rm -rf Assignment9_Report.pdf
	echo "Building project..."
	build	
	echo "Starting master EC2 instance..."
	aws ec2 run-instances --image-id $imageid --count 1 --instance-type $master_instance_type --key-name $key_name --security-groups $security_group >> /dev/null
	sleep 50
	getmasterip
}

getmasterip ()
{
	echo "Getting master IP Address... "
	masterPrivateip=$(aws ec2 describe-instances | grep PrivateIpAddress | grep -E -o "([0-9]{1,3}[\.]){3}[0-9]{1,3}" | uniq)
	masterPublicip=$(aws ec2 describe-instances | grep PublicIpAddress | grep -E -o "([0-9]{1,3}[\.]){3}[0-9]{1,3}" | uniq)
	echo $masterPrivateip >> masterPrivateAddress.txt
	echo $masterPublicip >> masterPublicAddress.txt
	startNodes
}

startNodes ()
{
	echo "Starting slave EC2 instances..."
	aws ec2 run-instances --image-id $imageid --count $count --instance-type $node_instance_type --key-name $key_name --security-groups $security_group >> /dev/null
	sleep 60
	getip
	sortData
}

getip ()
{
	echo "Getting IP Addresses of nodes... "
	mkdir -p tempfolder
	cp -r ~/.aws/* tempfolder
	ip=$(aws ec2 describe-instances | grep PublicIpAddress | grep -E -o "([0-9]{1,3}[\.]){3}[0-9]{1,3}")
	for name in ${ip[@]}; do
		echo $name >> temp.txt
		ssh -i $key_location -o StrictHostKeyChecking=no $user"@"$name "sudo apt-get -y update; sudo apt-get -y install awscli; sudo apt-get -y install python-software-properties debconf-utils; sudo add-apt-repository -y ppa:webupd8team/java; sudo apt-get -y update; echo debconf shared/accepted-oracle-license-v1-1 select true | sudo debconf-set-selections; echo debconf shared/accepted-oracle-license-v1-1 seen true | sudo debconf-set-selections; sudo apt-get -y install oracle-java8-installer; sudo mkdir -p "$secretfolder
		scp -i $key_location $key_location $user"@"$name":/home/"$user"/" && ssh -i $key_location -o StrictHostKeyChecking=no $user"@"$name "sudo mkdir -p ."$secretfolder"; sudo mv "$key_name".pem ."$secretfolder"/" 
		scp -i $key_location -r tempfolder $user"@"$name":/home/"$user"/" && ssh -i $key_location -o StrictHostKeyChecking=no $user"@"$name "sudo mv tempfolder .aws" 
		scp -i $key_location $jarName $user"@"$name":/home/"$user"/" 
	done
	rm -rf tempfolder

	grep -vf masterPublicAddress.txt temp.txt > dataNodesAddresses.txt
	rm -rf temp.txt

	for name in ${ip[@]}; do
		scp -i $key_location dataNodesAddresses.txt $user"@"$name":/home/"$user"/"
		scp -i $key_location masterPrivateAddress.txt $user"@"$name":/home/"$user"/"
		scp -i $key_location masterPublicAddress.txt $user"@"$name":/home/"$user"/"
	done
}

sortData ()
{
	masterip=`cat masterPublicAddress.txt`
	masterprivateip=`cat masterPrivateAddress.txt`
	echo "Running program on master "$masterip"..."
		ssh -i $key_location -o StrictHostKeyChecking=no $user"@"$masterip "nohup java -Xms2g -Xmx5g -jar "$jarName" server "$masterprivateip" "$port" "$count" "$inputBucketName" "$inputFolder" "$outputBucketName" "$outputFolder" 1> success.out 2> error.err < /dev/null &"
	clientList=`cat dataNodesAddresses.txt`
	for name in ${clientList[@]}; do
		echo "Running program on node "$name"..."
		ssh -i $key_location -o StrictHostKeyChecking=no $user"@"$name "nohup java -Xms5g -Xmx10g -jar "$jarName" client "$masterip" "$port" "$count" "$inputBucketName" "$inputFolder" "$outputBucketName" "$outputFolder" 1> success.out 2> error.err < /dev/null &"
	done
	time bash checkJobSuccess.sh $outputBucketName $outputFolder
	echo "Downloading output folder from S3..."
	aws s3 sync s3://${outputBucketName}/${outputFolder} output
	echo "Generating report..."
	Rscript -e "rmarkdown::render('Assignment9_Report.Rmd')"
}

rebuild ()
{
	build
	clientList=`cat dataNodesAddresses.txt`
	echo "$clientList"
	for name in ${clientList[@]}; do
		scp -i $key_location $jarName $user"@"$name":/home/"$user"/"
	done
	masterip=`cat masterPublicAddress.txt`
	echo "$masterip"
	scp -i $key_location $jarName $user"@"$masterip":/home/"$user"/"
}

stop ()
{
	instance=$(aws ec2 describe-instances | grep InstanceId | grep -E -o "i\-[0-9A-Za-z]+")
	for name in ${instance[@]}; do
		aws ec2 terminate-instances --instance-ids $instance >> /dev/null
	done
}

# "main"
case "$1" in
	start)
		start
		;;
	sortData)
		sortData
		;;
	stop)
		stop
		;;
	rebuild)
		rebuild
		;;
	build)
		build
		;;
esac
