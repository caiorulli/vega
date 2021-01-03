FROM --platform=linux/arm/v7 java

# Installing leiningen
RUN curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein -o /usr/local/bin/lein \
    && chmod a+x /usr/local/bin/lein

# Just so it self downloads.
RUN echo '(exit)' | lein repl

ADD . .
RUN lein uberjar

CMD ["java", "-jar", "./target/uberjar/vega-0.1.0-SNAPSHOT-standalone.jar"]
