# Build stage
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY migrations ./migrations
RUN mvn -q -DskipTests package

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/student-data-processing-0.0.1-SNAPSHOT.jar /app/app.jar
COPY migrations /app/migrations
ENV DB_HOST=db \
    DB_PORT=5432 \
    DB_NAME=studentdb \
    DB_USER=postgres \
    DB_PASSWORD=postgres \
    FLYWAY_FILES=/app/migrations \
    JAVA_TOOL_OPTIONS="-Xms512m -Xmx1g"
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
