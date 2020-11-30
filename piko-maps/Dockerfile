FROM openjdk:11-jre

COPY ./target/piko-maps.jar /var/piko-maps/piko-maps.jar
WORKDIR /var/piko-maps

EXPOSE 8080

CMD [ "java", "-jar", "piko-maps.jar", "--server.port=8080" ]
