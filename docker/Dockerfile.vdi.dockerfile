FROM amazoncorretto:21-alpine-jdk AS builder

ARG MAVEN_OPTS

RUN mkdir -p /usr/src/app

WORKDIR /usr/src/app

COPY . /usr/src/app

RUN ./mvnw clean package -Pvisa-vdi -B -Dquarkus.package.type=uber-jar -DskipTests=true $MAVEN_OPTS

FROM amazoncorretto:21-alpine

RUN mkdir -p /app

WORKDIR /app

# copy built application
COPY --from=builder /usr/src/app/visa-app-vdi/target/visa-vdi-runner.jar /app/visa-vdi.jar
COPY --from=builder /usr/src/app/db /app/db

CMD [ "java", "-Djava.util.logging.manager=org.jboss.logmanager.LogManager", "-jar", "/app/visa-vdi.jar" ]

EXPOSE 8087
