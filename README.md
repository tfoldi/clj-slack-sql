# clj-slack-sql [![Build Status](https://travis-ci.org/tfoldi/clj-slack-sql.svg?branch=master)](https://travis-ci.org/tfoldi/clj-slack-sql)

This app does exactly what you need: post periodically executed SQL queries' output to slack channels. It can connect
to multiple databases (in parallel) executing set of queries using configurable `c3p0` connection pooling also in parallel.
Additionally you can retrieve the current and previous execution cycle's timestamp allowing to construct complex SQL queries
looking for only recent entries in source tables.
 
The application is written in clojure but the compiled jar file can run everywhere without any dependency other than the
JVM itself. Linux, Mac, Windows + other unixes are all supported.

## Installation

You can find the latest releases in [Github releases.](https://github.com/tfoldi/clj-slack-sql/releases)

Additionally you can download the sources and build for yourself. It requires only [Leiningen|leiningen.org].

    $ git clone https://github.com/tfoldi/clj-slack-sql.git
    $ cd clj-slack-sql
    $ lein uberjar


## Usage

First of all you need a cutting edge configuration. Lets have a look on the [included sample 
configuration](https://github.com/tfoldi/clj-slack-sql/blob/master/config/statements.yml).

```yaml
# Example configuration

# If your database library is thread safe you can enable
# parallel execution of SQLs per database connections
multithread: no
# Poll interall in seconds
poll_interval: 10

# token can be specified by SLACK_TOKEN environment variable
slack:
  # slack web api token, you can get one from here: https://api.slack.com/web
  token: <foo>
  # bot's name. Can be anything
  username: csicska
  
# databases sections contains the source database and sql definitions
databases:
  - name: First DB Connection
    connection-uri: jdbc:h2:mem:test_mem
    user: sa
    password: ""
    statements:
    - name: no output
      channel: "#test"
      type: simple
      query: SELECT 1 WHERE 1 = 0
    - name: bad query
      type: simple
      channel: "#test"
      query: SELECT 1 WHERE 1 = # bad query
    - name: two lines, two columns
      type: simple
      channel: "#test"
      query: SELECT 1 col1, 2 col2 union all select 3 col1, 4 col2
  - name: Second DB Connection
    connection-uri: jdbc:h2:mem:test_mem2
    user: sa
    password: ""
    statements:
    - name: using
      channel: "#test"
      type: simple
      # multi line queries can be added with > or | signs
      query: |
        SELECT '%WINDOW_START_DATETIME%' as "Cycle Started at"
             , '%WINDOW_END_DATETIME%' as "Cycle Ended at"
```

Finally, executing the application:

    $ java -jar clj-slack-sql-0.1.0-standalone.jar [args]

Make sure that all of your database drivers are in your `CLASSPATH`.

## Options

You can specify alternate configuration file with `-c` or `--config` option.

## Examples

Additional examples are located in the `config` folder.

### Bugs

Cycle time is more like a sleep time - this will be changed soon.

## License

Copyright © 2015 [Tamas Foldi](http://github.com/tfoldi), [Starschema](http://www.starschema.net/) ltd

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
