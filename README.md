# clj-slack-sql [![Build Status](https://travis-ci.org/tfoldi/clj-slack-sql.svg?branch=master)](https://travis-ci.org/tfoldi/clj-slack-sql)

This app does exactly what you need: post periodically executed SQL queries' output to slack channels. It can connect
to multiple databases (in parallel) executing set of queries using configurable `c3p0` connection pooling also in parallel.
Additionally you can retrieve the current and previous execution cycles' timestamps allowing to construct complex SQL queries
looking for only recent entries in source tables.

You can get responses from SQLs like:

```sql
SELECT last_error_message "Error" 
  FROM app_logs 
 WHERE last_update_date >= '%WINDOW_START_DATE%' 
   AND last_update_date < '%WINDOW_END_DATE%'`
```

directly on your slack channel(s). 

The application is written in clojure but the compiled jar file can run everywhere without any dependency other than the
JVM itself. Linux, Mac, Windows + other unixes are all supported.

## Installation

You can find the latest releases in [Github releases.](https://github.com/tfoldi/clj-slack-sql/releases)

Additionally you can download the sources and build for yourself. It requires only [Leiningen](leiningen.org).

    $ git clone https://github.com/tfoldi/clj-slack-sql.git
    $ cd clj-slack-sql
    $ lein uberjar


## Usage

First of all you need a cutting edge configuration. Lets have a look on the [included sample 
configuration](https://github.com/tfoldi/clj-slack-sql/blob/master/config/statements.yml).

### Generic section

In the generic section you can have to set two generic options:

 * `multithread`: controlls if queries for one data source are executed in parallel or sequential. `clj-slack-sql`
 uses database connection pools and the majority of jdbc drivers are thread safe, thus, in most of the cases you can safely
 say `yes`. However, if you don't want to stress your database system just set the value to `no`. 
 * `poll_interval`: defines the time required between two cycles

```yaml
# Example configuration

# If your database library is thread safe you can enable
# parallel execution of SQLs per database connections
multithread: yes
# Poll interall in seconds
poll_interval: 600
```

### Slack configuration

You need to set your secure token (which can be requested [here](https://api.slack.com/web)) either in the 
config file or as `SLACK_TOKEN` environment variable. Also, you must add the bot's name here.

```yaml
# token can be specified by SLACK_TOKEN environment variable
slack:
  # slack web api token, you can get one from here: https://api.slack.com/web
  token: <foo>
  # bot's name. Can be anything
  username: mybot
```

### Database configuration

`clj-slack-sql` can connect to multiple database to execute multiple queries. Each data source requires `name`,
`connection-uri`, `user` and `password` along with the `statements`. The `connection-uri` is a standard JDBC URL. 
Please make sure that your JDBC driver is on the `class_path`. 

```yaml  
# databases sections contains the source database and sql definitions
databases:
  - name: First DB Connection
    connection-uri: jdbc:h2:mem:test_mem
    user: sa
    password: ""
```

For each statement you need a `name`, `channel` and a `query`. You can leverage multi line YAML syntax to define 
complex SQLs. 
    
```yaml
    statements:
    - name: no output
      channel: "#test"
      type: simple
      query: SELECT 1 
    - name: using
      channel: "#test"
      type: simple
      # multi line queries can be added with > or | signs
      query: |
        SELECT '%WINDOW_START_DATETIME%' as "Cycle Started at"
             , '%WINDOW_END_DATETIME%' as "Cycle Ended at"
```

The application provides date/time templates for limiting your query to the cycle's time window. These templates:

```
 %WINDOW_START_DATE% 
 %WINDOW_START_DATETIME%
 %WINDOW_START_TIMESTAMP%
 %WINDOW_END_DATE%
 %WINDOW_END_DATETIME%
 %WINDOW_END_TIMESTAMP%  
```

The values are using ISO date formats. Examples: date => `2015-08-11`  datetime => `2015-08-11T07:39:01-00:00` 
timestamp => `2015-08-11T07:39:01.954-00:00`.
 
Finally, executing the application:

    $ java -jar clj-slack-sql-0.1.0-standalone.jar [args]

Make sure that all of your database drivers are in your `CLASSPATH`.

## Options

You can specify alternate configuration file with `-c` or `--config` option. 

## Examples

Additional examples are located in the `config` folder.

## License

Copyright © 2015 [Tamas Foldi](http://github.com/tfoldi), [Starschema](http://www.starschema.net/) ltd

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
