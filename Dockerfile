FROM openjdk:21-jdk as builder

ARG MAVEN_OPTS

RUN mkdir -p /usr/src/app

WORKDIR /usr/src/app

COPY . /usr/src/app

RUN ./mvnw clean package -B -Dquarkus.package.type=uber-jar -DskipTests=true $MAVEN_OPTS

FROM amazoncorretto:21-alpine

RUN mkdir -p /app

WORKDIR /app

# copy built application
COPY --from=builder /usr/src/app/visa-app/target/visa-app-runner.jar /app/visa-app.jar
COPY --from=builder /usr/src/app/db /app/db

CMD java -jar /app/visa-app.jar

EXPOSE 8086 8087
