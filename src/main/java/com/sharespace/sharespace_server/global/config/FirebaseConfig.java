package com.sharespace.sharespace_server.global.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;

@Configuration
public class FirebaseConfig {
	@Value("${firebase.configuration-file}")
	private String account;

	@Value("${firebase.bucket}")
	private String bucket;

	@Bean
	public FirebaseApp firebaseApp() throws IOException {

		FirebaseOptions options = FirebaseOptions.builder()
			.setCredentials(
				GoogleCredentials.fromStream(
					new ClassPathResource(account).getInputStream()))
			.setStorageBucket(bucket)
			.build();

		FirebaseApp app = FirebaseApp.initializeApp(options);

		return app;
	}

	@Bean
	public Bucket bucket() throws IOException{
		return StorageClient.getInstance(firebaseApp()).bucket();
	}
}
