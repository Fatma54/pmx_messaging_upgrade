#
# Open JDK Dockerfile
#
# Nikolai Reed, reed@1manprojects.de

# Pull base image.
FROM openjdk:8u111-jre

EXPOSE 8080

# Define working directory.

WORKDIR /opt
ADD pcmConsole.jar /opt/data/
ADD target/pcmserver-0.0.1-SNAPSHOT.jar /opt/
ADD Palladio.resourcetype /opt/
ADD PrimitiveTypes.repository /opt/
	
RUN \
	mkdir /opt/input && \
	mkdir /opt/zip && \
	mkdir /opt/download && \
	mkdir /opt/output && \
	mkdir /opt/examples && \
	mkdir /opt/uploaded

ADD examples/bookstore-KIEKER.zip /opt/examples/
ADD examples/Calculator-KIEKER.zip /opt/examples/

ENTRYPOINT ["java", "-jar", "/opt/pcmserver-0.0.1-SNAPSHOT.jar"]

#CMD ["java", "-jar", "/opt/pcmserver-0.0.1-SNAPSHOT.jar", "--cores="edge-uq38n=4, middletier-64bqq=4""]
