# Janelle [WIP]
Janelle (pronounced *ya-nel*) is a toy SQL database written in Java. The goal of this project is to learn how databases work by building one.

## Features
- __REPL:__ UI for the DB.
- __SQL Parser:__ Generates ASTs for basic SQL queries (see _Queries_ section below for more info).
- __B+ Tree:__ Read-optimized data structure for DB tables and indexes.
- __Write-Ahead Log:__ Data structure for enabling ACID compliance.

## Demo
...

## Download
...

## Project setup
- Clone repo.
- Build `./gradlew buildRepl`.
- Run `java -jar repl/build/libs/repl.jar`.

## Queries
- CRUD table
- CRUD data
- Aggregate data (count, sum, average)

## REPL commands
- `.EXIT`: Exit REPL.
- `.TABLES`: List tables in the DB.
- `.DEBUG`: Enable debug mode with `.DEBUG Y` for extra info in query results. This is enabled by default. Disable with `.DEBUG N`.

## Not implemented
Things I'd have liked to implement:
- Pager (to take advantage of the [OS page cache](https://en.wikipedia.org/wiki/Page_cache)).
- Table joins.
- Primary, foreign and unique key constraints.
- Group-by aggregation.
