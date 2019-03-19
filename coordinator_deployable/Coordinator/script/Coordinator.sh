#!/bin/sh

HOME=`dirname $0`/..
HOME_JAR="${HOME}/target/Coordinator.jar"


JVM_OPTS="-Xms512m -Xmx2g"
## If you want JaCoCo Code Coverage reports... uncomment line below
#JVM_OPTS="$JVM_OPTS -javaagent:${HOME}/lib/jacocoagent.jar=dumponexit=true,output=file,destfile=${HOME}/target/jacoco.exec"
JVM_OPTS="$JVM_OPTS -XX:+TieredCompilation"
JVM_OPTS="$JVM_OPTS -XX:+UseCompressedOops"
JVM_OPTS="$JVM_OPTS -XX:+UseConcMarkSweepGC -XX:+AggressiveOpts -XX:+UseFastAccessorMethods"
JVM_OPTS="$JVM_OPTS -XX:MaxInlineSize=8192 -XX:FreqInlineSize=8192" 
JVM_OPTS="$JVM_OPTS -XX:CompileThreshold=1500" 

if [ ! -e ${HOME_JAR} ]; then
	echo "No Coordinator.jar"
fi

echo "Starting Coordinator..."
java ${JVM_OPTS} -Dlog4j.configurationFile=${HOME}/config/log4j2.xml -Djavax.net.ssl.keyStore=${HOME}/config/sslStore -jar ${HOME_JAR} $@
