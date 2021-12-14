FROM arm32v7/eclipse-temurin:17

COPY ./target/vega-1.0.0-SNAPSHOT-standalone.jar .

CMD ["java", "-jar", "vega-1.0.0-SNAPSHOT-standalone.jar"]
