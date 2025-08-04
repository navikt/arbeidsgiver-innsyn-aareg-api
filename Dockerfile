FROM gcr.io/distroless/java21-debian12
COPY /target/*.jar app.jar

# distroless does not pick up env set by webproxy: true (JAVA_PROXY_OPTIONS)
ENV JDK_JAVA_OPTIONS="$JAVA_PROXY_OPTIONS -XX:MaxRAMPercentage=75"
CMD ["app.jar"]
