package com.namazustudios.socialengine.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;

/**
 * Represents person who has entered into the system.  This includes the basic information on the
 * person
 *
 * Created by patricktwohig on 3/18/15.
 */
public class BasicEntrant {

    @Pattern(regexp = "\\s*", message = "Salutation must not be empty.")
    private String salutation;

    @Pattern(regexp = "\\s*", message = "First Name must not be empty.")
    private String firstName;

    @Pattern(regexp = "\\s*", message = "Last Name must not be empty.")
    private String lastName;

    @Pattern(regexp = "\\s*", message = "Email must not be empty.")
    private String email;

    @NotNull(message = "Birthday must be specified.")
    private Date birthday;

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

}
