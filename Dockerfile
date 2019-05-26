FROM gradle:4.6-jdk8 as builder

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN gradle build --stacktrace -x test

FROM openjdk:8-jre-slim
EXPOSE 8080
COPY --from=builder /home/gradle/src/ada-vcs/build/libs/ada-vcs-0.0.42.jar /app/ada-vcs.jar
WORKDIR /app
RUN mkdir journal
CMD java -jar ada-vcs.jar server
