# Source - https://stackoverflow.com/a
# Posted by Mark O'Connor, modified by community. See post 'Timeline' for change history
# Retrieved 2025-11-08, License - CC BY-SA 4.0

#
# Build stage
#
FROM maven:3.9.7-eclipse-temurin-21-alpine AS build
ENV HOME=/usr/app
RUN mkdir -p $HOME
WORKDIR $HOME
ADD . $HOME
RUN --mount=type=cache,target=/root/.m2 mvn -f $HOME/pom.xml clean package -DskipTests

#
# Package stage
#
FROM eclipse-temurin:21-jre-jammy

# OTHER DEPENDENCIES: pymol. graphviz, clustalo, mmseqs2, blast
# Latest pymol in Ubuntu 22.04 and 24.04 is 2.5.x
# i.e. for pymol 2.x to be installed this script requires a minimum of Ubuntu 22.04
RUN apt-get update && apt-get install -qqy pymol
RUN apt-get -yqq install ncbi-blast+ clustalo graphviz
ENV mmsdir=/opt/mmseqs2
RUN mkdir -p $mmsdir
# Download the AVX2 binary provided by mmseqs2 developers. More info at https://github.com/soedinglab/MMseqs2
# Note that the binary available at https://mmseqs.com/latest/mmseqs-static_avx2.tar.gz seems to be for the latest commit.
# We now point to a stable release. Whenever we need to upgrade we need to change the version below (see releases tab in github)
RUN curl -L "https://github.com/soedinglab/MMseqs2/releases/download/13-45111/mmseqs-linux-avx2.tar.gz" > /opt/mmseqs-static-avx2.tar.gz
# this unpacks it under mmseqs2
RUN tar -zxvf /opt/mmseqs-static-avx2.tar.gz -C $mmsdir --strip-components=1
RUN ln -s $mmsdir/bin/mmseqs /usr/local/bin/mmseqs

# JAVA from here
# Build arg to choose which module
ARG MODULE=eppic-cli
ARG JAR_FILE=/usr/app/${MODULE}/target/*${MODULE}*.jar
COPY --from=build ${JAR_FILE} /app/runner.jar

EXPOSE 8080
ENTRYPOINT exec java -jar /app/runner.jar
