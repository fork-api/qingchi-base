#! /bin/sh
# 1. kill,防止改端口   
sh shutdown.sh 8080
# 2. pull
git pull origin master
# 3. install
mvn install
# 4. run
cd great-tomcat
pwd
# mvn clean compile
# mvn exec:java -Dexec.mainClass="com.fucking.great.tomcat.TomcatStarter"
nohup mvn exec:java -Dexec.mainClass="com.fucking.great.tomcat.TomcatStarter" &
tail -F logs/rolling.log