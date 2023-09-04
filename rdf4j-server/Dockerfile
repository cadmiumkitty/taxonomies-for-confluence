FROM tomcat:9.0.73-jdk17-temurin-jammy

ARG RDF4J_VERSION=4.2.4

RUN addgroup --system tomcat ; \
    adduser --system tomcat --ingroup tomcat

ADD ./rdf4j-server/eclipse-rdf4j-${RDF4J_VERSION}-sdk.zip /tmp/eclipse-rdf4j-${RDF4J_VERSION}-sdk.zip

RUN apt-get update ; \
	apt-get -y install unzip

RUN set -eux ; \
    unzip -q /tmp/eclipse-rdf4j-${RDF4J_VERSION}-sdk.zip -d /tmp/ ; \
	mkdir /usr/local/tomcat/webapps/rdf4j-server ; \
	unzip /tmp/eclipse-rdf4j-${RDF4J_VERSION}/war/rdf4j-server.war -d /usr/local/tomcat/webapps/rdf4j-server ; \
	rm -Rf /tmp/eclipse-rdf4j-${RDF4J_VERSION} ; \
	rm /tmp/eclipse-rdf4j-${RDF4J_VERSION}-sdk.zip ; \
	mkdir /var/rdf4j ; \
    chown -R tomcat:tomcat /var/rdf4j /usr/local/tomcat ; \
	chmod 744 /usr/local/tomcat /usr/local/tomcat/bin /usr/local/tomcat/bin/catalina.sh ; \
	chmod 644 /var/rdf4j

ADD ./rdf4j-server/server.xml /usr/local/tomcat/conf/server.xml
ADD ./rdf4j-server/logging.properties /usr/local/tomcat/conf/logging.properties
ADD ./rdf4j-server/web.xml /usr/local/tomcat/webapps/rdf4j-server/WEB-INF/web.xml

USER tomcat:tomcat

WORKDIR /usr/local/tomcat/

EXPOSE 8080