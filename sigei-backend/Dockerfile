FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

COPY src src
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -q clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S sigei && adduser -S sigei -G sigei
WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar
COPY --chown=sigei:sigei planilhas planilhas

USER sigei
EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
