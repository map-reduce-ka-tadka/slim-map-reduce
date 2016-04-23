# Slim Map Reduce [![Build Status](https://travis-ci.org/map-reduce-ka-tadka/slim-map-reduce.svg?branch=master)](https://travis-ci.org/map-reduce-ka-tadka/slim-map-reduce)
  
A MapReduce framework with a streamlined API from first principles, running on AWS.

# Parallel data processing with Slim MapReduce API
For the purpose of this example, we will assume that you have cloned this repository in your home folder. **(~/slim-map-reduce)**

## Build from source
Navigate to slim-map-reduce folder and run the Bash script *build.sh*. slim-map-reduce.jar will be available in *dist* directory.

## Configuring your slim-map-reduce installation
**Note:** Your machine should have Java 8, Maven and awscli already installed. Make sure to set the default output type of awscli as JSON.

Navigate to slim-map-reduce folder and in the *binsh* directory, copy *smr.config.template* to *smr.config*. Provide the key-pair name, security group and the absolute path to *slim-map-reduce.jar* in *smr.config*.

## Using slim-map-reduce.jar in your projects
Copy *slim-map-reduce.jar* from *dist* folder in to your project folder and add it to your classpath.

## Deploying your program in AWS cluster
Make sure that you have followed the previous steps before deploying in AWS cluster. Let us assume that you are working in a project folder named WordCount and you have added *slim-map-reduce.jar* in the classpath of your project to use the MapReduce API. To start an AWS cluster with 1 master and 4 slaves, give the following command from your project folder -

~/slim-map-reduce/binsh/automate.sh start 4

Note that in the above command, you have to give a relative or absolute path to automate.sh script. Optionally, export the location of slim-map-reduce/binsh/automate.sh in your PATH environment variable, so that you can reference it directly from any folder.

The above command will start 1 master and 4 slave EC2 instances which are waiting for a user program. Package your project (WordCount) which uses Slim Map Reduce API as a JAR and deploy it as follows -

~/slim-map-reduce/binsh/automate.sh deploy WordCount.jar s3n://mybucket/input s3n://mybucket/output

Here we have assumed that your input and output folder paths are passed as command line arguments. Wait for the program to finish and check the output bucket for _SUCCESS file.

## Terminating the cluster
Use the following command -
~/slim-map-reduce/binsh/automate.sh stop
