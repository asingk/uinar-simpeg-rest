FROM eclipse-temurin:17-jdk-alpine
LABEL authors="asingk"

ARG CDN_URL=
#ARG JAR_FILE=build/libs/*SNAPSHOT.jar
ARG JAR_FILE=build/libs/*RELEASE.jar

RUN apk add --no-cache tzdata
ENV TZ=Asia/Jakarta
RUN apk add --no-cache openssh

RUN addgroup -S asingk && adduser -S asingk -G asingk
USER asingk
RUN mkdir -p ~/.ssh
RUN touch ~/.ssh/known_hosts
RUN	ssh-keyscan -H $CDN_URL >> ~/.ssh/known_hosts

COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]