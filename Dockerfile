FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY . .
RUN mvn -B -DskipTests -pl bootstrap -am package

FROM eclipse-temurin:17-jre
WORKDIR /opt/app

COPY --from=builder /app/bootstrap/target/app.jar ./app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
