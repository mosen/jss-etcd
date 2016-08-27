FROM tomcat:7.0.59-jre7

MAINTAINER Mosen <mosen@users.noreply.github.com>

RUN rm -rf /usr/local/tomcat/webapps/*
ADD ROOT.war /usr/local/tomcat/webapps/ROOT.war
WORKDIR /usr/local/tomcat/webapps
RUN /usr/bin/unzip /usr/local/tomcat/webapps/ROOT.war -d /usr/local/tomcat/webapps/ROOT
RUN rm /usr/local/tomcat/webapps/ROOT.war

RUN rm /usr/lib/jvm/java-7-openjdk-amd64/jre/lib/security/US_export_policy.jar
RUN rm /usr/lib/jvm/java-7-openjdk-amd64/jre/lib/security/local_policy.jar
ADD US_export_policy.jar /usr/lib/jvm/java-7-openjdk-amd64/jre/lib/security/US_export_policy.jar
ADD local_policy.jar /usr/lib/jvm/java-7-openjdk-amd64/jre/lib/security/local_policy.jar
RUN mkdir -p /Library/JSS/Logs

ADD .docker/jss.sh /usr/bin/jss.sh
RUN chmod +x /usr/bin/jss.sh

ADD .docker/DataBase.xml /usr/local/tomcat/webapps/ROOT/WEB-INF/xml/

WORKDIR /usr/local/tomcat/webapps/ROOT/WEB-INF/lib
COPY target/dependency/*.jar /usr/local/tomcat/webapps/ROOT/WEB-INF/lib/
COPY target/jss-etcd-1.0-SNAPSHOT.jar /usr/local/tomcat/webapps/ROOT/WEB-INF/lib/

EXPOSE 8443

CMD ["jss.sh"]