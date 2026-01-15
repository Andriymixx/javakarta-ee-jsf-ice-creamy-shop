package com.icecreamy.rest.security;

import com.icecreamy.beans.SessionBean;
import com.icecreamy.obj.UserRole;
import jakarta.inject.Inject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebFilter(urlPatterns = {"/my-account.xhtml"})
public class JsfSecurityFilter implements Filter {

    @Inject
    private SessionBean sessionBean;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getRequestURI();

        if (sessionBean == null || !sessionBean.isLoggedIn()) {
            res.sendRedirect(req.getContextPath() + "/login.xhtml?faces-redirect=true");
            return;
        }

        if (path.contains("admin.xhtml")) {
            if (sessionBean.getCurrentUser().getRole() != UserRole.ADMIN) {
                res.sendRedirect(req.getContextPath() + "/indexc.xhtml");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
