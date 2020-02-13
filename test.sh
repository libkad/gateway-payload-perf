#!/bin/bash
mvn clean package

cd restapp && mvn spring-boot:run &
cd gateway && mvn spring-boot:run &
sleep 20
(
  cd gatling-test
  mvn clean package
  GATLING_CMD="mvn gatling:test -Dpayload=100KPayload -Dnusers=100 -Dgatling.http.ssl.trustStore.password=changeit -Dgatling.http.ssl.trustStore.file=truststore.jks -Dgatling.http.ssl.trustStore.type=JKS"
  ${GATLING_CMD} -Dgatling.simulationClass=gateway.DirectSimulation || true
  ${GATLING_CMD} -Dgatling.simulationClass=gateway.DirectPayloadSimulation || true
  ${GATLING_CMD} -Dgatling.simulationClass=gateway.GatewaySimulation || true
  ${GATLING_CMD} -Dgatling.simulationClass=gateway.GatewayPayloadSimulation || true
)
curl -k -X POST https://localhost:7443/actuator/shutdown
curl -k -X POST https://localhost:9443/actuator/shutdown
