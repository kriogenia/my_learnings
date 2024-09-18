container_name := vespa

run:
	docker run --detach --name ${container_name} --hostname vespa-container \
		--publish 8080:8080 --publish 19071:19071 \
		--volume vespa_var:/opt/vespa/var  \
		--volume vespa_logs:/opt/vespa/logs  \
		vespaengine/vespa

deploy:
	@if [ -z "`docker ps -a -q -f name=vespa`" ]; then \
		echo "There's no ${container_name} container, please run the `make run` recipe first"; \
		exit 1; \
	elif [ -z "`docker ps -q -f name=vespa`" ]; then \
		echo "Vespa container is not running. Starting it..."; \
		docker start ${container_name}; \
	fi
	@$(MAKE) -s -C vespa deploy

.PHONY: run deploy
