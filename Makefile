SHELL := /bin/sh

MVNW     := ./mvnw
COMPOSE  := docker compose
COMPOSE_LOCAL := docker compose -f compose.yml --env-file .env
COMPOSE_PROD  := docker compose -f compose.prod.yml --env-file .env.prod
COMPOSE_LAB   := docker compose -f compose.local.yml --env-file .env.local

SERVICES := event-management-service seating-inventory-service orders-service payments-service ticket-issuance-service notifications-service
EXISTING_SERVICES := event-management-service seating-inventory-service payments-service ticket-issuance-service access-control-service notifications-service reporting-service
CRITICAL_SERVICES := rabbitmq event-management-service seating-inventory-service orders-service payments-service ticket-issuance-service notifications-service
PROD_SERVICES := rabbitmq event-management-service seating-inventory-service orders-service payments-service ticket-issuance-service access-control-service notifications-service reporting-service

.PHONY: help \
	test test-existing test-event-management test-seating-inventory test-orders test-payments test-ticket-issuance test-access-control test-notifications test-reporting \
	coverage-event-management coverage-check-event-management coverage-payments coverage-check-payments coverage-ticket-issuance coverage-check-ticket-issuance \
	compile compile-existing compile-event-management compile-seating-inventory compile-orders compile-payments compile-ticket-issuance compile-access-control compile-notifications compile-reporting \
	docker-build docker-build-existing docker-up docker-up-build docker-up-critical docker-up-event-management docker-up-seating-inventory docker-up-orders docker-up-payments docker-up-ticket-issuance docker-up-access-control docker-up-notifications docker-up-reporting docker-up-rabbitmq \
	docker-down docker-restart docker-ps logs logs-event-management logs-seating-inventory logs-orders logs-payments logs-ticket-issuance logs-access-control logs-notifications logs-reporting logs-rabbitmq \
	local-up local-up-build local-down local-restart local-ps local-logs \
	prod-up prod-up-build prod-down prod-restart prod-ps prod-logs \
	lab-up lab-up-build lab-down lab-restart lab-ps lab-logs

help:
	@echo "OrionTicket commands"
	@echo ""
	@echo "Tests"
	@echo "  make test                   Run tests for all Maven services"
	@echo "  make test-existing          Run tests for services currently present in the repo"
	@echo "  make test-event-management"
	@echo "  make test-seating-inventory"
	@echo "  make test-orders"
	@echo "  make test-payments"
	@echo "  make test-ticket-issuance"
	@echo "  make test-access-control"
	@echo "  make test-notifications"
	@echo "  make test-reporting"
	@echo ""
	@echo "Coverage"
	@echo "  make coverage-event-management        Generate the JaCoCo report for event-management-service unit tests"
	@echo "  make coverage-check-event-management  Run full event-management-service verification and enforce 70% focused line coverage"
	@echo "  make coverage-payments        Generate the JaCoCo report for payments-service unit tests"
	@echo "  make coverage-check-payments  Run full payments-service verification and enforce 70% focused line coverage"
	@echo "  make coverage-ticket-issuance        Generate the JaCoCo report for ticket-issuance-service unit tests"
	@echo "  make coverage-check-ticket-issuance  Run full ticket-issuance-service verification and enforce 10% temporary line coverage"
	@echo ""
	@echo "Compile"
	@echo "  make compile                Compile all Maven services"
	@echo "  make compile-existing       Compile services currently present in the repo"
	@echo "  make compile-payments"
	@echo ""
	@echo "Docker local (compose.yml + .env)"
	@echo "  make docker-build           Build Docker images for the 7-service local stack"
	@echo "  make docker-build-existing  Build Docker images for services currently present in the repo"
	@echo "  make docker-up              Start local 7-service stack with PostgreSQL and RabbitMQ"
	@echo "  make docker-up-build        Build and start local 7-service stack"
	@echo "  make docker-up-critical     Start critical purchase-flow services"
	@echo "  make docker-down            Stop and remove compose services"
	@echo "  make docker-ps              Show compose service status"
	@echo "  make logs                   Follow logs for all services"
	@echo "  make logs-payments          Follow logs for payments-service"
	@echo ""
	@echo "Local (compose.yml + .env)"
	@echo "  make local-up               Levantar 7 servicios, PostgreSQL y RabbitMQ"
	@echo "  make local-up-build         Build + levantar (local)"
	@echo "  make local-down             Detener y eliminar contenedores locales"
	@echo "  make local-restart          Reiniciar todos los servicios locales"
	@echo "  make local-ps               Estado de los servicios locales"
	@echo "  make local-logs             Seguir logs de todos los servicios locales"
	@echo ""
	@echo "Prod (compose.prod.yml + .env.prod)"
	@echo "  make prod-up                Levantar 9 servicios y RabbitMQ; DB via HAProxy"
	@echo "  make prod-up-build          Build + levantar (prod)"
	@echo "  make prod-down              Detener y eliminar contenedores de producción"
	@echo "  make prod-restart           Reiniciar todos los servicios de producción"
	@echo "  make prod-ps                Estado de los servicios de producción"
	@echo "  make prod-logs              Seguir logs de todos los servicios de producción"
	@echo ""
	@echo "Lab (compose.local.yml + .env.local)"
	@echo "  make lab-up                 Levantar 7 servicios y RabbitMQ; DB via HAProxy"
	@echo "  make lab-up-build           Build + levantar (lab)"
	@echo "  make lab-down               Detener y eliminar contenedores lab"
	@echo "  make lab-ps                 Estado de servicios lab"
	@echo "  make lab-logs               Seguir logs de servicios lab"

