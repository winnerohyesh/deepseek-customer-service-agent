spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/customer_agent
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

logging:
  level:
    root: INFO
    com.winnerohyesh.customeragent: INFO

server:
  port: ${SERVER_PORT:8080}
