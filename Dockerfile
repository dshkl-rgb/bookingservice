FROM amazoncorretto:21-alpine3.20
COPY target/bookingservice-0.0.1-SNAPSHOT.jar bookingservice-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/bookingservice-0.0.1-SNAPSHOT.jar"]