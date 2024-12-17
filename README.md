### Prerequisites
java 21, mvn latest, docker latest, docker-compose latest
### How to Run
navigate to repo root \
execute:\
Linux/Mac\
`docker compose down --volumes && mvn clean install  && docker compose -f compose.yaml up --build`

Windows Powershell:\
`docker compose down --volumes; mvn clean install; docker compose -f compose.yaml up --build`
\
*restart the env without running tests*:\ 
docker compose down --volumes && mvn clean install **-DskipTests** && docker compose -f compose.yaml up --build
### Swagger location
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

User swagger openapi to import a postman collection (easier testing)
[http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) - use this link for import
[how to import swagger into postman](https://learning.postman.com/docs/getting-started/importing-and-exporting/importing-from-swagger/) 
### Sample Requests

#### Get available rooms
curl --location 'http://localhost:8080/rooms?from=2024-12-19&to=2024-12-20&page=2&size=10'

#### Make reservation
curl --location 'http://localhost:8080/reservations' \
--header 'Content-Type: application/json' \
--data '{
"roomId":"8807",
"startDate":"2025-12-01",
"endDate":"2025-12-12"
}'

#### Cancel Reservation
curl --location --request DELETE 'http://localhost:8080/reservations/9808'

### How to run Unit Tests
mvn clean install
