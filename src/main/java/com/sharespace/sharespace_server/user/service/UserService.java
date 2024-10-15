package com.sharespace.sharespace_server.user.service;

import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.UserException;
import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.response.BaseResponseService;
import com.sharespace.sharespace_server.global.utils.LocationTransform;
import com.sharespace.sharespace_server.global.utils.S3ImageUpload;
import com.sharespace.sharespace_server.user.dto.UserEmailValidateRequest;
import com.sharespace.sharespace_server.user.dto.UserRegisterRequest;
import com.sharespace.sharespace_server.user.dto.UserUpdateRequest;
import com.sharespace.sharespace_server.user.entity.User;
import com.sharespace.sharespace_server.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BaseResponseService baseResponseService;
    private final LocationTransform locationTransform;
    private final BCryptPasswordEncoder encoder;
    private final JavaMailSender javaMailSender;
    private final Map<String, Integer> verificationCodes = new ConcurrentHashMap<>();
    private final S3ImageUpload s3ImageUpload;

    @Transactional
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

    // 이메일 인증여부 업데이트
    @Transactional
    public BaseResponse<Void> emailValidate(UserEmailValidateRequest request) {

        User user = userRepository.findById(request.getUserId()).orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));
        // 이메일 인증 확인 메소드 호출
        verifyCode(user.getEmail(), request.getValidationNumber());

        user.setEmailValidated(true);
        userRepository.save(user);

        return baseResponseService.getSuccessResponse();
    }

    // 회원 정보 수정
    @Transactional
    public BaseResponse<Void> update(UserUpdateRequest request) {
        User user = userRepository.findById(request.getUserId()).orElseThrow(
                () -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND)
        );

        // 주소 위/경도 변환 메소드 호출
        Map<String, Double> coordinates = locationTransform.getCoordinates(request.getLocation());

        Double latitude = coordinates.get("latitude");
        Double longitude = coordinates.get("longitude");

        // profile Image 수정
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            String newImageUrl = s3ImageUpload.updateImage(user.getImage(), request.getImage(), "profile/" + user.getId());
            user.setImage(newImageUrl);
        }

        user.setLocation(request.getLocation());
        user.setLatitude(latitude);
        user.setLongitude(longitude);
        user.setNickName(request.getNickName());

        userRepository.save(user);

        return baseResponseService.getSuccessResponse();
    }

    @Transactional
    public BaseResponse<String> getPlace(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));
        String location = user.getLocation();
        return baseResponseService.getSuccessResponse(location);
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

        // 이메일과 인증번호를 메모리에 저장
        verificationCodes.put(Email, number);

        javaMailSender.send(message);
    }

    // 이메일 인증 확인 메소드
    public void verifyCode(String email, Integer number) {
        Integer storedCode = verificationCodes.get(email);

        // 인증번호 확인
        if (storedCode != null && storedCode.equals(number)) {
            verificationCodes.remove(email);
        } else {
            throw new CustomRuntimeException(UserException.EMAIL_VALIDATION_FAIL);
        }
    }

    // 로그인할 때 Request가 Email로 오므로 email로 member 찾아야 함
    public boolean checkAccountLocked(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));
        // member의 lockTime이 null이 아니면 계정이 잠금상태인 것임
        if (user.getLockTime() != null) {
            LocalDateTime unlockTime = user.getLockTime().plusMinutes(5);
            if (LocalDateTime.now().isAfter(unlockTime)) {
                user.setLockTime(null);
                user.setFailedAttempts(0);
                userRepository.save(user);
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    // 로그인 시도 실패 로직

    public void loginAttemptationFailed(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));

        user.setFailedAttempts(user.getFailedAttempts() + 1);
        if (user.getFailedAttempts() >= 5) {
            user.setLockTime(LocalDateTime.now());
        }
        userRepository.save(user);
    }

    // 로그인 성공시
    public void loginAttemptationSuccess(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));
        user.setFailedAttempts(0);
        user.setLockTime(null);
        userRepository.save(user);
    }

}
