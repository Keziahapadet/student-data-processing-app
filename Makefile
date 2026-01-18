COMPOSE = podman compose

.PHONY: build up down logs ps clean

build:
	$(COMPOSE) build

up:
	$(COMPOSE) up -d

logs:
	$(COMPOSE) logs -f

ps:
	$(COMPOSE) ps

down:
	$(COMPOSE) down

clean:
	$(COMPOSE) down -v
	rm -rf data-output
