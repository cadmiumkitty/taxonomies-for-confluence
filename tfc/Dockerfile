FROM openjdk:17-jdk-alpine

RUN addgroup --system spring ; \
    adduser --system spring --ingroup spring

RUN mkdir /usr/local/tomcat ; \
    chown -R spring:spring /usr/local/tomcat ; \
    chmod 744 /usr/local/tomcat

USER spring:spring

ADD target/taxonomies-for-confluence-4.16.0.jar taxonomies-for-confluence-4.16.0.jar

ENTRYPOINT ["java", "-jar", "taxonomies-for-confluence-4.16.0.jar"]