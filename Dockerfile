# ---- Build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:resolve
COPY src ./src
COPY gen ./gen
RUN mvn -B -q -DskipTests package

# ---- Runtime ----
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 10001 beacon
COPY --from=build /app/target/app.jar app.jar
USER beacon
EXPOSE 8080 9090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
