#!/bin/sh 

(
  java  -Djava.util.logging.config.file=src/main/resources/jul.properties \
        -javaagent:./target/Trace-1.0-SNAPSHOT.jar \
        -classpath "target/*:target/dependency/*" \
        cc.coreid.tracer.Point

)
	
