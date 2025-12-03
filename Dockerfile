FROM eclipse-temurin:25-jdk-jammy
WORKDIR /app
COPY target/*.jar /app/app.jar
VOLUME /data
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
