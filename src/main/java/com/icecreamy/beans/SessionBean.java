package com.icecreamy.beans;

import com.icecreamy.obj.dto.CustomerDTO;
import com.icecreamy.service.SecurityService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.Serializable;

@Named("sessionBean")
@SessionScoped
public class SessionBean implements Serializable {
    @Inject
    private SecurityService securityService;
    private CustomerDTO currentUser;

    @PostConstruct
    public void init() {
        if (currentUser == null) {
            checkCookiesAndRestore();
        }
    }

    private void checkCookiesAndRestore() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("icecreamyAuth".equals(cookie.getName())) {
                    String email = cookie.getValue();
                    com.icecreamy.entity.Customer customer = securityService.getCustomerByEmail(email);
                    if (customer != null) {
                        this.currentUser = com.icecreamy.util.Utilities.convertToOrFromDto(customer, CustomerDTO.class);
                    }
                    break;
                }
            }
        }
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return isLoggedIn() && currentUser.getRole() == com.icecreamy.obj.UserRole.ADMIN;
    }

    public String getUserDisplayName() {
        if (currentUser == null) return "";
        String firstName = currentUser.getFirstName();
        String lastName = currentUser.getSurName();
        if (lastName != null && !lastName.isEmpty()) {
            return firstName + " " + lastName.substring(0, 1).toUpperCase() + ".";
        }
        return firstName;
    }

    public void login(CustomerDTO user) {
        this.currentUser = user;
    }

    public String logout() {
        currentUser = null;
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance()
                .getExternalContext().getResponse();
        Cookie cookie = new Cookie("icecreamyAuth", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "indexc?faces-redirect=true";
    }

    public CustomerDTO getCurrentUser() {
        return currentUser;
    }
}
