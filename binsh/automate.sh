#!/bin/bash
#
# Authors: Akshay Raje, Afan Khan, Deepen Mehta, Abhijeet Sharma
#
# Use this script to automatically create a cluster, run Slim MapReduce jobs and terminate the cluster.
#
# Pre-requisites:
# ===============
# awscli    (http://docs.aws.amazon.com/cli/latest/userguide/installing.html)
# Java 8 SE
# Maven     (https://maven.apache.org/install.html)
#
# Step 0:
# =======
# Run the following to make this script executable -> chmod +x automate.sh
#
# Usage:
# ======
#  ./automate.sh start {No. of slaves}                (spins up EC2 instances, installs required software and starts the master)
#  ./automate.sh stop                                 (terminate all EC2 instances to save money)
#  ./automate.sh deploy {Job JAR} {arguments for JAR} (run Slim MapReduce job in the cluster and wait for the result in output S3 bucket)

# Check for the existence of smr.config file
current_dir=$(dirname "$BASH_SOURCE")
if [ ! -f "${current_dir}/smr.config" ];
then
   echo "Config file ${current_dir}/smr.config does not exist." >&2
   exit 1 # Exit the script with an error status
fi

# Read in all the parameters from the config file
source ${current_dir}/smr.config

# Perform validation of necessary config parameters
if [ -z "$SLIM_MAP_REDUCE_CLASSPATH" ];
then
    echo "Absolute path to slim-map-reduce.jar is not set in smr.config." >&2
    exit 1 # Exit the script with an error status
fi
if [ -z "$key_location" ];
then
    echo "Absolute path to key-pair is not set in smr.config." >&2
    exit 1 # Exit the script with an error status
fi
if [ -z "$security_group" ];
then
    echo "EC2 security group is not set in smr.config." >&2
    exit 1 # Exit the script with an error status
fi
if [ -z "$image_id" ];
then
    echo "Image Id is required in smr.config to spin up ec2 instances." >&2
    exit 1 # Exit the script with an error status
fi
if [ -z "$user" ];
then
    echo "Default EC2 user name must be set in smr.config." >&2
    exit 1 # Exit the script with an error status
fi
if [ -z "$secret_folder" ];
then
    echo "Secret hidden folder name for storing key-pairs on EC2 must be specified in smr.config." >&2
    exit 1 # Exit the script with an error status
fi
if [ -z "$master_instance_type" ];
then
    echo "Specify an EC2 instance type for master node in smr.config." >&2
    exit 1 # Exit the script with an error status
fi
if [ -z "$node_instance_type" ];
then
    echo "Specify an EC2 instance type for slave nodes in smr.config." >&2
    exit 1 # Exit the script with an error status
fi
if [ -z "$port" ];
then
    echo "Default port number for Socket communication not specified in smr.config." >&2
    exit 1 # Exit the script with an error status
fi

# Retrieve the name of .pem file from the given absolute path
key_name_with_extension=$(basename "$key_location")
# Discard the .pem extension
key_name="${key_name_with_extension%.*}"

cleanup ()
{
    echo "Cleaning up..."
    rm -rf _work
	rm -rf tempfolder
}

stop ()
{
	instance=$(aws ec2 describe-instances | grep InstanceId | grep -E -o "i\-[0-9A-Za-z]+")
	echo "====================================================================="
	echo "Terminating all running EC2 instances."
	echo "====================================================================="
	for name in ${instance[@]}; do
		aws ec2 terminate-instances --instance-ids $instance >> /dev/null
	done
    cleanup
}

start_slaves ()
{
    # First argument to this function is the number of slave nodes to spawn
    local count=$1
	echo "Starting ${count} slave EC2 instance(s)..."
	aws ec2 run-instances --image-id ${image_id} --count ${count} --instance-type ${node_instance_type} --key-name ${key_name} --security-groups ${security_group} >> /dev/null
	if [[ $? != 0 ]];
	then
		echo "Failed to start slave EC2 instances. Abandoning cluster initialization."
	    echo "====================================================================================================="
	    echo "There may be stray EC2 instances in running state. Check AWS EC2 console and terminate them manually."
	    echo "====================================================================================================="
	    cleanup
		exit 1
	fi
	# Allow ample time for the EC2 instances to spawn
	sleep 60
}

start_master ()
{
    echo "Starting master EC2 instance..."
	aws ec2 run-instances --image-id ${image_id} --count 1 --instance-type ${master_instance_type} --key-name ${key_name} --security-groups ${security_group} >> /dev/null
	if [[ $? != 0 ]];
	then
		echo "Failed to start master EC2 instance. Abandoning cluster initialization."
		echo "====================================================================================================="
	    echo "There may be stray EC2 instances in running state. Check AWS EC2 console and terminate them manually."
	    echo "====================================================================================================="
	    cleanup
		exit 1
	fi
	# Allow ample time for the EC2 node to start up
	sleep 50
}

