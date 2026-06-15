FROM maven:3.9.6-eclipse-temurin-17
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean install -DskipTests
CMD mvn flyway:migrate -Dflyway.url="${SPRING_DATASOURCE_URL}" -Dflyway.user="${SPRING_DATASOURCE_USERNAME}" -Dflyway.password="${SPRING_DATASOURCE_PASSWORD}" && mvn exec:java