test: test-event-management test-seating-inventory test-orders test-payments test-ticket-issuance test-access-control test-notifications test-reporting

test-existing: test-event-management test-seating-inventory test-payments test-ticket-issuance test-access-control test-notifications test-reporting

test-event-management:
	$(MVNW) -f event-management-service/pom.xml test

test-seating-inventory:
	$(MVNW) -f seating-inventory-service/pom.xml test

test-orders:
	$(MVNW) -f orders-service/pom.xml test

test-payments:
	$(MVNW) -f payments-service/pom.xml test

test-ticket-issuance:
	$(MVNW) -f ticket-issuance-service/pom.xml test

test-access-control:
	$(MVNW) -f access-control-service/pom.xml test

test-notifications:
	$(MVNW) -f notifications-service/pom.xml test

test-reporting:
	$(MVNW) -f reporting-service/pom.xml test



coverage-event-management:
	$(MVNW) -f event-management-service/pom.xml -Dtest='!*IntegrationTest' test jacoco:report

coverage-check-event-management:
	$(MVNW) -f event-management-service/pom.xml verify

coverage-payments:
	$(MVNW) -f payments-service/pom.xml -Dtest='!*IntegrationTest' test jacoco:report

coverage-check-payments:
	$(MVNW) -f payments-service/pom.xml verify

coverage-ticket-issuance:
	$(MVNW) -f ticket-issuance-service/pom.xml -Dtest='!*IntegrationTest' test jacoco:report

coverage-check-ticket-issuance:
	$(MVNW) -f ticket-issuance-service/pom.xml verify

compile: compile-event-management compile-seating-inventory compile-orders compile-payments compile-ticket-issuance compile-access-control compile-notifications compile-reporting

compile-existing: compile-event-management compile-seating-inventory compile-payments compile-ticket-issuance compile-access-control compile-notifications compile-reporting

compile-event-management:
	$(MVNW) -f event-management-service/pom.xml clean package -DskipTests

compile-seating-inventory:
	$(MVNW) -f seating-inventory-service/pom.xml clean package -DskipTests

compile-orders:
	$(MVNW) -f orders-service/pom.xml clean package -DskipTests

