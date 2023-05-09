package dev.getelements.elements.model;

import dev.getelements.elements.Constants;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;

/**
 * Represents person who has entered into the system.  This includes the basic information on the
 * person
 *
 * Created by patricktwohig on 3/18/15.
 */
@ApiModel
public class BasicEntrantProfile implements Serializable {

    @NotNull
    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    private String salutation;

    @NotNull
    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    private String firstName;

    @NotNull
    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BasicEntrantProfile)) return false;

        BasicEntrantProfile that = (BasicEntrantProfile) o;

        if (getSalutation() != null ? !getSalutation().equals(that.getSalutation()) : that.getSalutation() != null)
            return false;
        if (getFirstName() != null ? !getFirstName().equals(that.getFirstName()) : that.getFirstName() != null)
            return false;
        if (getLastName() != null ? !getLastName().equals(that.getLastName()) : that.getLastName() != null)
            return false;
        if (getEmail() != null ? !getEmail().equals(that.getEmail()) : that.getEmail() != null) return false;
        return getBirthday() != null ? getBirthday().equals(that.getBirthday()) : that.getBirthday() == null;
    }

    @Override
    public int hashCode() {
        int result = getSalutation() != null ? getSalutation().hashCode() : 0;
        result = 31 * result + (getFirstName() != null ? getFirstName().hashCode() : 0);
        result = 31 * result + (getLastName() != null ? getLastName().hashCode() : 0);
        result = 31 * result + (getEmail() != null ? getEmail().hashCode() : 0);
        result = 31 * result + (getBirthday() != null ? getBirthday().hashCode() : 0);
        return result;
    }

}
