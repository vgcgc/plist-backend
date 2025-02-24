package com.zerobase.plistbackend.module.user.oauth2;

import com.zerobase.plistbackend.module.refresh.service.RefreshServiceImpl;
import com.zerobase.plistbackend.module.user.jwt.JwtUtil;
import com.zerobase.plistbackend.module.user.model.auth.CustomOAuth2User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtUtil jwtUtil;
  private final RefreshServiceImpl refreshService;

  @Value("${login-url}")
  private String loginUrl;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {

    CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
    String email = customOAuth2User.findEmail();

    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
    GrantedAuthority auth = iterator.next();
    String role = auth.getAuthority();

    String access = jwtUtil.createJwt("access", email, role);
    String refresh = jwtUtil.createJwt("refresh", email, role);
    refreshService.addRefreshEntity(customOAuth2User.findId(), refresh);

    response.setStatus(HttpServletResponse.SC_FOUND);
    ResponseCookie cookie = jwtUtil.createCookie("refresh", refresh);
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    String url = String.format(loginUrl + "/auth/redirect?access-token=%s&is-member=%s",
        access, customOAuth2User.findIsMember());
    response.sendRedirect(url);
  }

}
