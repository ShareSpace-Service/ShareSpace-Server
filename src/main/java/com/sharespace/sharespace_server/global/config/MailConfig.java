package com.sharespace.sharespace_server.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

// mail:
//    host: smtp.naver.com
//    port: 465
//    username: teamsharespace@naver.com
//    password: ${MAIL_SMTP_PASSWORD}
//    properties:
//      mail.smtp.auth: true
//      mail.smtp.ssl.enable: true
//      mail.smtp.ssl.trust: smtp.naver.com
//      mail.debug: true

@Configuration
public class MailConfig {
    @Value("smtp.naver.com")
    private String host;

    @Value("465")
    private int port;

    @Value("teamsharespace@naver.com")
    private String username;

    @Value("${MAIL_SMTP_PASSWORD}")
    private String password;

    @Value("true")
    private boolean auth;

    @Value("true")
    private boolean sslEnable;

    @Value("smtp.naver.com")
    private String sslTrust;

    @Value("true")
    private boolean debug;

    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        System.out.println("MAIL_USER_NAME : " + username);
        System.out.println("MAIL_SMTP_PASSWORD: " + password);

        Properties mailProperties = mailSender.getJavaMailProperties();
        mailProperties.put("mail.smtp.auth", auth);
        mailProperties.put("mail.smtp.ssl.enable", sslEnable);
        mailProperties.put("mail.smtp.ssl.trust", sslTrust);
        mailProperties.put("mail.debug", debug);

        return mailSender;
    }
}
