# Janelle [WIP]
Janelle (pronounced *ya-nel*) is a simple SQL database written in Java. The goal of this project is to learn how databases work by building one.

## Features
- Connect to DB via TCP server or REPL.
- Run basic SQL queries (see section below).
- Index tables for faster queries.
- Start and commit/roll back transactions.

## Setup
- Clone repo.
- Build REPL `./gradlew buildRepl`.
- Or build server `./gradlew buildServer`.
- Run REPL `java -jar repl/build/libs/repl.jar`.
- Or run server `java -jar server/build/libs/server.jar`.

## Queries
- CRUD table
- CRUD data
- Aggregate data (count, sum, average, group by)
- Join tables

## REPL commands
- `.EXIT`: Exit REPL.
