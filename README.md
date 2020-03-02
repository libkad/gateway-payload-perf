### Purpose
To demonstrate response time degradation with payload and TLS enabled.

### How
Deploy Spring Cloud Gateway that will route requests to a spring boot microservice.
Gatling will drive the load, measure the latency and plot the graphs.
All components are running on the same host.
- Spring Boot 2.2.4
- Spring Cloud (Hoxton.SR2)
- Netty 4.1.45
- Spring Cloud Gateway 2.2.1.

### Results
The test runs 4 scenarios. Calling microservice directly with/out payload and calling
it via. gateway with/out payload.
The payload used as request body was 100k JSON.

__Summary__

| | response time [ms] |
|----------|:-------------:|
| Direct with Payload | 6 | 
| Direct no Payload | 2 |
| Gateway with payload | 11 |
| Gateway no payload | 3 |

Gateway's response time with payload: 11 - 6 = 5ms  
Gateway's response time without payload: 3 - 2 = 1ms  

What we also noticed is that if we disable TLS then there isn't any degradation.

### How To Run
From root directory ./test.sh  
The results can be found in gatling-test/target/gatling/
