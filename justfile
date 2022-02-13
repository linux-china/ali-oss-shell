#!/usr/bin/env just --justfile

build:
   mvn -DskipTests package

shell: build
   java -jar target/ali-oss-java-cli-1.0.0.jar