# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only what Maven needs first (better caching)
COPY pom.xml .
COPY src ./src

# Build the jar
RUN mvn -DskipTests package

# ---------- Run stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built jar
COPY --from=build /app/target/*.jar app.jar

# Render provides PORT; your app should use server.port=${PORT:8080}
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
