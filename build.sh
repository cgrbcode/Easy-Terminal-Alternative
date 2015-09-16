#!/bin/bash -l
set -ex 

JAVA='/local/cluster/ETA/jdk1.7.0_01/bin/'
LIBS=`ls -1 lib | grep jar | tr "\n" ":" | sed 's/:/:lib\//g'`
mkdir -p etastart  etamonitor etautil etasubmit war/WEB-INF/classes
${JAVA}javac -cp src:lib/$LIBS -d  etastart/ -Xlint:unchecked -sourcepath src src/cgrb/eta/server/remote/etastart/ETAStart.java
${JAVA}javac -cp src:lib/$LIBS -d  etamonitor/ -Xlint:unchecked -sourcepath src src/cgrb/eta/server/remote/etamonitor/Monitor.java
${JAVA}javac -cp src:lib/$LIBS -d  etautil/ -Xlint:unchecked -sourcepath src src/cgrb/eta/server/remote/etautil/RemoteETAUtil.java
${JAVA}javac -cp src:lib/$LIBS -d  etasubmit/ -Xlint:unchecked -sourcepath src src/cgrb/eta/server/remote/etasubmit/ETASubmit.java
${JAVA}javac -cp src:lib/$LIBS -d war/WEB-INF/classes -Xlint:unchecked -sourcepath src src/cgrb/eta/server/*.java src/cgrb/eta/server/services/*.java
unzip lib/drmaa.jar -d etastart
cd etastart
echo "Main-Class: cgrb.eta.server.remote.etastart.ETAStart" > mainClass
${JAVA}jar cmf  mainClass ../ETAStart.jar cgrb META-INF org com
cd ..
cd etamonitor
echo "Main-Class: cgrb.eta.server.remote.etamonitor.Monitor" > mainClass
${JAVA}jar cmf  mainClass ../ETAMonitor.jar cgrb
cd ..
cd etautil
echo "Main-Class: cgrb.eta.server.remote.etautil.RemoteETAUtil" > mainClass
${JAVA}jar cmf  mainClass ../ETAUtil.jar cgrb
cd ..
cd etasubmit
echo "Main-Class: cgrb.eta.server.remote.etautil.RemoteETAUtil" > mainClass
${JAVA}jar cmf  mainClass ../ETASubmit.jar cgrb
cd ..
rm -rf etastart  etamonitor etautil etasubmit
#ps aux | grep ETAS | awk '{print $2}'  | xargs -i++ kill -9 ++
#cp *.jar /local/ETA/.
${JAVA}java -Xmx512m -cp src:lib/$LIBS com.google.gwt.dev.Compiler -logLevel INFO -style OBFUSCATED -localWorkers 3 -war war cgrb.eta.eta
cd war
rm -f war.tar.gz settings query.jar *.html *.jar WEB-INF/settings
#tar -zcf war.tar.gz --exclude='.svn' *
#mv war.tar.gz /var/www/html/current.tar.gz
cd ..