save_master_ip ()
{
	echo "Getting private and public IP Addresses of the master... "
	local master_private_ip=$(aws ec2 describe-instances | grep PrivateIpAddress | grep -E -o "([0-9]{1,3}[\.]){3}[0-9]{1,3}" | uniq)
	local master_public_ip=$(aws ec2 describe-instances | grep PublicIpAddress | grep -E -o "([0-9]{1,3}[\.]){3}[0-9]{1,3}" | uniq)
	echo ${master_private_ip} > _work/masterPrivateAddress.txt
	echo ${master_public_ip} > _work/masterPublicAddress.txt
	local n1=$(wc -w < _work/masterPrivateAddress.txt)
	local n2=$(wc -w < _work/masterPublicAddress.txt)
	if [[ ${n1} == 0 || ${n2} == 0 ]];
	then
	    echo "IP Address of the master could not be retrieved. Abandoning cluster initialization."
	    echo "====================================================================================================="
	    echo "There may be stray EC2 instances in running state. Check AWS EC2 console and terminate them manually."
	    echo "====================================================================================================="
	    cleanup
	    exit 1
	fi
}

install_software ()
{
    # Copy contents of .aws folder to another directory for copying to EC2
    mkdir -p tempfolder
	cp -r ~/.aws/* tempfolder

    echo "===================================================================================="
	echo "Installing pre-requisite software on all EC2 instances (this might take a while)... "
	echo "===================================================================================="
	local ip=$(aws ec2 describe-instances | grep PublicIpAddress | grep -E -o "([0-9]{1,3}[\.]){3}[0-9]{1,3}")
	for name in ${ip[@]}; do
		echo ${name} >> _work/temp.txt
		ssh -i ${key_location} -o StrictHostKeyChecking=no ${user}"@"${name} "sudo add-apt-repository -y ppa:webupd8team/java; sudo apt-get -y update; echo debconf shared/accepted-oracle-license-v1-1 select true | sudo debconf-set-selections; echo debconf shared/accepted-oracle-license-v1-1 seen true | sudo debconf-set-selections; sudo apt-get -y install oracle-java8-installer"
		if [[ $? != 0 ]];
		then
		    echo "Error in opening SSH session with an EC2 instance. Abandoning software installation."
		    stop
		    exit 1
		fi
	    scp -i ${key_location} -o StrictHostKeyChecking=no ${key_location} ${user}"@"${name}":/home/${user}/" 1> /dev/null && ssh -i ${key_location} -o StrictHostKeyChecking=no ${user}"@"${name} "sudo mkdir -p .${secret_folder}; sudo mv ${key_name_with_extension} .${secret_folder}/"
	    scp -i ${key_location} -o StrictHostKeyChecking=no -r tempfolder ${user}"@"${name}":/home/${user}/" 1> /dev/null && ssh -i ${key_location} -o StrictHostKeyChecking=no ${user}"@"${name} "sudo chmod 777 tempfolder; sudo mv tempfolder .aws; sudo rm -rf tempfolder"
	done

	# Erase the tempfolder
	rm -rf tempfolder

    # Exclude master's public address from the list to get all slave public addresses
	grep -vf _work/masterPublicAddress.txt _work/temp.txt > _work/slavePublicAddresses.txt
	rm -rf _work/temp.txt

    # Append the configured port number to master's public address for use in running job JAR
	local master_ip=$(cat _work/masterPublicAddress.txt)
	cp _work/masterPublicAddress.txt _work/masterPublicAddressOnly.txt
	rm -rf _work/masterPublicAddress.txt
	echo ${master_ip}:${port} > _work/masterPublicAddress.txt

    # Notify master's public address and configured port to all the slave nodes and also check if Java 8 was installed
	for name in ${ip[@]}; do
	    ssh -i ${key_location} -o StrictHostKeyChecking=no ${user}"@"${name} "which java" >> _work/installation.txt
	    if [[ $? != 0 ]];
		then
		    echo "Java did not install on all machines. Cluster initialization failed."
		    stop
		    exit 1
		fi
		scp -i ${key_location} -o StrictHostKeyChecking=no _work/masterPublicAddress.txt ${user}"@"${name}":/home/${user}/" 1> /dev/null
	done
}

check_job_status () {
    # Use check_job_status to determine if _SUCCESS file has been generated in S3 bucket
    local buc=$1
    local fol=$2
    local comm=$(aws s3 ls s3://$buc/$fol/_SUCCESS | wc -l)
    if [[ $comm -gt 0 ]];
    then
        return 1 # Indicate failure to find _SUCCESS file
    else
        return 0 # Indicate success in finding _SUCCESS file
    fi
}

wait_for_job_completion () {
    echo "Waiting for job to complete (this might take a while) ..."
    # wait_for_job_completion expects S3 bucket name and folder name
    local bucket=$1
    local folder=$2
    while check_job_status $bucket $folder ;
    do
        sleep 5 # Try again after 5 seconds
    done
    echo "Job completed successfully."
}

start () 
{
    # First argument to this function is the number of slave nodes to spawn
    local count=$1      # Number of slave nodes excluding master
    if [ -z "$count" ];
    then
        echo "Number of slave instances is not mentioned." >&2
        exit 1 # Exit the script with an error status
    fi
    echo "==============================================================="
	echo "Initializing a cluster with 1 master and ${count} EC2 machines."
	echo "==============================================================="
	echo " "
	echo "Cleaning old files and temp directories..."
	rm -rf _work
	rm -rf tempfolder
	rm -rf output
	rm -rf _SUCCESS
	mkdir -p _work
	start_master
	save_master_ip
	start_slaves $count
	install_software
	echo "============================================================="
	echo "Cluster is ready to accept new jobs."
	echo "============================================================="
}

deploy ()
{
	echo "============================================================================================"
	echo "Deploying user job in the cluster..."
	echo "============================================================================================"
    local jar_name=$1
    local other_params=${@:2}
	local master_ip=$(cat _work/masterPublicAddressOnly.txt)
	local master_private_ip=$(cat _work/masterPrivateAddress.txt)
	local client_list=$(cat _work/slavePublicAddresses.txt)
	local no_of_slaves=$(wc -l < _work/slavePublicAddresses.txt)

	echo "Starting resource manager on master "${master_ip}"..."
	ssh -i ${key_location} -o StrictHostKeyChecking=no ${user}"@"${master_ip} "rm -rf success.out; rm -rf error.err"
	if [[ $? != 0 ]];
	then
	    echo "Could not connect to master. Abandoning deployment."
	    exit 1
	fi
	scp -i ${key_location} -o StrictHostKeyChecking=no ${SLIM_MAP_REDUCE_CLASSPATH} "${user}@${master_ip}:/home/${user}/"
	if [[ $? != 0 ]];
	then
	    echo "Could not deploy to master. Abandoning deployment."
	    exit 1
	fi
	ssh -i ${key_location} -o StrictHostKeyChecking=no ${user}"@"${master_ip} "nohup java -Xms2g -Xmx5g -jar slim-map-reduce.jar ${master_private_ip} ${port} ${no_of_slaves} 1> success.out 2> error.err < /dev/null &"
    if [[ $? != 0 ]];
	then
	    echo "Could not start resource manager on master. Abandoning deployment."
	    exit 1
	fi

	for name in ${client_list[@]}; do
		echo "Starting job on node "${name}"..."
        ssh -i ${key_location} -o StrictHostKeyChecking=no ${user}"@"${name} "rm -rf success.out; rm -rf error.err"
		if [[ $? != 0 ]];
	    then
	        echo "Could not connect to slave instance. Abandoning deployment."
	        echo "================================================================================================================================"
	        echo "User program might still be running at master. Terminate cluster and deploy again."
	        echo "================================================================================================================================"
	        exit 1
	    fi
	    scp -i ${key_location} -o StrictHostKeyChecking=no ${jar_name} ${user}"@"${name}":/home/${user}/"
	    if [[ $? != 0 ]];
	    then
	        echo "Could not deploy to slave instance. Abandoning deployment."
	        echo "================================================================================================================================"
	        echo "User program might still be running at master. Terminate cluster and deploy again."
	        echo "================================================================================================================================"
	        exit 1
	    fi
	    ssh -i ${key_location} -o StrictHostKeyChecking=no ${user}"@"${name} "nohup java -Xms5g -Xmx10g -jar ${jar_name} ${other_params} 1> success.out 2> error.err < /dev/null &"
	    if [[ $? != 0 ]];
	    then
	        echo "Could not start job on slave instance. Abandoning deployment."
	        echo "================================================================================================================================"
	        echo "User program might still be running at master and other slave EC2 instances. Terminate cluster and deploy again."
	        echo "================================================================================================================================"
	        exit 1
	    fi
	done

	echo "========================================================================================================="
	echo "User job is running in the cluster. Check for _SUCCESS or _ERROR file in the output folder after a while."
	echo "========================================================================================================="
}

# Starting point for the Bash script
case "$1" in
	start)
		start $2
		;;
	deploy)
		deploy ${@:2}
		;;
	stop)
		stop
		;;
esac
