container_name := vespa

run:
	docker run --detach --name ${container_name} --hostname vespa-container \
		--publish 8080:8080 --publish 19071:19071 \
		vespaengine/vespa

deploy:
	@docker container inspect -f '{{.State.Status}}' ${container_name}
	@$(MAKE) -s -C vespa deploy

.PHONY: run deploy
