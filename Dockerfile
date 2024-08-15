
FROM openjdk:21-jdk-oracle
# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/fpl-scraper.jar /app/fpl-scraper.jar

# Expose the port your Spring Boot app runs on
EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java", "-jar", "/app/fpl-scraper.jar"]
