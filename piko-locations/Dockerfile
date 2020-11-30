FROM openjdk:11-jre

COPY ./target/piko-locations.jar /var/piko-locations/piko-locations.jar
WORKDIR /var/piko-locations

EXPOSE 8080

CMD [ "java", "-jar", "piko-locations.jar", "--server.port=8080" ]
