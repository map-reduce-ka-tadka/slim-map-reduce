#!/bin/bash
#
# Authors: Akshay Raje, Afan Khan, Deepen Mehta, Abhijeet Sharma
#
# Builds slim-map-reduce.jar and copy to lib folder.
#
# Pre-requisites:
# ===============
# Java 8 SE
# Maven     (https://maven.apache.org/install.html)
#
# Step 0:
# =======
# Run the following to make this script executable -> chmod +x build.sh
#
echo "====================================================================="
echo "Building slim-map-reduce.jar."
echo "====================================================================="
mkdir -p dist
mvn clean package 1> /dev/null && cp target/slim-map-reduce.jar dist/slim-map-reduce.jar
if [[ $? != 0 ]];
then
	echo "Error in building slim-map-reduce.jar. Existing slim-map-reduce.jar will remain untouched."
	exit 1
fi
echo "Latest build of slim-map-reduce.jar is available in dist folder."