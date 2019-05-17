FROM gradle:4.6-jdk8 as builder

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN echo "HELLO" > test.txt
RUN cat test.txt
RUN gradle build --stacktrace

FROM openjdk:8-jre-slim
EXPOSE 8080
COPY --from=builder /home/gradle/src/ada-vcs/build/libs/ada-vcs-0.0.42.jar /app/ada-vcs.jar
WORKDIR /app
CMD java -jar ada-vcs.jar server
