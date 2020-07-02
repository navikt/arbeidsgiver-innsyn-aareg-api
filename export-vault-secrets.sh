#!/usr/bin/env bash

if test -f /var/run/secrets/nais.io/srvag-arbforhold/password;
then
    export  STS_PASS=$(cat /var/run/secrets/nais.io/srvag-arbforhold/password)
    echo "Setting SYSTEMBRUKER_STS_PASS"
fi
