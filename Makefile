artifact_id=proxy-api
group_id=com.proxy.demo


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
	docker run --rm -v $(shell pwd):/work bridgecrew/checkov -f /work/java/Dockerfile --framework dockerfile

build-java:
	docker build -t proxy-api:$(shell git rev-parse --short HEAD) -f java/Dockerfile java

start-java:
	TAG=$(shell git rev-parse --short HEAD) docker-compose -f java/docker-compose.yml up -d

stop-java:
	docker-compose -f java/docker-compose.yml down

