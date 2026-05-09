# =============================================================================
# MAVEN
# =============================================================================

.PHONY: compile build test test-class test-method verify package clean run

compile: ## Compile source code only
	./mvnw compile

build: ## Full build (compile + test + package)
	./mvnw clean install

test: ## Run all tests
	./mvnw test

test-class: ## Run a single test class.  Usage: make test-class CLASS=CreateAccountServiceUT
	./mvnw test -Dtest=$(CLASS)

test-method: ## Run a single test method. Usage: make test-method CLASS=CreateAccountServiceUT METHOD=create_shouldReturnSavedAccount
	./mvnw test -Dtest=$(CLASS)#$(METHOD)

verify: ## Full verification including integration tests (Testcontainers)
	./mvnw verify

package: ## Package without running tests
	./mvnw clean package -DskipTests

clean: ## Clean build artifacts
	./mvnw clean

run: ## Run locally with Spring Boot dev tools (hot reload)
	./mvnw spring-boot:run