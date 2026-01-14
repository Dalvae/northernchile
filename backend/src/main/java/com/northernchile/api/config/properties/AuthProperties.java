package com.northernchile.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Centralized authentication configuration properties.
 * Includes JWT settings and token expiration times.
 */
@Component
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    private Jwt jwt = new Jwt();
    private Token token = new Token();

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public static class Jwt {
        private String secret;
        private long expiration = 86400000; // 24 hours in ms

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getExpiration() {
            return expiration;
        }

        public void setExpiration(long expiration) {
            this.expiration = expiration;
        }
    }

    public static class Token {
        private Verification verification = new Verification();
        private PasswordReset passwordReset = new PasswordReset();

        public Verification getVerification() {
            return verification;
        }

        public void setVerification(Verification verification) {
            this.verification = verification;
        }

        public PasswordReset getPasswordReset() {
            return passwordReset;
        }

        public void setPasswordReset(PasswordReset passwordReset) {
            this.passwordReset = passwordReset;
        }

        public static class Verification {
            private int expirationHours = 24;

            public int getExpirationHours() {
                return expirationHours;
            }

            public void setExpirationHours(int expirationHours) {
                this.expirationHours = expirationHours;
            }
        }

        public static class PasswordReset {
            private int expirationHours = 2;

            public int getExpirationHours() {
                return expirationHours;
            }

            public void setExpirationHours(int expirationHours) {
                this.expirationHours = expirationHours;
            }
        }
    }
}
