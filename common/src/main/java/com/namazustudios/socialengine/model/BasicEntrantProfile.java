package com.namazustudios.socialengine.model;

import com.namazustudios.socialengine.Constants;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;

/**
 * Represents person who has entered into the system.  This includes the basic information on the
 * person
 *
 * Created by patricktwohig on 3/18/15.
 */
@ApiModel
public class BasicEntrantProfile {

    @NotNull
    @Pattern(regexp = Constants.Regexp.NON_BLANK_STRING)
    private String salutation;

    @NotNull
    @Pattern(regexp = Constants.Regexp.NON_BLANK_STRING)
    private String firstName;

    @NotNull
    @Pattern(regexp = Constants.Regexp.NON_BLANK_STRING)
    private String lastName;

    @NotNull
    @Pattern(regexp = Constants.Regexp.EMAIL_ADDRESS)
    private String email;

    @NotNull
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
