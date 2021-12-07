#!/bin/sh 

(
  "java" -Djava.util.logging.config.file=src/main/resources/jul.properties \
         -cp target/classes \
         cc.coreid.tracer.Point
)
	
