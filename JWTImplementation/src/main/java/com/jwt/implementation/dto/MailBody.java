package com.jwt.implementation.dto;

public record MailBody(String to, String subject, String text) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String to;
        private String subject;
        private String text;

        public Builder to(String to) {
            this.to = to;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public MailBody build() {
            if (to == null || to.trim().isEmpty()) {
                throw new IllegalArgumentException("Recipient email cannot be null or empty");
            }
            if (subject == null || subject.trim().isEmpty()) {
                throw new IllegalArgumentException("Subject cannot be null or empty");
            }
            if (text == null || text.trim().isEmpty()) {
                throw new IllegalArgumentException("Text cannot be null or empty");
            }
            return new MailBody(to, subject, text);
        }
    }
}