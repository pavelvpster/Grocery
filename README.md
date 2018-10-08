# Grocery

Grocery is a web application that allows users to track their purchases made in different shops.

## Documentation

[Release Process](doc/release-process.md)

## Setup database

Install PostgreSQL:

```
sudo apt-get install postgresql
```

Start `psql` as `postgres` user:

```
sudo su postgres
psql
```

Create user and database:

```
CREATE USER grocery WITH PASSWORD 'grocery';
CREATE DATABASE grocery OWNER grocery;

\q (quit from PSQL)
```

Update database schema:

```
./gradlew update
```

Rollback one migration:

```
./gradlew rollbackCount -PliquibaseCommandValue=1
```

## Build

```
./gradlew clean build test
```

## Run

```
./gradlew run
```

or

```
java -jar grocery-<version>.jar
```

## License

Copyright (C) 2016-2018 Pavel Prokhorov (pavelvpster@gmail.com)


This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
