package com.northernchile.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Centralized mail configuration properties.
 * Includes SMTP/SES settings and notification preferences.
 */
@Component
@ConfigurationProperties(prefix = "mail")
public class MailProperties {

    private boolean enabled = false;
    private From from = new From();
    private Reminder reminder = new Reminder();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public From getFrom() {
        return from;
    }

    public void setFrom(From from) {
        this.from = from;
    }

    public Reminder getReminder() {
        return reminder;
    }

    public void setReminder(Reminder reminder) {
        this.reminder = reminder;
    }

    public static class From {
        private String email;
        private String name;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Reminder {
        private int hoursBeforeTour = 24;

        public int getHoursBeforeTour() {
            return hoursBeforeTour;
        }

        public void setHoursBeforeTour(int hoursBeforeTour) {
            this.hoursBeforeTour = hoursBeforeTour;
        }
    }
}
