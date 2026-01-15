package com.icecreamy.service;

import java.util.Collections;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import com.icecreamy.dao.impl.CustomerDaoImpl;
import com.icecreamy.entity.Customer;
import com.icecreamy.obj.SignupWebModel;
import com.icecreamy.obj.UserRole;
import com.icecreamy.obj.WebResponse;
import com.icecreamy.obj.dto.CustomerDTO;
import com.icecreamy.util.AppConstants;
import com.icecreamy.util.Utilities;

@Stateless
public class SecurityService {
    private static final Logger logger = Logger.getLogger(SecurityService.class);

    @EJB
    private CustomerDaoImpl customerDaoStub;

    public WebResponse signUp(@NotNull SignupWebModel signupModel) {
        Customer customerWithSameEmail = this.getCustomerByEmail(signupModel.getEmail());
        String signUpError = Utilities.getSignUpErrors(signupModel, customerWithSameEmail);
        if (StringUtils.isNotEmpty(signUpError)) {
            logger.error("Failde to signUp, Error message: " + signUpError);
            return new WebResponse(AppConstants.BAD_SIGNUP, false, signUpError);
        }
        try {
            String encryptedPassword = Utilities.encrypt(signupModel.getPassword(), signupModel.getPassword(), signupModel.getEmail());
            Customer newCustomer = new Customer(signupModel.getFirstName(), signupModel.getSurName(),
                    encryptedPassword, signupModel.getEmail(), UserRole.CUSTOMER);
            customerDaoStub.create(newCustomer);
            CustomerDTO customerDto = Utilities.convertToOrFromDto(newCustomer, CustomerDTO.class);
            return new WebResponse(AppConstants.GOOD_SIGNUP, true, customerDto);
        } catch (Exception e) {
            e.printStackTrace();
            return new WebResponse(AppConstants.BAD_SIGNUP, false, e.getMessage());
        }
    }

    public WebResponse login(String email, String password) {
        try {
            Customer loginCustomer = this.getCustomerByEmail(email);
            String decryptedPassword =
                    loginCustomer != null
                            ? Utilities.decrypt(loginCustomer.getPassword(), password, email)
                            : StringUtils.EMPTY;

            if (decryptedPassword.equals(password)) {
                CustomerDTO customerDto = Utilities.convertToOrFromDto(loginCustomer, CustomerDTO.class);
                logger.info("GOOD_LOGIN: " + loginCustomer);
                return new WebResponse(AppConstants.GOOD_LOGIN, true, customerDto);
            }
        } catch (Exception e) {
            logger.error(String.format("An error occurred while trying to login user, customer email: %s", email));
        }
        return new WebResponse(AppConstants.BAD_LOGIN, false);
    }

    public Customer getCustomerByEmail(String email) {
        return customerDaoStub.getResultCustomQuery("getCustomerByEmail",
                Collections.singletonMap("email", email));
    }
}
