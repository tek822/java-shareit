FROM amazoncorretto:11-alpine-jdk
#RUN apk add --no-cache alpine-conf && setup-timezone -z Europe/Moscow
ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:9091
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
