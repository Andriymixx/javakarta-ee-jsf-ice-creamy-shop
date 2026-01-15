package com.icecreamy.beans;

import com.icecreamy.obj.SignupWebModel;
import com.icecreamy.obj.UserRole;
import com.icecreamy.obj.dto.CustomerDTO;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import com.icecreamy.service.SecurityService;
import com.icecreamy.obj.WebResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Named
@RequestScoped
public class LoginBean {
    private String email;
    private String password;
    private boolean rememberMe;
    private String loginMessage;

    private String name;
    private String surname;
    private String confirmpassword;
    private String signupMessage;

    @Inject
    private SecurityService securityService;
    @Inject
    private SessionBean sessionBean;

    public void signup() {
        if (!password.equals(confirmpassword)) {
            signupMessage = "Паролі не збігаються";
            return;
        }

        SignupWebModel signupModel = new SignupWebModel(name, surname, password, confirmpassword, email);
        WebResponse response = securityService.signUp(signupModel);

        if (response.isSuccesfullOpt()) {
            CustomerDTO user = (CustomerDTO) response.getModelDtoObject();
            sessionBean.login(user);

            try {
                FacesContext context = FacesContext.getCurrentInstance();
                context.getExternalContext().getFlash().setKeepMessages(true);
                context.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Вітаємо!", "Ваш акаунт успішно створено"));

                context.getExternalContext().redirect("indexc.xhtml");
            } catch (IOException e) {
                signupMessage = "Помилка при переході на головну сторінку";
            }
        } else {
            signupMessage = response.getAdditionalInfo() != null ?
                    response.getAdditionalInfo() : "Помилка реєстрації";
        }
    }

    public void login() {
        WebResponse response = securityService.login(email, password);
        if (response.isSuccesfullOpt()) {
            CustomerDTO user = (CustomerDTO) response.getModelDtoObject();
            sessionBean.login(user);
            if (rememberMe) {
                setAuthCookie(user.getEmail());
            }
            try {
                String referer = (String) FacesContext.getCurrentInstance()
                        .getExternalContext().getRequestHeaderMap().get("referer");
                if (referer == null || referer.contains("login.xhtml")) {
                    FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Успіх!", "Ви успішно увійшли в систему"));
                    referer = "indexc.xhtml";
                }
                FacesContext.getCurrentInstance().getExternalContext().redirect(referer);
            } catch (IOException e) {
                loginMessage = "Помилка перенаправлення";
            }
        } else {
            loginMessage = response.getMessage() != null ? response.getMessage() : "Невірний логін або пароль";
        }
    }


    private String handleLoginResponse(WebResponse response) {
        if (response.isSuccesfullOpt()) {
            CustomerDTO user = (CustomerDTO) response.getModelDtoObject();
            setAuthCookie(user.getEmail());
            if (user.getRole() == UserRole.ADMIN) {
                return "admin?faces-redirect=true";
            }
            return "index?faces-redirect=true";
        } else {
            loginMessage = response.getMessage() != null ? response.getMessage() : "Невірний логін або пароль";
            return null;
        }
    }

    private void setAuthCookie(String email) {
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance()
                .getExternalContext().getResponse();
        Cookie cookie = new Cookie("icecreamyAuth", email);
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        if (rememberMe) {
            cookie.setMaxAge(60 * 60 * 24 * 7);
        } else {
            cookie.setMaxAge(-1);
        }
        response.addCookie(cookie);
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public String getLoginMessage() {
        return loginMessage;
    }

    public void setLoginMessage(String loginMessage) {
        this.loginMessage = loginMessage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getConfirmpassword() {
        return confirmpassword;
    }

    public void setConfirmpassword(String confirmpassword) {
        this.confirmpassword = confirmpassword;
    }

    public String getSignupMessage() {
        return signupMessage;
    }

    public void setSignupMessage(String signupMessage) {
        this.signupMessage = signupMessage;
    }


}
