package no.nav.tag.innsynAareg.services.sts;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.innsynAareg.InnsynAaregApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static no.nav.tag.innsynAareg.services.sts.StsCacheConfig.STS_CACHE;


@Slf4j
@Component
class STSClient {

    val RestTemplate restTemplate;
    val HttpEntity<String> requestEntity;
    val String uriString;

    @Autowired
    public STSClient(@Value("${sts.stsPass}") String stsPass,
    @Value("${sts.stsUrl}") String stsUrl,
    RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.requestEntity = getRequestEntity(stsPass);
        this.uriString = buildUriString(stsUrl);
    }

    private HttpEntity<String> getRequestEntity(String stsPass) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(InnsynAaregApplication.APP_NAME, stsPass);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return new HttpEntity<>(headers);
    }

    private String buildUriString(String stsUrl) {
        return UriComponentsBuilder.fromHttpUrl(stsUrl)
                .queryParam("grant_type","client_credentials")
                .queryParam("scope","openid")
                .toUriString();
    }

    @Cacheable(STS_CACHE)
    public STStoken getToken() {
        try {
            ResponseEntity<STStoken> response = restTemplate.exchange(uriString, HttpMethod.GET, requestEntity, STStoken.class);
            if(response.getStatusCode() != HttpStatus.OK){
                String message = "Kall mot STS feiler med HTTP-" + response.getStatusCode();
                log.error(message);
                throw new RuntimeException(message);
            }
            return (response.getBody());
        }
        catch(HttpClientErrorException e){
            log.error("Feil ved oppslag i STS", e);
            throw new RuntimeException(e);
        }
    }

    @CacheEvict(STS_CACHE)
    public void evict() {
    }

}
