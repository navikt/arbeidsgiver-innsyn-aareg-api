FROM navikt/java:11
COPY export-vault-secrets.sh /init-scripts/
COPY /target/innsynAaregApi-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080