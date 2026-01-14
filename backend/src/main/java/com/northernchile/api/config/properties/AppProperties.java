package com.northernchile.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Centralized application configuration properties.
 * General app settings including URLs, timezone, tax, and booking rules.
 */
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String baseUrl = "http://localhost:8080";
    private String frontendBaseUrl = "http://localhost:3000";
    private String timezone = "America/Santiago";
    private double taxRate = 0.19;
    private Booking booking = new Booking();
    private Manifest manifest = new Manifest();
    private Cookie cookie = new Cookie();
    private Security security = new Security();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getFrontendBaseUrl() {
        return frontendBaseUrl;
    }

    public void setFrontendBaseUrl(String frontendBaseUrl) {
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(double taxRate) {
        this.taxRate = taxRate;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Manifest getManifest() {
        return manifest;
    }

    public void setManifest(Manifest manifest) {
        this.manifest = manifest;
    }

    public Cookie getCookie() {
        return cookie;
    }

    public void setCookie(Cookie cookie) {
        this.cookie = cookie;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public static class Booking {
        private int minHoursBeforeTour = 2;

        public int getMinHoursBeforeTour() {
            return minHoursBeforeTour;
        }

        public void setMinHoursBeforeTour(int minHoursBeforeTour) {
            this.minHoursBeforeTour = minHoursBeforeTour;
        }
    }

    public static class Manifest {
        private String operatorEmail = "contacto@northernchile.com";
        private String emergencyContact = "+56 9 5765 5764";

        public String getOperatorEmail() {
            return operatorEmail;
        }

        public void setOperatorEmail(String operatorEmail) {
            this.operatorEmail = operatorEmail;
        }

        public String getEmergencyContact() {
            return emergencyContact;
        }

        public void setEmergencyContact(String emergencyContact) {
            this.emergencyContact = emergencyContact;
        }
    }

    public static class Cookie {
        private boolean insecure = false;
        private String domain = "";

        public boolean isInsecure() {
            return insecure;
        }

        public void setInsecure(boolean insecure) {
            this.insecure = insecure;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }
    }

    public static class Security {
        private List<String> allowedRedirectDomains = List.of(
            "http://localhost:3000",
            "https://www.northernchile.com",
            "https://northernchile.com"
        );

        public List<String> getAllowedRedirectDomains() {
            return allowedRedirectDomains;
        }

        public void setAllowedRedirectDomains(List<String> allowedRedirectDomains) {
            this.allowedRedirectDomains = allowedRedirectDomains;
        }
    }
}
