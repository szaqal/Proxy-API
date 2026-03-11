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

build-java:
	docker build -t proxy-api:$(shell git rev-parse --short HEAD) -f java/Dockerfile java

start:
	TAG=$(shell git rev-parse --short HEAD) docker-compose up -d

stop:
	docker-compose down

