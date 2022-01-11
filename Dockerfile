FROM navikt/java:17
COPY export-vault-secrets.sh /init-scripts/
COPY /target/innsyn-aareg-api-0.0.1-SNAPSHOT.jar app.jar
