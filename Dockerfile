FROM maven:3.6-openjdk-14 as builder

ARG MAVEN_OPTS

RUN mkdir -p /usr/src/app

WORKDIR /usr/src/app

COPY . /usr/src/app

RUN mvn package -B -DskipTests=true $MAVEN_OPTS

FROM openjdk:14-alpine

RUN mkdir -p /app

WORKDIR /app

# copy built application
COPY --from=builder /usr/src/app/visa-app/configuration.yml /app
COPY --from=builder /usr/src/app/visa-app/target/visa-app.jar /app
COPY --from=builder /usr/src/app/db /app/db

CMD java -jar /app/visa-app.jar server /app/configuration.yml

EXPOSE 8086 8087
