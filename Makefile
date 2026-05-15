SHELL := /bin/sh

MVNW := ./mvnw

.PHONY: help test test-existing test-identity test-event-management test-seating-inventory test-orders test-payments test-ticket-issuance test-access-control test-notifications test-reporting test-gateway

help:
	@echo "OrionTicket test commands"
	@echo ""
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
