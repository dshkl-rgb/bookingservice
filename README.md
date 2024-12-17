### Prerequisites
java 21, mvn latest, docker latest, docker-compose latest
### How to Run
navigate to repo root
docker compose down --volumes && mvn clean install  && docker compose -f compose.yaml up --build

*restart the env without running tests*: docker compose down --volumes && mvn clean install **-DskipTests** && docker compose -f compose.yaml up --build
### Swagger location
http://localhost:8080/swagger-ui/index.html
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
