package org.clematis.auth;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Anton Troshin
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpringBootIntegrationTest {

    private static final String SERVICE_INFO_URL = "/auth";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void whenLoadApplication_thenSuccess() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<?> clematisDomain = testRestTemplate
                .exchange(SERVICE_INFO_URL, HttpMethod.GET, new HttpEntity<String>(headers), String.class);
        Assertions.assertEquals(HttpStatus.SEE_OTHER, clematisDomain.getStatusCode());

    }

}
