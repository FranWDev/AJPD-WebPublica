package org.dubini.frontend_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "resend")
public class ResendProperties {

    private String mail;
    private String associationEmailContact;
    private String associationEmailMuseum;
    private String associationEmailMuseum2;
    private String associationEmailInscription;
    private String apiKey;

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getAssociationEmailContact() {
        return associationEmailContact;
    }

    public void setAssociationEmailContact(String associationEmailContact) {
        this.associationEmailContact = associationEmailContact;
    }

    public String getAssociationEmailMuseum() {
        return associationEmailMuseum;
    }

    public void setAssociationEmailMuseum(String associationEmailMuseum) {
        this.associationEmailMuseum = associationEmailMuseum;
    }

    public String getAssociationEmailMuseum2() {
        return associationEmailMuseum2;
    }

    public void setAssociationEmailMuseum2(String associationEmailMuseum2) {
        this.associationEmailMuseum2 = associationEmailMuseum2;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getAssociationEmailInscription() {
        return associationEmailInscription;
    }

    public void setAssociationEmailInscription(String associationEmailInscription) {
        this.associationEmailInscription = associationEmailInscription;
    }
}