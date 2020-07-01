#!/usr/bin/env bash

if test -f /var/run/secrets/nais.io/service_user/password;
then
    export  STS_PASS=$(cat /var/run/secrets/nais.io/srvAG-Arbforhold/password)
    echo "Setting SYSTEMBRUKER_STS_PASS"
fi
