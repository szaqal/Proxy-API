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

build-java:
	proxy-api/mvnw clean package -f proxy-api/pom.xml

run-java:
	java -jar proxy-api/target/proxy-api-$(current_version)-SNAPSHOT.jar

