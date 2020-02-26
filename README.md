### Purpose
To demonstrate response time degradation request's payload overhead when TLS enabled.

### How
Deploy Spring Cloud Gateway that will route requests to a spring boot microservice. Gatling will drive the load, measure the latency and plot the graphs. All componets are running on the same host.
- Spring Boot 2.2.4
- Spring Cloud (Hoxton.SR1)
- Netty 4.1.45
- Spring Cloud Gateway 2.2.1.

### Results
The test runs 4 scenarios. Calling microservice directly with/out payload and calling it via. gateway with/out payload.
The payload used was 100k JSON.

__Summary__

| | response time [ms] |
|----------|:-------------:|
| Direct with Payload | 5 | 
| Direct no Payload | 2 |
| Gateway with payload | 11 |
| Gateway no payload | 3 |

### To Run
From root directory ./test.sh 
The results can be found in gatling-test/target/gatling/
