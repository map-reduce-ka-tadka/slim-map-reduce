# Slim Map Reduce [![Build Status](https://travis-ci.org/map-reduce-ka-tadka/slim-map-reduce.svg?branch=master)](https://travis-ci.org/map-reduce-ka-tadka/slim-map-reduce)
  
A MapReduce framework with a streamlined API from first principles, running on AWS.

# Build from source
If you wish to build from source, run the Bash script *build.sh*. JAR will be available in *dist* folder.

# Deploying in AWS cluster
Your machine should have Java 8, Maven and awscli already installed. Make sure to set the default output type of awscli as JSON.

In the *binsh* directory, copy smr.config.template to smr.config. Provide the key-pair name, security group and the absolute path to slim-map-reduce.jar in smr.config.

From the project 
Optionally, export the location of slim-map-reduce/binsh/automate.sh in your PATH environment variable.
