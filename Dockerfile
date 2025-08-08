FROM eclipse-temurin:21-jre
WORKDIR /app
COPY /target/*.jar app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_PROXY_OPTIONS -XX:MaxRAMPercentage=75 -jar /app/app.jar"]
