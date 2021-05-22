package org.clematis.keycloak;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import lombok.extern.slf4j.Slf4j;

import io.restassured.RestAssured;
import io.restassured.response.Response;
/**
 * @author Anton Troshin
 */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpringBootIntegrationTest {

    public final static String AUTH_SERVER = "/auth/realms/clematis/protocol/openid-connect";

    static final Logger LOG = LoggerFactory.getLogger(SpringBootIntegrationTest.class);

    private static final String SERVICE_INFO_URL = "/auth";

    private final static String REDIRECT_URL = "http://localhost:8082/test-client/login/oauth2/code/custom";

    private final static String CLIENT_ID = "testClient";
    private final static String CLIENT_SECRET = "fdd76d92-ab54-415f-99fe-72c43a0ac7b4";

    private final static String USERNAME = "john@test.com";
    private final static String PASSWORD = "123";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    int port;

    @BeforeEach
    @SuppressFBWarnings
    public void setUp() {
        RestAssured.port = port;
    }

    @Test
    public void whenLoadApplication_thenSuccess() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<?> clematisDomain = testRestTemplate
                .exchange(SERVICE_INFO_URL, HttpMethod.GET, new HttpEntity<String>(headers), String.class);
        Assertions.assertEquals(HttpStatus.SEE_OTHER, clematisDomain.getStatusCode());

    }

    @Test
    public void passwordFlowTest() {
        final Map<String, String> params = new HashMap<>();
        params.put("grant_type", "password");
        params.put("client_id", CLIENT_ID);
        params.put("username", USERNAME);
        params.put("password", PASSWORD);
        params.put("scope", "openid");
        final Response response = RestAssured
                .given()
                .auth()
                .preemptive()
                .basic(CLIENT_ID, CLIENT_SECRET)
                .and()
                .with()
                .params(params)
                .when()
                .post(AUTH_SERVER + "/token");
        String body = response.getBody().print();
        assertNotNull(body);
        String token = response.jsonPath().getString("access_token");
        assertNotNull(token);
    }

    @Test
    public void authorizationCodeFlowTest() {
        String authorizeUrl = AUTH_SERVER + SERVICE_INFO_URL;
        String tokenUrl = AUTH_SERVER + "/token";

        Map<String, String> loginParams = new HashMap<>();
        loginParams.put("client_id", CLIENT_ID);
        loginParams.put("response_type", "code");
        loginParams.put("redirect_uri", REDIRECT_URL);
        loginParams.put("scope", "openid");

        // user login
        Response response = RestAssured
                .given()
                .formParams(loginParams)
                .get(authorizeUrl);

        String cookieValue = response.getCookie("AUTH_SESSION_ID");
        LOG.info(response.getBody().print());

        assertNotEquals("", cookieValue);
        assertNotNull(cookieValue);
        assertTrue(cookieValue.contains("."));
        assertDoesNotThrow(() -> UUID.fromString(cookieValue.substring(0, cookieValue.indexOf("."))));
        assertTrue(response.getBody().print().contains("Sign in to your account"));

        String authUrlWithCode = response.htmlPath().getString("'**'.find{node -> node.name()=='form'}*.@action");
        assertNotNull(authUrlWithCode);

       // get code
        Map<String, String> codeParams = new HashMap<>();
        codeParams.put("username", USERNAME);
        codeParams.put("password", PASSWORD);
        response = RestAssured
                .given()
                .cookie("AUTH_SESSION_ID", cookieValue)
                .formParams(codeParams)
                .post(authUrlWithCode);

        final String location = response.getHeader(HttpHeaders.LOCATION);

        assertEquals(HttpStatus.FOUND.value(), response.getStatusCode());
        final String code = location.split("#|=|&")[3];

        //get access token
        Map<String, String> tokenParams = new HashMap<>();
        tokenParams.put("grant_type", "authorization_code");
        tokenParams.put("client_id", CLIENT_ID);
        tokenParams.put("client_secret", CLIENT_SECRET);
        tokenParams.put("redirect_uri", REDIRECT_URL);
        tokenParams.put("code", code);

        response = RestAssured.given().formParams(tokenParams).post(tokenUrl);

        String token = response.jsonPath().getString("access_token");
        assertNotNull(token);
    }

    @Test
    public void implicitFlowTest() {
        String authorizeUrl = AUTH_SERVER + SERVICE_INFO_URL;

        Map<String, String> loginParams = new HashMap<>();
        loginParams.put("grant_type", "implicit");
        loginParams.put("client_id", CLIENT_ID);
        loginParams.put("response_type", "token");
        loginParams.put("redirect_uri", REDIRECT_URL);
        loginParams.put("scope", "openid");

        // user login
        Response response = RestAssured
                .given()
                .formParams(loginParams)
                .with()
                .param("nonce", "325qjlalf09230")
                .get(authorizeUrl);

        String cookieValue = response.getCookie("AUTH_SESSION_ID");
        LOG.info(response.getBody().print());

        String authUrlWithCode = response.htmlPath().getString("'**'.find{node -> node.name()=='form'}*.@action");
        assertNotNull(authUrlWithCode);

        // get access token
        Map<String, String> tokenParams = new HashMap<>();
        tokenParams.put("username", USERNAME);
        tokenParams.put("password", PASSWORD);
        tokenParams.put("client_id", CLIENT_ID);
        tokenParams.put("redirect_uri", REDIRECT_URL);
        response = RestAssured.given().cookie("AUTH_SESSION_ID", cookieValue).formParams(tokenParams)
                .post(authUrlWithCode);

        final String location = response.getHeader(HttpHeaders.LOCATION);

        assertEquals(HttpStatus.FOUND.value(), response.getStatusCode());
        final String accessToken = location.split("#|=|&")[4];
        assertNotNull(accessToken);
    }
}
