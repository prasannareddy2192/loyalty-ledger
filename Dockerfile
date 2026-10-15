FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/ledger-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-Duser.timezone=UTC", "-jar", "app.jar"]
EXPOSE 8080