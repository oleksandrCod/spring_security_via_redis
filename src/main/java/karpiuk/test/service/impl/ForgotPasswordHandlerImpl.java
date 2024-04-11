package karpiuk.test.service.impl;

import karpiuk.test.dto.request.ForgotPasswordRequest;
import karpiuk.test.dto.request.PasswordChangeRequest;
import karpiuk.test.dto.response.ForgotPasswordResponse;
import karpiuk.test.dto.response.ResetPasswordResponse;
import karpiuk.test.model.User;
import karpiuk.test.repository.UserRepository;
import karpiuk.test.service.EmailSender;
import karpiuk.test.service.ForgotPasswordHandler;
import karpiuk.test.util.HashProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import static java.util.concurrent.TimeUnit.SECONDS;

@Service
@Slf4j
@RequiredArgsConstructor
public class ForgotPasswordHandlerImpl implements ForgotPasswordHandler {
    private static final String RESET_PASSWORD_EMAIL_SUBJECT =
            "Dear customer, an instruction to reset your password write below!";
    private static final String RESET_PASSWORD_EMAIL_TEXT =
            "To confirm your password change, please use this identification code: ";
    private static final String RESET_PASSWORD_RESPONSE_MESSAGE =
            "Dear customer, to confirm password change we sent you an email!";
    private static final String SUCCESSFUL_PASSWORD_CHANGE = "Your password was change successfully";

    private final ServiceHelper serviceHelper;
    private final EmailSender emailSender;
    private final UserRepository userRepository;
    private final HashProvider hashProvider;
    private final StringRedisTemplate redis;

    @Value("${spring.security.password-reset.token.length}")
    private Long resetPasswordTokenExpirationLength;

    @Override
    public ForgotPasswordResponse forgotPasswordValidation(
            ForgotPasswordRequest requestDto) {
        log.info("Processing forgot password request for email: {}", requestDto.email());

        User user = serviceHelper.getUserByEmail(requestDto.email());
        String resetToken = createPasswordResetToken(user);
        redis.opsForValue().set(resetToken, user.getEmail(), resetPasswordTokenExpirationLength, SECONDS);

        sendPasswordResetEmail(user, resetToken);
        log.info("Forgot password request processed for email: {}", requestDto.email());

        return new ForgotPasswordResponse(RESET_PASSWORD_RESPONSE_MESSAGE);
    }

    @Override
    public ResetPasswordResponse changePassword(PasswordChangeRequest requestDto) {
        log.info("Processing password change request with token: {}", requestDto.resetToken());

        String userEmailFromToken = validateResetPasswordToken(requestDto.resetToken());
        User user = serviceHelper.getUserByEmail(userEmailFromToken);
        user.setPassword(serviceHelper.encodePassword(requestDto.password()));
        userRepository.save(user);
        log.info("Password change request processed for email: {}", userEmailFromToken);

        return new ResetPasswordResponse(SUCCESSFUL_PASSWORD_CHANGE);
    }

    private String createPasswordResetToken(User user) {
        log.info("Creating password reset token for user: {}", user.getEmail());

        String resetToken = hashProvider.hashToSha256(user);

        log.info("Password reset token created for user: {}", user.getEmail());
        return resetToken;
    }

    private void sendPasswordResetEmail(User user, String resetToken) {
        log.info("Sending password reset email to user: {}", user.getEmail());

        SimpleMailMessage resetEmail = new SimpleMailMessage();

        resetEmail.setTo(user.getEmail());
        resetEmail.setSubject(RESET_PASSWORD_EMAIL_SUBJECT);
        resetEmail.setText(RESET_PASSWORD_EMAIL_TEXT + resetToken);
        emailSender.sendEmail(resetEmail);

        log.info("Password reset email sent to user: {}", user.getEmail());
    }


    private String validateResetPasswordToken(String resetToken) {
        log.info("Validating password reset token: {}", resetToken);

        String userEmailFromToken = redis.opsForValue().get(resetToken);
        if (userEmailFromToken != null) {
            serviceHelper.getUserByEmail(userEmailFromToken);
        }

        log.info("Password reset token validated: {}", resetToken);
        return userEmailFromToken;
    }
}
