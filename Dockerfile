FROM eclipse-temurin:17-jre

COPY ./target/*.jar /usr/app/application.jar
COPY env.properties /usr/app/
WORKDIR /usr/app

ENTRYPOINT ["java", "-jar", "application.jar"]