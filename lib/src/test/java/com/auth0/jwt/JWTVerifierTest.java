package com.auth0.jwt;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Clock;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class JWTVerifierTest {
    private static final long DATE_TOKEN_MS_VALUE = 1477592 * 1000;
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldThrowWhenInitializedWithoutAlgorithm() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("The Algorithm cannot be null");
        JWTVerifier.init(null);
    }

    @Test
    public void shouldThrowWhenAlgorithmDoesntMatchTheTokensAlgorithm() {
        exception.expect(AlgorithmMismatchException.class);
        exception.expectMessage("The provided Algorithm doesn't match the one defined in the JWT's Header.");
        JWTVerifier verifier = JWTVerifier.init(Algorithm.HMAC512("secret")).build();
        verifier.verify("eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhdXRoMCJ9.s69x7Mmu4JqwmdxiK6sesALO7tcedbFsKEEITUxw9ho");
    }

    @Test
    public void shouldValidateIssuer() {
        String token = "eyJhbGciOiJIUzI1NiIsImN0eSI6IkpXVCJ9.eyJpc3MiOiJhdXRoMCJ9.mZ0m_N1J4PgeqWmi903JuUoDRZDBPB7HwkS4nVyWH1M";
        DecodedJWT jwt = JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withIssuer("auth0")
                .build()
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldValidateMultipleIssuers() {
        String auth0Token = "eyJhbGciOiJIUzI1NiIsImN0eSI6IkpXVCJ9.eyJpc3MiOiJhdXRoMCJ9.mZ0m_N1J4PgeqWmi903JuUoDRZDBPB7HwkS4nVyWH1M";
        String otherIssuertoken = "eyJhbGciOiJIUzI1NiIsImN0eSI6IkpXVCJ9.eyJpc3MiOiJvdGhlcklzc3VlciJ9.k4BCOJJl-c0_Y-49VD_mtt-u0QABKSV5i3W-RKc74co";
        JWTVerifier verifier = JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withIssuer("otherIssuer", "auth0")
                .build();

        assertThat(verifier.verify(auth0Token), is(notNullValue()));
        assertThat(verifier.verify(otherIssuertoken), is(notNullValue()));
    }

    @Test
    public void shouldThrowOnInvalidIssuer() {
        exception.expect(InvalidClaimException.class);
        exception.expectMessage("The Claim 'iss' value doesn't match the required issuer.");
        String token = "eyJhbGciOiJIUzI1NiIsImN0eSI6IkpXVCJ9.eyJpc3MiOiJhdXRoMCJ9.mZ0m_N1J4PgeqWmi903JuUoDRZDBPB7HwkS4nVyWH1M";
        JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withIssuer("invalid")
                .build()
                .verify(token);
    }

    @Test
    public void shouldThrowOnNullIssuer() {
        exception.expect(InvalidClaimException.class);
        exception.expectMessage("The Claim 'iss' value doesn't match the required issuer.");

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.e30.t-IDcSemACt8x4iTMCda8Yhe3iZaWbvV5XKSTbuAn0M";
        JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withIssuer("auth0")
                .build()
                .verify(token);
    }

    @Test
    public void shouldValidateSubject() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.Rq8IxqeX7eA6GgYxlcHdPFVRNFFZc5rEI3MQTZZbK3I";
        DecodedJWT jwt = JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withSubject("1234567890")
                .build()
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldThrowOnInvalidSubject() {
        exception.expect(InvalidClaimException.class);
        exception.expectMessage("The Claim 'sub' value doesn't match the required one.");
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.Rq8IxqeX7eA6GgYxlcHdPFVRNFFZc5rEI3MQTZZbK3I";
        JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withSubject("invalid")
                .build()
                .verify(token);
    }

    @Test
    public void shouldValidateAudience() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJNYXJrIn0.xWB6czYI0XObbVhLAxe55TwChWZg7zO08RxONWU2iY4";
        DecodedJWT jwt = JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withAudience("Mark")
                .build()
                .verify(token);

        assertThat(jwt, is(notNullValue()));

        String tokenArr = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsiTWFyayIsIkRhdmlkIl19.6WfbIt8m61f9WlCYIQn5CThvw4UNyC66qrPaoinfssw";
        DecodedJWT jwtArr = JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withAudience("Mark", "David")
                .build()
                .verify(tokenArr);

        assertThat(jwtArr, is(notNullValue()));
    }

    @Test
    public void shouldAcceptPartialAudience() {
        //Token 'aud' = ["Mark", "David", "John"]
        String tokenArr = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsiTWFyayIsIkRhdmlkIiwiSm9obiJdfQ.DX5xXiCaYvr54x_iL0LZsJhK7O6HhAdHeDYkgDeb0Rw";
        DecodedJWT jwtArr = JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withAudience("John")
                .build()
                .verify(tokenArr);

        assertThat(jwtArr, is(notNullValue()));
    }

    @Test
    public void shouldThrowOnInvalidAudience() {
        exception.expect(InvalidClaimException.class);
        exception.expectMessage("The Claim 'aud' value doesn't contain the required audience.");
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.Rq8IxqeX7eA6GgYxlcHdPFVRNFFZc5rEI3MQTZZbK3I";
        JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withAudience("nope")
                .build()
                .verify(token);
    }

    @Test
    public void shouldRemoveAudienceWhenPassingNullReference() throws Exception {
        Algorithm algorithm = mock(Algorithm.class);
        JWTVerifier verifier = JWTVerifier.init(algorithm)
                .withAudience((String) null)
                .build();

        assertThat(verifier.claims, is(notNullValue()));
        assertThat(verifier.claims, not(hasKey("aud")));

        verifier = JWTVerifier.init(algorithm)
                .withAudience((String[]) null)
                .build();

        assertThat(verifier.claims, is(notNullValue()));
        assertThat(verifier.claims, not(hasKey("aud")));

        verifier = JWTVerifier.init(algorithm)
                .withAudience()
                .build();

        assertThat(verifier.claims, is(notNullValue()));
        assertThat(verifier.claims, not(hasKey("aud")));

        String emptyAud = "   ";
        verifier = JWTVerifier.init(algorithm)
                .withAudience(emptyAud)
                .build();

        assertThat(verifier.claims, is(notNullValue()));
        assertThat(verifier.claims, hasEntry("aud", Collections.singletonList(emptyAud)));
    }

    @Test
    public void shouldRemoveAudienceWhenPassingNull() throws Exception {
        Algorithm algorithm = mock(Algorithm.class);
        JWTVerifier verifier = JWTVerifier.init(algorithm)
                .withAudience("John")
                .withAudience((String) null)
                .build();

        assertThat(verifier.claims, is(notNullValue()));
        assertThat(verifier.claims, not(hasKey("aud")));

        verifier = JWTVerifier.init(algorithm)
                .withAudience("John")
                .withAudience((String[]) null)
                .build();

        assertThat(verifier.claims, is(notNullValue()));
        assertThat(verifier.claims, not(hasKey("aud")));
    }

    @Test
    public void shouldThrowOnNullCustomClaimName() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("The Custom Claim's name can't be null.");
        JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withClaim(null, "value");
    }

    @Test
    public void shouldThrowWhenExpectedArrayClaimIsMissing() throws Exception {
        exception.expect(InvalidClaimException.class);
        exception.expectMessage("The Claim 'missing' value doesn't match the required one.");
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcnJheSI6WzEsMiwzXX0.wKNFBcMdwIpdF9rXRxvexrzSM6umgSFqRO1WZj992YM";
        JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withArrayClaim("missing", 1, 2, 3)
                .build()
                .verify(token);
    }

    @Test
    public void shouldThrowWhenExpectedClaimIsMissing() throws Exception {
        exception.expect(InvalidClaimException.class);
        exception.expectMessage("The Claim 'missing' value doesn't match the required one.");
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjbGFpbSI6InRleHQifQ.aZ27Ze35VvTqxpaSIK5ZcnYHr4SrvANlUbDR8fw9qsQ";
        JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withClaim("missing", "text")
                .build()
                .verify(token);
    }

    @Test
    public void shouldThrowOnInvalidCustomClaimValueOfTypeString() throws Exception {
        exception.expect(InvalidClaimException.class);
        exception.expectMessage("The Claim 'name' value doesn't match the required one.");
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjpbInNvbWV0aGluZyJdfQ.3ENLez6tU_fG0SVFrGmISltZPiXLSHaz_dyn-XFTEGQ";
        JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withClaim("name", "value")
                .build()
                .verify(token);
    }

    @Test
    public void shouldThrowOnInvalidCustomClaimValueOfTypeInteger() {
        exception.expect(InvalidClaimException.class);
        exception.expectMessage("The Claim 'name' value doesn't match the required one.");
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjpbInNvbWV0aGluZyJdfQ.3ENLez6tU_fG0SVFrGmISltZPiXLSHaz_dyn-XFTEGQ";
        JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withClaim("name", 123)
                .build()
                .verify(token);
    }

    @Test
    public void shouldThrowOnInvalidCustomClaimValueOfTypeDouble() {
        exception.expect(InvalidClaimException.class);
        exception.expectMessage("The Claim 'name' value doesn't match the required one.");
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjpbInNvbWV0aGluZyJdfQ.3ENLez6tU_fG0SVFrGmISltZPiXLSHaz_dyn-XFTEGQ";
        JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withClaim("name", 23.45)
                .build()
                .verify(token);
    }

    @Test
    public void shouldThrowOnInvalidCustomClaimValueOfTypeBoolean() {
        exception.expect(InvalidClaimException.class);
        exception.expectMessage("The Claim 'name' value doesn't match the required one.");
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjpbInNvbWV0aGluZyJdfQ.3ENLez6tU_fG0SVFrGmISltZPiXLSHaz_dyn-XFTEGQ";
        JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withClaim("name", true)
                .build()
                .verify(token);
    }

    @Test
    public void shouldThrowOnInvalidCustomClaimValueOfTypeDate() {
        exception.expect(InvalidClaimException.class);
        exception.expectMessage("The Claim 'name' value doesn't match the required one.");
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjpbInNvbWV0aGluZyJdfQ.3ENLez6tU_fG0SVFrGmISltZPiXLSHaz_dyn-XFTEGQ";
        JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withClaim("name", new Date())
                .build()
                .verify(token);
    }

    @Test
    public void shouldThrowOnInvalidCustomClaimValueOfTypeInstant() {
        exception.expect(InvalidClaimException.class);
        exception.expectMessage("The Claim 'name' value doesn't match the required one.");
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjpbInNvbWV0aGluZyJdfQ.3ENLez6tU_fG0SVFrGmISltZPiXLSHaz_dyn-XFTEGQ";
        JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withClaim("name", Instant.now())
                .build()
                .verify(token);
    }

    @Test
    public void shouldThrowOnInvalidCustomClaimValue() {
        exception.expect(InvalidClaimException.class);
        exception.expectMessage("The Claim 'name' value doesn't match the required one.");
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjpbInNvbWV0aGluZyJdfQ.3ENLez6tU_fG0SVFrGmISltZPiXLSHaz_dyn-XFTEGQ";
        Map<String, Object> map = new HashMap<>();
        map.put("name", new Object());
        JWTVerifier verifier = new JWTVerifier(Algorithm.HMAC256("secret"), map, new ClockImpl());
        verifier.verify(token);
    }

    @Test
    public void shouldValidateCustomClaimOfTypeString() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoidmFsdWUifQ.Jki8pvw6KGbxpMinufrgo6RDL1cu7AtNMJYVh6t-_cE";
        DecodedJWT jwt = JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withClaim("name", "value")
                .build()
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldValidateCustomClaimOfTypeInteger() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoxMjN9.XZAudnA7h3_Al5kJydzLjw6RzZC3Q6OvnLEYlhNW7HA";
        DecodedJWT jwt = JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withClaim("name", 123)
                .build()
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldValidateCustomClaimOfTypeLong() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjo5MjIzMzcyMDM2ODU0Nzc2MDB9.km-IwQ5IDnTZFmuJzhSgvjTzGkn_Z5X29g4nAuVC56I";
        DecodedJWT jwt = JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withClaim("name", 922337203685477600L)
                .build()
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldValidateCustomClaimOfTypeDouble() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoyMy40NX0.7pyX2OmEGaU9q15T8bGFqRm-d3RVTYnqmZNZtxMKSlA";
        DecodedJWT jwt = JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withClaim("name", 23.45)
                .build()
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldValidateCustomClaimOfTypeBoolean() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjp0cnVlfQ.FwQ8VfsZNRqBa9PXMinSIQplfLU4-rkCLfIlTLg_MV0";
        DecodedJWT jwt = JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withClaim("name", true)
                .build()
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldValidateCustomClaimOfTypeDate() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoxNDc4ODkxNTIxfQ.mhioumeok8fghQEhTKF3QtQAksSvZ_9wIhJmgZLhJ6c";
        Instant instant = Instant.ofEpochMilli(1478891521000L);
        DecodedJWT jwt = JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withClaim("name", instant)
                .build()
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldValidateCustomArrayClaimOfTypeString() {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJuYW1lIjpbInRleHQiLCIxMjMiLCJ0cnVlIl19.lxM8EcmK1uSZRAPd0HUhXGZJdauRmZmLjoeqz4J9yAA";
        DecodedJWT jwt = JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withArrayClaim("name", "text", "123", "true")
                .build()
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldValidateCustomArrayClaimOfTypeInteger() {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJuYW1lIjpbMSwyLDNdfQ.UEuMKRQYrzKAiPpPLhIVawWkKWA1zj0_GderrWUIyFE";
        DecodedJWT jwt = JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withArrayClaim("name", 1, 2, 3)
                .build()
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldValidateCustomArrayClaimOfTypeLong() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJuYW1lIjpbNTAwMDAwMDAwMDAxLDUwMDAwMDAwMDAwMiw1MDAwMDAwMDAwMDNdfQ.vzV7S0gbV9ZAVxChuIt4XZuSVTxMH536rFmoHzxmayM";
        DecodedJWT jwt = JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withArrayClaim("name", 500000000001L, 500000000002L, 500000000003L)
                .build()
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldValidateCustomArrayClaimOfTypeLongWhenValueIsInteger() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJuYW1lIjpbMSwyLDNdfQ.UEuMKRQYrzKAiPpPLhIVawWkKWA1zj0_GderrWUIyFE";
        DecodedJWT jwt = JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withArrayClaim("name", 1L, 2L, 3L)
                .build()
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldValidateCustomArrayClaimOfTypeLongWhenValueIsIntegerAndLong() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJuYW1lIjpbMSw1MDAwMDAwMDAwMDIsNTAwMDAwMDAwMDAzXX0.PQjb2rPPpYjM2sItZEzZcjS2YbfPCp6xksTSPjpjTQA";
        DecodedJWT jwt = JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withArrayClaim("name", 1L, 500000000002L, 500000000003L)
                .build()
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    // Generic Delta
    @SuppressWarnings("RedundantCast")
    @Test
    public void shouldAddDefaultLeewayToDateClaims() {
        Algorithm algorithm = mock(Algorithm.class);
        JWTVerifier verifier = JWTVerifier.init(algorithm)
                .build();

        assertThat(verifier.claims, is(notNullValue()));
        assertThat(verifier.claims, hasEntry("iat", (Object) 0L));
        assertThat(verifier.claims, hasEntry("exp", (Object) 0L));
        assertThat(verifier.claims, hasEntry("nbf", (Object) 0L));
    }

    @SuppressWarnings("RedundantCast")
    @Test
    public void shouldAddCustomLeewayToDateClaims() {
        Algorithm algorithm = mock(Algorithm.class);
        JWTVerifier verifier = JWTVerifier.init(algorithm)
                .acceptLeeway(1234L)
                .build();

        assertThat(verifier.claims, is(notNullValue()));
        assertThat(verifier.claims, hasEntry("iat", (Object) 1234L));
        assertThat(verifier.claims, hasEntry("exp", (Object) 1234L));
        assertThat(verifier.claims, hasEntry("nbf", (Object) 1234L));
    }

    @SuppressWarnings("RedundantCast")
    @Test
    public void shouldOverrideDefaultIssuedAtLeeway() {
        Algorithm algorithm = mock(Algorithm.class);
        JWTVerifier verifier = JWTVerifier.init(algorithm)
                .acceptLeeway(1234L)
                .acceptIssuedAt(9999L)
                .build();

        assertThat(verifier.claims, is(notNullValue()));
        assertThat(verifier.claims, hasEntry("iat", (Object) 9999L));
        assertThat(verifier.claims, hasEntry("exp", (Object) 1234L));
        assertThat(verifier.claims, hasEntry("nbf", (Object) 1234L));
    }

    @SuppressWarnings("RedundantCast")
    @Test
    public void shouldOverrideDefaultExpiresAtLeeway() {
        Algorithm algorithm = mock(Algorithm.class);
        JWTVerifier verifier = JWTVerifier.init(algorithm)
                .acceptLeeway(1234L)
                .acceptExpiresAt(9999L)
                .build();

        assertThat(verifier.claims, is(notNullValue()));
        assertThat(verifier.claims, hasEntry("iat", (Object) 1234L));
        assertThat(verifier.claims, hasEntry("exp", (Object) 9999L));
        assertThat(verifier.claims, hasEntry("nbf", (Object) 1234L));
    }

    @SuppressWarnings("RedundantCast")
    @Test
    public void shouldOverrideDefaultNotBeforeLeeway() {
        Algorithm algorithm = mock(Algorithm.class);
        JWTVerifier verifier = JWTVerifier.init(algorithm)
                .acceptLeeway(1234L)
                .acceptNotBefore(9999L)
                .build();

        assertThat(verifier.claims, is(notNullValue()));
        assertThat(verifier.claims, hasEntry("iat", (Object) 1234L));
        assertThat(verifier.claims, hasEntry("exp", (Object) 1234L));
        assertThat(verifier.claims, hasEntry("nbf", (Object) 9999L));
    }

    @Test
    public void shouldThrowOnNegativeCustomLeeway() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Leeway value can't be negative.");
        Algorithm algorithm = mock(Algorithm.class);
        JWTVerifier.init(algorithm)
                .acceptLeeway(-1);
    }

    // Expires At
    @Test
    public void shouldValidateExpiresDateAtWithLeeway() {
        Clock clock = mock(Clock.class);
        when(clock.getNow()).thenReturn(Instant.ofEpochMilli(DATE_TOKEN_MS_VALUE + 1000));

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE0Nzc1OTJ9.isvT0Pqx0yjnZk53mUFSeYFJLDs-Ls9IsNAm86gIdZo";
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWTVerifier.init(Algorithm.HMAC256("secret"))
                .acceptExpiresAt(2);
        DecodedJWT jwt = verification
                .build(clock)
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }


    @Test
    public void shouldValidateExpiresInstantAtWithLeeway() {
        Clock clock = mock(Clock.class);
        when(clock.getNow()).thenReturn(Instant.ofEpochMilli(DATE_TOKEN_MS_VALUE + 1000));

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE0Nzc1OTJ9.isvT0Pqx0yjnZk53mUFSeYFJLDs-Ls9IsNAm86gIdZo";
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWTVerifier.init(Algorithm.HMAC256("secret"))
                .acceptExpiresAt(2);
        DecodedJWT jwt = verification
                .build(clock)
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldValidateExpiresAtDateIfPresent() {
        Clock clock = mock(Clock.class);
        when(clock.getNow()).thenReturn(Instant.ofEpochMilli(DATE_TOKEN_MS_VALUE));

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE0Nzc1OTJ9.isvT0Pqx0yjnZk53mUFSeYFJLDs-Ls9IsNAm86gIdZo";
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWTVerifier.init(Algorithm.HMAC256("secret"));
        DecodedJWT jwt = verification
                .build(clock)
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldValidateExpiresAtInstantIfPresent() {
        Clock clock = mock(Clock.class);
        when(clock.getNow()).thenReturn(Instant.ofEpochMilli(DATE_TOKEN_MS_VALUE));

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE0Nzc1OTJ9.isvT0Pqx0yjnZk53mUFSeYFJLDs-Ls9IsNAm86gIdZo";
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWTVerifier.init(Algorithm.HMAC256("secret"));
        DecodedJWT jwt = verification
                .build(clock)
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldThrowOnInvalidExpiresAtDateIfPresent() {
        exception.expect(TokenExpiredException.class);
        exception.expectMessage(startsWith("The Token has expired on"));
        Clock clock = mock(Clock.class);
        when(clock.getNow()).thenReturn(Instant.ofEpochMilli(DATE_TOKEN_MS_VALUE + 1000));

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE0Nzc1OTJ9.isvT0Pqx0yjnZk53mUFSeYFJLDs-Ls9IsNAm86gIdZo";
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWTVerifier.init(Algorithm.HMAC256("secret"));
        verification
                .build(clock)
                .verify(token);
    }

    @Test
    public void shouldThrowOnInvalidExpiresAtInstantIfPresent() {
        exception.expect(TokenExpiredException.class);
        exception.expectMessage(startsWith("The Token has expired on"));
        Clock clock = mock(Clock.class);
        when(clock.getNow()).thenReturn(Instant.ofEpochMilli(DATE_TOKEN_MS_VALUE + 1000));

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE0Nzc1OTJ9.isvT0Pqx0yjnZk53mUFSeYFJLDs-Ls9IsNAm86gIdZo";
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWTVerifier.init(Algorithm.HMAC256("secret"));
        verification
                .build(clock)
                .verify(token);
    }

    @Test
    public void shouldThrowOnNegativeExpiresAtLeeway() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Leeway value can't be negative.");
        Algorithm algorithm = mock(Algorithm.class);
        JWTVerifier.init(algorithm)
                .acceptExpiresAt(-1);
    }

    // Not before
    @Test
    public void shouldValidateNotBeforeDateWithLeeway() {
        Clock clock = mock(Clock.class);
        when(clock.getNow()).thenReturn(Instant.ofEpochMilli(DATE_TOKEN_MS_VALUE - 1000));

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYmYiOjE0Nzc1OTJ9.wq4ZmnSF2VOxcQBxPLfeh1J2Ozy1Tj5iUaERm3FKaw8";
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWTVerifier.init(Algorithm.HMAC256("secret"))
                .acceptNotBefore(2);
        DecodedJWT jwt = verification
                .build(clock)
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }


    @Test
    public void shouldValidateNotBeforeInstantWithLeeway() {
        Clock clock = mock(Clock.class);
        when(clock.getNow()).thenReturn(Instant.ofEpochMilli(DATE_TOKEN_MS_VALUE - 1000));

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYmYiOjE0Nzc1OTJ9.wq4ZmnSF2VOxcQBxPLfeh1J2Ozy1Tj5iUaERm3FKaw8";
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWTVerifier.init(Algorithm.HMAC256("secret"))
                .acceptNotBefore(2);
        DecodedJWT jwt = verification
                .build(clock)
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldThrowOnInvalidNotBeforeDateIfPresent() {
        exception.expect(InvalidClaimException.class);
        exception.expectMessage(startsWith("The Token can't be used before"));
        Clock clock = mock(Clock.class);
        when(clock.getNow()).thenReturn(Instant.ofEpochMilli(DATE_TOKEN_MS_VALUE - 1000));

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYmYiOjE0Nzc1OTJ9.wq4ZmnSF2VOxcQBxPLfeh1J2Ozy1Tj5iUaERm3FKaw8";
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWTVerifier.init(Algorithm.HMAC256("secret"));
        verification
                .build(clock)
                .verify(token);
    }

    @Test
    public void shouldThrowOnInvalidNotBeforeInstantIfPresent() {
        exception.expect(InvalidClaimException.class);
        exception.expectMessage(startsWith("The Token can't be used before"));
        Clock clock = mock(Clock.class);
        when(clock.getNow()).thenReturn(Instant.ofEpochMilli(DATE_TOKEN_MS_VALUE - 1000));

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYmYiOjE0Nzc1OTJ9.wq4ZmnSF2VOxcQBxPLfeh1J2Ozy1Tj5iUaERm3FKaw8";
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWTVerifier.init(Algorithm.HMAC256("secret"));
        verification
                .build(clock)
                .verify(token);
    }

    @Test
    public void shouldValidateNotBeforeDateIfPresent() {
        Clock clock = mock(Clock.class);
        when(clock.getNow()).thenReturn(Instant.ofEpochMilli(DATE_TOKEN_MS_VALUE));

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE0Nzc1OTJ9.isvT0Pqx0yjnZk53mUFSeYFJLDs-Ls9IsNAm86gIdZo";
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWTVerifier.init(Algorithm.HMAC256("secret"));
        DecodedJWT jwt = verification
                .build(clock)
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldValidateNotBeforeInstantIfPresent() {
        Clock clock = mock(Clock.class);
        when(clock.getNow()).thenReturn(Instant.ofEpochMilli(DATE_TOKEN_MS_VALUE));

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE0Nzc1OTJ9.isvT0Pqx0yjnZk53mUFSeYFJLDs-Ls9IsNAm86gIdZo";
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWTVerifier.init(Algorithm.HMAC256("secret"));
        DecodedJWT jwt = verification
                .build(clock)
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldThrowOnNegativeNotBeforeLeeway() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Leeway value can't be negative.");
        Algorithm algorithm = mock(Algorithm.class);
        JWTVerifier.init(algorithm)
                .acceptNotBefore(-1);
    }

    // Issued At with future date
    @Test
    public void shouldThrowOnFutureIssuedAtDate() {
        exception.expect(InvalidClaimException.class);
        exception.expectMessage("The Token can't be used before 1970-01-18T02:26:32Z.");

        Clock clock = mock(Clock.class);
        when(clock.getNow()).thenReturn(Instant.ofEpochMilli(DATE_TOKEN_MS_VALUE - 1000));

        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE0Nzc1OTJ9.CWq-6pUXl1bFg81vqOUZbZrheO2kUBd2Xr3FUZmvudE";
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWTVerifier.init(Algorithm.HMAC256("secret"));

        DecodedJWT jwt = verification.build(clock).verify(token);
        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldThrowOnFutureIssuedAtInstant() {
        exception.expect(InvalidClaimException.class);
        exception.expectMessage("The Token can't be used before 1970-01-18T02:26:32Z.");

        Clock clock = mock(Clock.class);
        when(clock.getNow()).thenReturn(Instant.ofEpochMilli(DATE_TOKEN_MS_VALUE - 1000));

        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE0Nzc1OTJ9.CWq-6pUXl1bFg81vqOUZbZrheO2kUBd2Xr3FUZmvudE";
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWTVerifier.init(Algorithm.HMAC256("secret"));

        DecodedJWT jwt = verification.build(clock).verify(token);
        assertThat(jwt, is(notNullValue()));
    }

    // Issued At with future date and ignore flag
    @Test
    public void shouldSkipIssuedAtDateVerificationWhenFlagIsPassed() {
        Clock clock = mock(Clock.class);
        when(clock.getNow()).thenReturn(Instant.ofEpochMilli(DATE_TOKEN_MS_VALUE - 1000));

        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE0Nzc1OTJ9.CWq-6pUXl1bFg81vqOUZbZrheO2kUBd2Xr3FUZmvudE";
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWTVerifier.init(Algorithm.HMAC256("secret"));
        verification.ignoreIssuedAt();

        DecodedJWT jwt = verification.build(clock).verify(token);
        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldSkipIssuedAtInstantVerificationWhenFlagIsPassed() {
        Clock clock = mock(Clock.class);
        when(clock.getNow()).thenReturn(Instant.ofEpochMilli(DATE_TOKEN_MS_VALUE - 1000));

        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE0Nzc1OTJ9.CWq-6pUXl1bFg81vqOUZbZrheO2kUBd2Xr3FUZmvudE";
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWTVerifier.init(Algorithm.HMAC256("secret"));
        verification.ignoreIssuedAt();

        DecodedJWT jwt = verification.build(clock).verify(token);
        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldThrowOnInvalidIssuedAtDateIfPresent() {
        exception.expect(InvalidClaimException.class);
        exception.expectMessage(startsWith("The Token can't be used before"));
        Clock clock = mock(Clock.class);
        when(clock.getNow()).thenReturn(Instant.ofEpochMilli(DATE_TOKEN_MS_VALUE - 1000));

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE0Nzc1OTJ9.0WJky9eLN7kuxLyZlmbcXRL3Wy8hLoNCEk5CCl2M4lo";
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWTVerifier.init(Algorithm.HMAC256("secret"));
        verification
                .build(clock)
                .verify(token);
    }

    @Test
    public void shouldThrowOnInvalidIssuedAtInstantIfPresent() {
        exception.expect(InvalidClaimException.class);
        exception.expectMessage(startsWith("The Token can't be used before"));
        Clock clock = mock(Clock.class);
        when(clock.getNow()).thenReturn(Instant.ofEpochMilli(DATE_TOKEN_MS_VALUE - 1000));

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE0Nzc1OTJ9.0WJky9eLN7kuxLyZlmbcXRL3Wy8hLoNCEk5CCl2M4lo";
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWTVerifier.init(Algorithm.HMAC256("secret"));
        verification
                .build(clock)
                .verify(token);
    }

    @Test
    public void shouldOverrideAcceptIssuedAtWhenIgnoreIssuedAtDateFlagPassedAndSkipTheVerification() {
        Clock clock = mock(Clock.class);
        when(clock.getNow()).thenReturn(Instant.ofEpochMilli(DATE_TOKEN_MS_VALUE - 1000));

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE0Nzc1OTJ9.0WJky9eLN7kuxLyZlmbcXRL3Wy8hLoNCEk5CCl2M4lo";
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWTVerifier.init(Algorithm.HMAC256("secret"));
        DecodedJWT jwt = verification.acceptIssuedAt(20).ignoreIssuedAt()
                .build()
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldOverrideAcceptIssuedAtWhenIgnoreIssuedAtInstantFlagPassedAndSkipTheVerification() {
        Clock clock = mock(Clock.class);
        when(clock.getNow()).thenReturn(Instant.ofEpochMilli(DATE_TOKEN_MS_VALUE - 1000));

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE0Nzc1OTJ9.0WJky9eLN7kuxLyZlmbcXRL3Wy8hLoNCEk5CCl2M4lo";
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWTVerifier.init(Algorithm.HMAC256("secret"));
        DecodedJWT jwt = verification.acceptIssuedAt(20).ignoreIssuedAt()
                .build()
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldValidateIssuedAtDateIfPresent() {
        Clock clock = mock(Clock.class);
        when(clock.getNow()).thenReturn(Instant.ofEpochMilli(DATE_TOKEN_MS_VALUE));

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE0Nzc1OTJ9.0WJky9eLN7kuxLyZlmbcXRL3Wy8hLoNCEk5CCl2M4lo";
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWTVerifier.init(Algorithm.HMAC256("secret"));
        DecodedJWT jwt = verification
                .build(clock)
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldValidateIssuedAtInstantIfPresent() {
        Clock clock = mock(Clock.class);
        when(clock.getNow()).thenReturn(Instant.ofEpochMilli(DATE_TOKEN_MS_VALUE));

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE0Nzc1OTJ9.0WJky9eLN7kuxLyZlmbcXRL3Wy8hLoNCEk5CCl2M4lo";
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWTVerifier.init(Algorithm.HMAC256("secret"));
        DecodedJWT jwt = verification
                .build(clock)
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldThrowOnNegativeIssuedAtLeeway() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Leeway value can't be negative.");
        Algorithm algorithm = mock(Algorithm.class);
        JWTVerifier.init(algorithm)
                .acceptIssuedAt(-1);
    }

    @Test
    public void shouldValidateJWTId() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJqd3RfaWRfMTIzIn0.0kegfXUvwOYioP8PDaLMY1IlV8HOAzSVz3EGL7-jWF4";
        DecodedJWT jwt = JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withJWTId("jwt_id_123")
                .build()
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }

    @Test
    public void shouldThrowOnInvalidJWTId() {
        exception.expect(InvalidClaimException.class);
        exception.expectMessage("The Claim 'jti' value doesn't match the required one.");
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJqd3RfaWRfMTIzIn0.0kegfXUvwOYioP8PDaLMY1IlV8HOAzSVz3EGL7-jWF4";
        JWTVerifier.init(Algorithm.HMAC256("secret"))
                .withJWTId("invalid")
                .build()
                .verify(token);
    }

    @Test
    public void shouldRemoveClaimWhenPassingNull() {
        Algorithm algorithm = mock(Algorithm.class);
        JWTVerifier verifier = JWTVerifier.init(algorithm)
                .withIssuer("iss")
                .withIssuer((String) null)
                .build();

        assertThat(verifier.claims, is(notNullValue()));
        assertThat(verifier.claims, not(hasKey("iss")));

        verifier = JWTVerifier.init(algorithm)
                .withIssuer("iss")
                .withIssuer((String[]) null)
                .build();

        assertThat(verifier.claims, is(notNullValue()));
        assertThat(verifier.claims, not(hasKey("iss")));
    }

    @Test
    public void shouldRemoveIssuerWhenPassingNullReference() throws Exception {
        Algorithm algorithm = mock(Algorithm.class);
        JWTVerifier verifier = JWTVerifier.init(algorithm)
                .withIssuer((String) null)
                .build();

        assertThat(verifier.claims, is(notNullValue()));
        assertThat(verifier.claims, not(hasKey("iss")));

        verifier = JWTVerifier.init(algorithm)
                .withIssuer((String[]) null)
                .build();

        assertThat(verifier.claims, is(notNullValue()));
        assertThat(verifier.claims, not(hasKey("iss")));

        verifier = JWTVerifier.init(algorithm)
                .withIssuer()
                .build();

        assertThat(verifier.claims, is(notNullValue()));
        assertThat(verifier.claims, not(hasKey("iss")));

        String emptyIss = "  ";
        verifier = JWTVerifier.init(algorithm)
                .withIssuer(emptyIss)
                .build();

        assertThat(verifier.claims, is(notNullValue()));
        assertThat(verifier.claims, hasEntry("iss", Collections.singletonList(emptyIss)));
    }

    @Test
    public void shouldSkipClaimValidationsIfNoClaimsRequired() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.e30.t-IDcSemACt8x4iTMCda8Yhe3iZaWbvV5XKSTbuAn0M";
        DecodedJWT jwt = JWTVerifier.init(Algorithm.HMAC256("secret"))
                .build()
                .verify(token);

        assertThat(jwt, is(notNullValue()));
    }
}
