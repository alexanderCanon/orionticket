SHELL := /bin/sh

MVNW     := ./mvnw
COMPOSE  := docker compose
COMPOSE_LOCAL := docker compose -f docker-compose.local.yml --env-file .env.local
COMPOSE_PROD  := docker compose -f docker-compose.prod.yml  --env-file .env.prod

SERVICES := identity-service event-management-service seating-inventory-service orders-service payments-service ticket-issuance-service access-control-service notifications-service reporting-service gateway-service
EXISTING_SERVICES := identity-service event-management-service seating-inventory-service payments-service ticket-issuance-service access-control-service notifications-service reporting-service gateway-service
CRITICAL_SERVICES := rabbitmq identity-service event-management-service seating-inventory-service orders-service payments-service ticket-issuance-service gateway-service

.PHONY: help \
	test test-existing test-identity test-event-management test-seating-inventory test-orders test-payments test-ticket-issuance test-access-control test-notifications test-reporting test-gateway \
	coverage-identity coverage-check-identity coverage-event-management coverage-check-event-management coverage-payments coverage-check-payments coverage-ticket-issuance coverage-check-ticket-issuance \
	compile compile-existing compile-identity compile-event-management compile-seating-inventory compile-orders compile-payments compile-ticket-issuance compile-access-control compile-notifications compile-reporting compile-gateway \
	docker-build docker-build-existing docker-up docker-up-build docker-up-critical docker-up-gateway docker-up-identity docker-up-event-management docker-up-seating-inventory docker-up-orders docker-up-payments docker-up-ticket-issuance docker-up-access-control docker-up-notifications docker-up-reporting docker-up-rabbitmq \
	docker-down docker-restart docker-ps logs logs-gateway logs-identity logs-event-management logs-seating-inventory logs-orders logs-payments logs-ticket-issuance logs-access-control logs-notifications logs-reporting logs-rabbitmq \
	local-up local-up-build local-down local-restart local-ps local-logs \
	prod-up prod-up-build prod-down prod-restart prod-ps prod-logs

help:
	@echo "OrionTicket commands"
	@echo ""
	@echo "Tests"
	@echo "  make test                   Run tests for all Maven services"
	@echo "  make test-existing          Run tests for services currently present in the repo"
	@echo "  make test-identity"
	@echo "  make test-event-management"
	@echo "  make test-seating-inventory"
	@echo "  make test-orders"
	@echo "  make test-payments"
	@echo "  make test-ticket-issuance"
	@echo "  make test-access-control"
	@echo "  make test-notifications"
	@echo "  make test-reporting"
	@echo "  make test-gateway"
	@echo ""
	@echo "Coverage"
	@echo "  make coverage-identity        Generate the JaCoCo report for identity-service unit tests"
	@echo "  make coverage-check-identity  Run full identity-service verification and enforce 70% focused line coverage"
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
	@echo "Docker (full stack con DBs embebidas — docker-compose.yml)"
	@echo "  make docker-build           Build Docker images for all compose services"
	@echo "  make docker-build-existing  Build Docker images for services currently present in the repo"
	@echo "  make docker-up              Start all compose services"
	@echo "  make docker-up-build        Build and start all compose services"
	@echo "  make docker-up-critical     Start critical purchase-flow services"
	@echo "  make docker-down            Stop and remove compose services"
	@echo "  make docker-ps              Show compose service status"
	@echo "  make logs                   Follow logs for all services"
	@echo "  make logs-payments          Follow logs for payments-service"
	@echo ""
	@echo "Local (DB externa, docker-compose.local.yml + .env.local)"
	@echo "  make local-up               Levantar RabbitMQ y todos los servicios (DB externa)"
	@echo "  make local-up-build         Build + levantar (local)"
	@echo "  make local-down             Detener y eliminar contenedores locales"
	@echo "  make local-restart          Reiniciar todos los servicios locales"
	@echo "  make local-ps               Estado de los servicios locales"
	@echo "  make local-logs             Seguir logs de todos los servicios locales"
	@echo ""
	@echo "Prod (DB y RabbitMQ externos, docker-compose.prod.yml + .env.prod)"
	@echo "  make prod-up                Levantar todos los servicios en producción"
	@echo "  make prod-up-build          Build + levantar (prod)"
	@echo "  make prod-down              Detener y eliminar contenedores de producción"
	@echo "  make prod-restart           Reiniciar todos los servicios de producción"
	@echo "  make prod-ps                Estado de los servicios de producción"
	@echo "  make prod-logs              Seguir logs de todos los servicios de producción"

test: test-identity test-event-management test-seating-inventory test-orders test-payments test-ticket-issuance test-access-control test-notifications test-reporting test-gateway

test-existing: test-identity test-event-management test-seating-inventory test-payments test-ticket-issuance test-access-control test-notifications test-reporting test-gateway

test-identity:
	$(MVNW) -f identity-service/pom.xml test

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

test-gateway:
	$(MVNW) -f gateway-service/pom.xml test

coverage-identity:
	$(MVNW) -f identity-service/pom.xml -Dtest='!*IntegrationTest' test jacoco:report

coverage-check-identity:
	$(MVNW) -f identity-service/pom.xml verify

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

compile: compile-identity compile-event-management compile-seating-inventory compile-orders compile-payments compile-ticket-issuance compile-access-control compile-notifications compile-reporting compile-gateway

compile-existing: compile-identity compile-event-management compile-seating-inventory compile-payments compile-ticket-issuance compile-access-control compile-notifications compile-reporting compile-gateway

compile-identity:
	$(MVNW) -f identity-service/pom.xml clean package -DskipTests

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

compile-gateway:
	$(MVNW) -f gateway-service/pom.xml clean package -DskipTests

docker-build:
	$(COMPOSE) build $(SERVICES)

docker-build-existing:
	$(COMPOSE) build $(EXISTING_SERVICES)

docker-up:
	$(COMPOSE) up -d

docker-up-build:
	$(COMPOSE) up -d --build

docker-up-critical:
	$(COMPOSE) up -d $(CRITICAL_SERVICES)

docker-up-rabbitmq:
	$(COMPOSE) up -d rabbitmq

docker-up-gateway:
	$(COMPOSE) up -d gateway-service

docker-up-identity:
	$(COMPOSE) up -d identity-service

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
	$(COMPOSE) up -d access-control-service

docker-up-notifications:
	$(COMPOSE) up -d notifications-service

docker-up-reporting:
	$(COMPOSE) up -d reporting-service

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

logs-gateway:
	$(COMPOSE) logs -f gateway-service

logs-identity:
	$(COMPOSE) logs -f identity-service

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
	$(COMPOSE) logs -f access-control-service

logs-notifications:
	$(COMPOSE) logs -f notifications-service

logs-reporting:
	$(COMPOSE) logs -f reporting-service

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
