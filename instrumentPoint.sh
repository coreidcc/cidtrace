#!/bin/sh

(
  "java" -Djava.util.logging.config.file=src/main/resources/jul.properties \
         -classpath "target/*:target/dependency/*" \
         cc.coreid.tracer.Main
)
	
