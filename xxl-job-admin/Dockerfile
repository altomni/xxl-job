FROM openjdk:8-jre-slim
MAINTAINER xuxueli

ENV PARAMS=""

#ENV TZ=PRC
#RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

#ENV JAVA_OPTS="-Xms1024m -Xmx3072m"

ADD target/xxl-job-admin-*.jar /app.jar

EXPOSE 8080

ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /app.jar $PARAMS"]