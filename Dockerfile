FROM eclipse-temurin:25-jdk-jammy
WORKDIR /app
COPY target/*.jar /app/app.jar

# Create a non-root user to run the application
RUN groupadd -r appuser -g 1001 && \
    useradd -r -u 1001 -g appuser appuser && \
    chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1
