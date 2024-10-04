package com.sharespace.sharespace_server.user.service;

import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.UserException;
import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.response.BaseResponseService;
import com.sharespace.sharespace_server.global.utils.LocationTransform;
import com.sharespace.sharespace_server.user.dto.UserRegisterRequest;
import com.sharespace.sharespace_server.user.entity.User;
import com.sharespace.sharespace_server.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BaseResponseService baseResponseService;
    private final LocationTransform locationTransform;
    private final BCryptPasswordEncoder encoder;
    private final JavaMailSender javaMailSender;

    public BaseResponse<Long> register(UserRegisterRequest request) {

        // 이메일 중복 검사 메소드 호출
        emailDuplicate(request.getEmail());

        // 닉네임 중복 검사 메소드 호출
        nickNameDuplicate(request.getNickname());

        // 주소 위/경도 변환 메소드 호출
        Map<String, Double> coordinates = locationTransform.getCoordinates(request.getLocation());

        Double latitude = coordinates.get("latitude");
        Double longitude = coordinates.get("longitude");

        // User 생성
        User user = User.builder()
                .email(request.getEmail())
                .role(request.getRole())
                .password(encoder.encode(request.getPassword())) // 비밀번호 암호화
                .location(request.getLocation())
                .nickName(request.getNickname())
                .latitude(latitude)
                .longitude(longitude)
                .emailValidated(false)
                .build();


        // User 저장
        userRepository.save(user);

        // 이메일 전송 메소드 호출
        sendEmail(request.getEmail());

        return baseResponseService.getSuccessResponse(user.getId());
    }

    // 이메일 중복 검사 메소드
    private void emailDuplicate(String email) {
        if(userRepository.findByEmail(email).isPresent()) {
            throw new CustomRuntimeException(UserException.EMAIL_DUPLICATED);
        }
    }

    // 닉네임 중복 검사 메소드
    private void nickNameDuplicate(String nickname) {
        if(userRepository.findByNickName(nickname).isPresent()) {
            throw new CustomRuntimeException(UserException.NICKNAME_DUPLICATED);
        }
    }

    // 이메일 전송 메소드
    private void sendEmail(String Email) {
        int number = (int)(Math.random() * (90000)) + 100000;
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom("teamsharespace@naver.com");
            message.setRecipients(MimeMessage.RecipientType.TO, Email);
            message.setSubject("이메일 인증");
            String body = "";
            body += "<h3>" + "요청하신 인증 번호입니다." + "</h3>";
            body += "<h1>" + number + "</h1>";
            body += "<h3>" + "감사합니다." + "</h3>";
            message.setText(body,"UTF-8", "html");
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        System.out.println(number);
        System.out.println(message);

        javaMailSender.send(message);
    }


}
