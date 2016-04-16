#!/bin/bash
# Authors: Deepen Mehta, Abhijeet Sharma, Afan Khan, Akshay Raje
# This script is meant to be called from automate.sh main script
bucket=$1
folder=$2
checkJobStatus() {
    comm=$(aws s3 ls s3://$bucket/$folder/_SUCCESS | wc -l)
    if [[ $comm -gt 0 ]];
    then
        return 1
    else
        return 0
    fi
}
echo "Waiting for Job to complete"
while checkJobStatus
do
sleep 5
done
echo "Job Complete"
