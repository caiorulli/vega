FROM openjdk:17

COPY ./target/vega-1.0.0-standalone.jar .

CMD ["java", "-jar", "vega-1.0.0-standalone.jar"]
