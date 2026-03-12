artifact_id=proxy-api
group_id=com.proxy.demo
current_version=0.0.1
tag=$(current_version)-$(shell git rev-parse --short HEAD) 


init-mvn:
	cd java && spring init \
		--java-version=21 \
		--build=maven \
		--packaging=jar \
		--type=maven-project \
		--artifact-id=$(artifact_id) \
		--group-id=$(group_id) \
		--dependencies=actuator,web \
		--extract $(artifact_id)

checkov-java:
	docker run --rm -v $(shell pwd):/work bridgecrew/checkov -f /work/Dockerfile.local --framework dockerfile

docker-build-java:
	docker build -t proxy-api:$(tag) -f Dockerfile.local .

docker-run-java:
	TAG=$(tag) docker-compose -f docker-compose.yml up -d

docker-stop-java:
	docker-compose -f docker-compose.yml down

docker-logs:
	docker-compose -f docker-compose.yml logs -f

build-java:
	proxy-api/mvnw clean package -f proxy-api/pom.xml 

run-java:
	java -jar proxy-api/target/proxy-api-$(current_version)-SNAPSHOT.jar

bench:
	ab -n 100 -c 100 "http://localhost:8080/forecast?latitude=52.52&longitude=13.41&current=temperature_2m,wind_speed_10m"

