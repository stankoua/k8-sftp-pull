FROM openjdk:8-jre-alpine

RUN mkdir -p /opt/app
WORKDIR /opt/app

COPY ./devops/run_jar.sh ./target/scala-2.13/k8-sftp-push-assembly-0.1.0.jar ./

ENTRYPOINT ["./run_jar.sh"]