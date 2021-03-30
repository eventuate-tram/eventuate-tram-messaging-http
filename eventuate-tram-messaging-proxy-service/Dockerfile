FROM openjdk:8u171-jre-alpine
RUN apk --no-cache add curl
HEALTHCHECK --start-period=30s --interval=5s CMD curl -f http://localhost:8080/actuator/health || exit 1
CMD java ${JAVA_OPTS} -jar proxy.jar
ARG proxyServiceVersion
COPY build/libs/eventuate-tram-messaging-proxy-service-${proxyServiceVersion}.jar ./proxy.jar
