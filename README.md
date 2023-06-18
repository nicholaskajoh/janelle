# Janelle [WIP]
Janelle (pronounced *ya-nel*) is a toy SQL database written in Java. The goal of this project is to learn how databases work by building one.

## Features
- __CLI:__ For connecting to and querying the DB.
- __TCP Client & Server__: For communication between the CLI and DB server.
- __SQL Parser:__ For parsing a flavor of SQL called NickSQL (pronounced _Nick's QL_). Generates abstract syntax trees for basic SQL queries (see _Queries_ section below for more info).
- __B+ Tree:__ Data structure for DB tables.
- __Two-phase Locking:__ To facilitate transactions.

## Demo
...

## Setup
- Clone repo.
- Build `./gradlew build`.
- Run server `java -jar server/build/libs/server.jar`.
- Run client `java -jar client/build/libs/client.jar`.
- Run tests `./gradlew test`.

## Queries
```sql
  -- create table
  create table customers (
    name string(50) required,
    email string(25) nullable,
    num_orders int default 0,
    voucher_balance float default 0,
    has_premium_plan bool default false
  );

  -- read table
  select *
  from customers;

  describe table customers;

  -- update table
  alter table customers
  add column phone_number string;

  alter table customers
  rename column phone_number to phone;

  alter table customers
  drop column phone_number;

  -- delete table
  drop table customers;

  -- create rows
  insert into customers (name, email, has_premium_plan)
  values ("Janelle", "ja@nel.le", true);

  -- read rows
  select name, email, num_orders
  from customers
  where (num_orders > 0 and num_orders <= 5) or voucher_balance = 50.00 or (email = "ja@nel.le" and name != null and has_premium_plan = true)
  order by num_orders desc;

  -- update rows
  update customers
  set name = "Milan", email = "mi@la.no"
  where email = "ja@nel.le";

  -- delete rows
  delete from customers
  where email = "mi@la.no";

  -- transactions
  begin;

  commit;

  rollback;
  ```

## CLI commands
- `.EXIT`: Exit CLI.
- `.TABLES`: List tables in the DB.
- `.COLUMNS`: List columns in a table with `.COLUMNS {table_name}` e.g `.COLUMNS jn_configs`.
- `.GENERATE`: Create table and populate it with sample data. Usage - `.GENERATE {number_of_records}` e.g `.GENERATE 100`.

## Not implemented
Things I'd have liked to implement:
- Indexes for faster reads.
- Paging and a shared buffer (i.e. in-memory cache) for minimizing disk hits and thus read-write latency.
- Write-ahead log (WAL) and a check-pointer for crash recovery and data replication.
- Table joins.
- Primary, foreign and unique key constraints.
- Aggregates (like count, sum and average), and other functions.
