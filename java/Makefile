.PHONY: help

help:
	@echo "Proxy-API Makefile"
	@echo ""
	@echo "Available targets:"
	@echo "  init-mvn             Initialize a new Spring Boot Maven project"
	@echo "  checkov-java         Run Checkov security analysis on Dockerfile"
	@echo "  docker-build-java    Build Docker image"
	@echo "  docker-run-java      Run Docker container"
	@echo "  docker-stop-java     Stop Docker container"
	@echo "  docker-logs          View Docker container logs"
	@echo "  build-java           Build Java application with Maven"
	@echo "  run-java             Run Java application locally"
	@echo "  bench                Run Apache Bench performance test"
	@echo "  minikube-start       Start Minikube cluster"
	@echo "  minikube-enable-ingress Enable ingress addon"
	@echo "  minikube-delete      Delete Minikube cluster"
	@echo "  pods                 List Kubernetes pods in proxy namespace"
	@echo "  k8s-proxy-logs       View application logs in Kubernetes"
	@echo "  k8s-helm-install     Deploy application with Helm"
	@echo "  k8s-helm-upgrade     Upgrade Helm deployment"
	@echo "  k8s-helm-uninstall   Uninstall Helm release"
	@echo "  k8s-check            Check deployed service"

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

minikube-enable-ingress:
	minikube addons enable ingress

minikube-delete:
	minikube delete


pods:
	minikube kubectl -- get pods -n proxy

k8s-proxy-logs:
	kubectl logs -f deployments/proxy-api -n proxy -f

k8s-helm-install:
	kubectl create namespace proxy 
	helm install redis oci://registry-1.docker.io/bitnamicharts/redis --namespace proxy --set architecture=standalone,auth.enabled=false
	helm install proxy-api helm/proxy-api --set image.tag=$(published_image_tag) --namespace proxy

k8s-helm-upgrade:
	helm upgrade proxy-api helm/proxy-api --set image.tag=$(published_image_tag) --namespace proxy

k8s-helm-uninstall:
	helm uninstall proxy-api -n proxy
	kubectl delete namespace proxy 

k8s-check:
	curl -H "Host: proxy-api.local" "http://$$(minikube ip)/forecast?latitude=52.52&longitude=13.41&current=temperature_2m,wind_speed_10m"
