FROM java

COPY ./target/vega-1.0.0-SNAPSHOT-standalone.jar .

CMD ["java", "-jar", "vega-1.0.0-SNAPSHOT-standalone.jar"]
