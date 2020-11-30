FROM openjdk:11-jre

COPY ./target/piko-admin.jar /var/piko-admin/piko-admin.jar
WORKDIR /var/piko-admin

EXPOSE 8080

CMD [ "java", "-jar", "piko-admin.jar", "--server.port=8080" ]
