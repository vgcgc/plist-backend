package com.zerobase.plistbackend.common.app.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.FileInputStream;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseConfig {

  @Bean
  public FirebaseMessaging firebaseMessaging() throws IOException {
    FileInputStream serviceAccount = new FileInputStream(
        "src/main/resources/firebase-service-account.json");

    FirebaseOptions options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();

    FirebaseApp firebaseApp = FirebaseApp.initializeApp(options);
    return FirebaseMessaging.getInstance(firebaseApp);
  }
}