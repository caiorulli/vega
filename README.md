# Vega

My personal telegram bot. Built with [integrant](https://github.com/weavejester/integrant), [datahike](https://github.com/replikativ/datahike) and [morse](https://github.com/Otann/morse).

### Features

- `/help` - Display a help text.
- `/time [friend name]` - Displays current time for your friend's timezone. Be sure to check on this only after you bombard him with messages late at night.
- `/reaction [trigger] [sentence]` - Ensures vega will say [sentence] whenever anyone says [trigger].
- `/reaction_list` - Lists registered reactions.
- `/reddit [subreddit?]` - Gets random image from specified subreddit. If no subreddit is specified, fetches from default subreddit

#### How to run it

``` sh
clojure -M:run
```

#### How to run tests

``` sh
clojure -M:test
```

#### How to build uberjar

``` sh
clojure -T:build uber
```
