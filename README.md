# Grocery
Grocery is a web application that allows users to track their purchases made in different shops.


## Documentation
[Release Process](doc/release-process.md)


## Build Docker
```sh
./gradlew clean bootJar

cp ./build/libs/grocery-*.jar ./docker/backend/grocery.jar

cp ./src/main/resources/db/migration/*.sql ./docker/db/initdb
rm ./docker/db/initdb/*-rollback.sql

cd ./docker

docker-compose build
```


## Run Docker
```sh
cd ./docker
docker-compose up
```


## License
Copyright (C) 2016-2022 Pavel Prokhorov (pavelvpster@gmail.com)


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
