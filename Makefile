CONTRACTS_URL ?= https://github.com/Frobama/beacon-contracts.git
CONTRACTS_TAG ?= v0.1.0

.PHONY: proto build test run docker

proto: ## Genera los stubs desde beacon-contracts
	buf generate "$(CONTRACTS_URL)#tag=$(CONTRACTS_TAG)" --template buf.gen.yaml

build: ## Compila (requiere make proto antes)
	mvn -B -DskipTests package

test: ## Corre los tests
	mvn -B test

run: ## Ejecuta el servicio en local (necesita la base de datos de beacon-infra)
	mvn -B spring-boot:run

docker: ## Construye la imagen (requiere make proto antes)
	docker build -t beacon-incidents:dev .
