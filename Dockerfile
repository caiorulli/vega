FROM clojure:tools-deps
ARG telegram_token

COPY . .
RUN clojure -P
ENV telegram_token=$telegram_token

CMD ["clojure", "-X", "vega.core/start"]
