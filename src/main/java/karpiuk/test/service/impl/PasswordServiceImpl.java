package karpiuk.test.service.impl;

import karpiuk.test.dto.ForgotPasswordRequest;
import karpiuk.test.dto.ForgotPasswordResponse;
import karpiuk.test.dto.PasswordChangeRequest;
import karpiuk.test.dto.ResetPasswordResponse;
import karpiuk.test.model.User;
import karpiuk.test.repository.UserRepository;
import karpiuk.test.service.EmailService;
import karpiuk.test.service.PasswordService;
import karpiuk.test.util.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {
    private static final String RESET_PASSWORD_EMAIL_SUBJECT =
            "Dear customer, an instruction to reset your password write below!";
    private static final String RESET_PASSWORD_EMAIL_TEXT =
            "To confirm your password change, please use this identification code: ";
    private static final String RESET_PASSWORD_RESPONSE_MESSAGE =
            "Dear customer, to confirm password change we sent you an email!";
    private static final String SUCCESSFUL_PASSWORD_CHANGE = "Your password was change successfully";

    private final ServiceHelper serviceHelper;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final HashUtil hashUtil;
    private final StringRedisTemplate redis;

    @Value("${spring.security.password-reset.token.length}")
    private Long resetPasswordTokenExpirationLength;

    @Override
    public ForgotPasswordResponse forgotPasswordValidation(
            ForgotPasswordRequest requestDto) {
        User user = serviceHelper.getUserByEmail(requestDto.email());
        String resetToken = createPasswordResetToken(user);
        redis.opsForValue().set(resetToken, user.getEmail(), resetPasswordTokenExpirationLength, TimeUnit.MILLISECONDS);
        sendPasswordResetEmail(user, resetToken);
        return new ForgotPasswordResponse(RESET_PASSWORD_RESPONSE_MESSAGE);
    }

    @Override
    public ResetPasswordResponse changePassword(PasswordChangeRequest requestDto) {
        String userEmailFromToken = validateResetPasswordToken(requestDto.resetToken());
        User user = serviceHelper.getUserByEmail(userEmailFromToken);
        user.setPassword(serviceHelper.encodePassword(requestDto.password()));
        userRepository.save(user);
        return new ResetPasswordResponse(SUCCESSFUL_PASSWORD_CHANGE);
    }

    private String createPasswordResetToken(User user) {
        return hashUtil.hashToSha256(user);
    }

    private void sendPasswordResetEmail(User user, String resetToken) {
        SimpleMailMessage resetEmail = new SimpleMailMessage();
        resetEmail.setTo(user.getEmail());
        resetEmail.setSubject(RESET_PASSWORD_EMAIL_SUBJECT);
        resetEmail.setText(RESET_PASSWORD_EMAIL_TEXT + resetToken);
        emailService.sendEmail(resetEmail);
    }


    private String validateResetPasswordToken(String resetToken) {
        String userEmailFromToken = redis.opsForValue().get(resetToken);
        if (userEmailFromToken != null) {
            serviceHelper.getUserByEmail(userEmailFromToken);
        }
        return userEmailFromToken;
    }
}
