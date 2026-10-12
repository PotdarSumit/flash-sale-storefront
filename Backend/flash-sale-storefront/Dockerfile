
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/flash-sale-storefront-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=35.0", "-jar", "app.jar"]