compile-payments:
	$(MVNW) -f payments-service/pom.xml clean package -DskipTests

compile-ticket-issuance:
	$(MVNW) -f ticket-issuance-service/pom.xml clean package -DskipTests

compile-access-control:
	$(MVNW) -f access-control-service/pom.xml clean package -DskipTests

compile-notifications:
	$(MVNW) -f notifications-service/pom.xml clean package -DskipTests

compile-reporting:
	$(MVNW) -f reporting-service/pom.xml clean package -DskipTests

docker-build:
	$(COMPOSE) build $(SERVICES)

docker-build-existing:
	$(COMPOSE_PROD) build $(PROD_SERVICES)

docker-up:
	$(COMPOSE) up -d

docker-up-build:
	$(COMPOSE) up -d --build

docker-up-critical:
	$(COMPOSE) up -d $(CRITICAL_SERVICES)

docker-up-rabbitmq:
	$(COMPOSE) up -d rabbitmq


docker-up-event-management:
	$(COMPOSE) up -d event-management-service

docker-up-seating-inventory:
	$(COMPOSE) up -d seating-inventory-service

docker-up-orders:
	$(COMPOSE) up -d orders-service

docker-up-payments:
	$(COMPOSE) up -d payments-service

docker-up-ticket-issuance:
	$(COMPOSE) up -d ticket-issuance-service

docker-up-access-control:
	$(COMPOSE_PROD) up -d access-control-service

docker-up-notifications:
	$(COMPOSE) up -d notifications-service

docker-up-reporting:
	$(COMPOSE_PROD) up -d reporting-service

docker-down:
	$(COMPOSE) down

docker-restart:
	$(COMPOSE) restart

docker-ps:
	$(COMPOSE) ps

logs:
	$(COMPOSE) logs -f

logs-rabbitmq:
	$(COMPOSE) logs -f rabbitmq


logs-event-management:
	$(COMPOSE) logs -f event-management-service

logs-seating-inventory:
	$(COMPOSE) logs -f seating-inventory-service

logs-orders:
	$(COMPOSE) logs -f orders-service

logs-payments:
	$(COMPOSE) logs -f payments-service

logs-ticket-issuance:
	$(COMPOSE) logs -f ticket-issuance-service

logs-access-control:
	$(COMPOSE_PROD) logs -f access-control-service

logs-notifications:
	$(COMPOSE) logs -f notifications-service

logs-reporting:
	$(COMPOSE_PROD) logs -f reporting-service

# ── Local (DB externa) ────────────────────────────────────────────────────

local-up:
	$(COMPOSE_LOCAL) up -d

local-up-build:
	$(COMPOSE_LOCAL) up -d --build

local-down:
	$(COMPOSE_LOCAL) down

local-restart:
	$(COMPOSE_LOCAL) restart

local-ps:
	$(COMPOSE_LOCAL) ps

local-logs:
	$(COMPOSE_LOCAL) logs -f

# ── Prod (DB y RabbitMQ externos) ────────────────────────────────────────

prod-up:
	$(COMPOSE_PROD) up -d

prod-up-build:
	$(COMPOSE_PROD) up -d --build

prod-down:
	$(COMPOSE_PROD) down

prod-restart:
	$(COMPOSE_PROD) restart

prod-ps:
	$(COMPOSE_PROD) ps

prod-logs:
	$(COMPOSE_PROD) logs -f

# ── Lab (DB externa por HAProxy, gateway externo) ────────────────────────

lab-up:
	$(COMPOSE_LAB) up -d

lab-up-build:
	$(COMPOSE_LAB) up -d --build

lab-down:
	$(COMPOSE_LAB) down

lab-restart:
	$(COMPOSE_LAB) restart

lab-ps:
	$(COMPOSE_LAB) ps

lab-logs:
	$(COMPOSE_LAB) logs -f
