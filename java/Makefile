artifact_id=proxy-api
group_id=com.proxy.demo
current_version=0.0.1
tag=$(current_version)-$(shell git rev-parse --short HEAD) 
published_image_tag=0.0.1-fbf344f


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


minikube-start:
	minikube start --cpus 4 --memory 8000 --nodes=1

minikube-delete:
	minikube delete


pods:
	minikube kubectl -- get pods -n proxy

k8s-proxy-logs:
	kubectl logs -f deployments/proxy-api -n proxy -f

k8s-helm-install:
	kubectl create namespace proxy 
	helm install redis oci://registry-1.docker.io/bitnamicharts/redis --namespace proxy --set architecture=standalone
	helm install proxy-api helm/proxy-api --set image.tag=$(published_image_tag) --namespace proxy

k8s-helm-upgrade:
	helm upgrade proxy-api helm/proxy-api --set image.tag=$(published_image_tag)

k8s-helm-uninstall:
	helm uninstall proxy-api -n proxy
	kubectl delete namespace proxy 
