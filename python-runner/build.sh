#!/bin/bash
# Remove old files
rm -fr ./{filters,transforms}
# Copy fresh files
cp -R ../{filters,transforms} .

docker build -t datacater/python-runner:alpha-20221101 .
