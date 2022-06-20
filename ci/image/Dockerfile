FROM ubuntu:bionic-20200112

RUN apt-get update
RUN apt-get install --no-install-recommends -y ca-certificates net-tools git curl jq
RUN rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME /opt/openjdk
ENV PATH $JAVA_HOME/bin:$PATH
RUN mkdir -p /opt/openjdk && \
    cd /opt/openjdk && \
    curl -L https://github.com/bell-sw/Liberica/releases/download/8u333+2/bellsoft-jdk8u333+2-linux-amd64.tar.gz | tar xz --strip-components=1

ADD https://raw.githubusercontent.com/spring-io/concourse-java-scripts/v0.0.2/concourse-java.sh /opt/