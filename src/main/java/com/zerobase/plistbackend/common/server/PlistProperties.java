package com.zerobase.plistbackend.common.server;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class PlistProperties {
  @Value("${server.env}")
  private String env;

  @Value("${server.port}")
  private String port;

  @Value("${serverName}")
  private String serverName;

  @Value("${server.serverAddress}")
  private String serverAddress;

  @Value("${spring.datasource.driver-class-name}")
  private String driverClassName;

  @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
  private String loginRedirectUri;
}
