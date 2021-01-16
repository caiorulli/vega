FROM java
ARG telegram_token

COPY ./target/vega-1.0.0-SNAPSHOT-standalone.jar .
ENV telegram_token=$telegram_token

CMD ["java", "-jar", "vega-1.0.0-SNAPSHOT-standalone.jar"]
