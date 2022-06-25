FROM openjdk:11.0.15-jre

COPY grocery.jar /home

WORKDIR /home

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "./grocery.jar"]
