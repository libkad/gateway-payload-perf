#!/bin/bash
mvn clean install

cd restapp && mvn spring-boot:run &
cd gateway && mvn spring-boot:run &
sleep 20
cd gatling-test && mvn gatling:test -Dgatling.simulationClass=gateway.GatewaySimulation -Dpayload=100KPayload -Dnusers=300
cd ..
curl -X POST http://localhost:8080/actuator/shutdown
curl -X POST http://localhost:9090/actuator/shutdown


