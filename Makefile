
init-mvn:
	cd java && spring init \
		--java-version=21 \
		--build=maven \
		--packaging=jar \
		--type=maven-project \
		--artifact-id=proxy-api 
		--group-id=com.proxy.demo \
		--dependencies=actuator,web \
		--extract ptc-demo

