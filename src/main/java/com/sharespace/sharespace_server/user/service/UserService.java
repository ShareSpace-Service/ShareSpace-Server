package com.sharespace.sharespace_server.user.service;

import com.sharespace.sharespace_server.global.enums.Role;
import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.JwtException;
import com.sharespace.sharespace_server.global.exception.error.UserException;
import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.response.BaseResponseService;
import com.sharespace.sharespace_server.global.utils.LocationTransform;
import com.sharespace.sharespace_server.global.utils.S3ImageUpload;
import com.sharespace.sharespace_server.jwt.entity.Token;
import com.sharespace.sharespace_server.jwt.repository.TokenJpaRepository;
import com.sharespace.sharespace_server.jwt.service.TokenBlacklistService;
import com.sharespace.sharespace_server.user.dto.UserEmailValidateRequest;
import com.sharespace.sharespace_server.user.dto.UserGetInfoResponse;
import com.sharespace.sharespace_server.user.dto.UserRegisterRequest;
import com.sharespace.sharespace_server.user.dto.UserUpdateRequest;
import com.sharespace.sharespace_server.user.entity.User;
import com.sharespace.sharespace_server.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
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
    private final TokenBlacklistService tokenBlacklistService;
    private final TokenJpaRepository tokenJpaRepository;


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
    public BaseResponse<Void> update(UserUpdateRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
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

    @Transactional
    public BaseResponse<UserGetInfoResponse> getInfo(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));

        UserGetInfoResponse userInfo = UserGetInfoResponse.builder()
                .nickName(user.getNickName())
                .email(user.getEmail())
                .image(user.getImage())
                .role(StringUtils.capitalize(user.getRole().getValue().toLowerCase(Locale.ROOT)))
                .location(user.getLocation())
                .build();

        return baseResponseService.getSuccessResponse(userInfo);
    }

    // 로그아웃
    @Transactional
    public BaseResponse<Void> logout(String accessToken, String refreshToken, HttpServletResponse response, Long userId) {

        Token token = tokenJpaRepository.findByUserId(userId).orElseThrow(() -> new CustomRuntimeException(JwtException.REFRESH_TOKEN_NOT_FOUND_EXCEPTION));

        // 1. AccessToken 블랙리스트에 추가
        tokenBlacklistService.addToBlacklist(accessToken);

        // 2. 쿠키 만료 처리
        expireCookie(response, "accessToken");
        expireCookie(response, "refreshToken");

        tokenJpaRepository.delete(token);

        return baseResponseService.getSuccessResponse();
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
    private void sendEmail(String email) {
        int verificationCode = generateVerificationCode();
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom("teamsharespace@naver.com");
            message.setRecipients(MimeMessage.RecipientType.TO, email);
            message.setSubject("ShareSpace 회원가입 이메일 인증");

            String emailBody = createEmailBody(verificationCode);
            message.setText(emailBody, "UTF-8", "html");

            // 인증번호를 메모리에 저장
            verificationCodes.put(email, verificationCode);

            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new CustomRuntimeException(UserException.EMAIL_SEND_FAIL);
        }
    }

    // 인증번호 생성 메서드
    private int generateVerificationCode() {
        return (int) (Math.random() * 90000) + 100000; // 6자리 랜덤 인증번호 생성
    }

    // 이메일 본문 생성 메서드
    private String createEmailBody(int verificationCode) {
        StringBuilder body = new StringBuilder();
        body.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0;'>")
            .append("<img src='https://sharespace-images-bucket.s3.ap-northeast-2.amazonaws.com/sharespace.svg' alt='ShareSpace 로고' style='display: block; margin-bottom: 20px; max-width: 150px;'>")
            .append("<h2 style='color: #333; font-weight: bold; font-size: 24px; border-top: 2px solid #ddd; padding-top: 40px;'>회원가입을 위한 인증 메일입니다.</h2>")
            .append("<p>안녕하세요, 회원님</p>")
            .append("<p>Share Space 이메일 인증을 위한 인증번호가 발급되었습니다.</p>")
            .append("<p>아래의 인증번호 6자리를 진행 중인 화면에 입력하고 인증을 완료해 주세요.</p>")
            .append("<div style='margin: 50px 0;'>")
            .append("<table style='width: 100%;'>")
            .append("<tr><td style='font-size: 14px; color: #333;'>인증번호</td>")
            .append("<td style='font-size: 30px; color: #007bff; font-weight: bold; text-align: left;'>").append(verificationCode).append("</td></tr>")
            .append("<tr><td style='font-size: 14px; color: #333; padding-top: 16px;'>유효기간</td>")
            .append("<td style='font-size: 14px; color: #666; text-align: left; padding-top: 16px;'>")
            .append(LocalDateTime.now().plusMinutes(10).format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")))
            .append(" 까지</td></tr>")
            .append("</table></div>")
            .append("<div style='font-size: 12px; color: #555; background-color: #f9f9f9; padding: 15px; border-radius: 15px; display: inline-block;'>")
            .append("<p><strong>안내</strong></p>")
            .append("<ul style='padding-left: 20px;'>")
            .append("<li>회원님의 개인정보 보호를 위해서 인증 번호 유효기간 내에 인증을 받으셔야 정상적으로 회원가입이 가능합니다.</li>")
            .append("<li>타인이 실수로 회원님의 이메일 주소를 입력했을 경우 해당 메일이 발송될 수 있습니다.</li>")
            .append("<li>궁금하신 사항은 고객센터 FAQ를 확인하시거나 teamsharespace@naver.com로 문의해주시면 정성껏 답변해 드리겠습니다.</li>")
            .append("</ul></div></div>");
        return body.toString();
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

//    // 로그인 시도 검증 로직
//    public void loginAttemptaionCheck(String email) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));
//
//        if (!user.getEmailValidated()) {
//            throw new CustomRuntimeException(UserException.EMAIL_NOT_VALIDATED);
//        }
//    }

    // 로그인 시도 실패 후 로직
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

    private void expireCookie(HttpServletResponse response, String tokenName) {
        Cookie cookie = new Cookie(tokenName, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);  // 즉시 만료
        response.addCookie(cookie);
    }

}
