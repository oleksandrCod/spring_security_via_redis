package karpiuk.test.service.impl;

import karpiuk.test.dto.RegistrationResponse;
import karpiuk.test.dto.ResendEmailConfirmationRequest;
import karpiuk.test.dto.ResendEmailConfirmationResponse;
import karpiuk.test.dto.UserConfirmedRegistration;
import karpiuk.test.exception.exceptions.UserNotFoundException;
import karpiuk.test.model.User;
import karpiuk.test.repository.UserRepository;
import karpiuk.test.service.EmailConfirmationService;
import karpiuk.test.service.EmailService;
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
public class EmailConfirmationServiceImpl implements EmailConfirmationService {
    private static final String CONFIRMATION_EMAIL_SUBJECT =
            "Dear customer, please confirm your account registration!";
    private static final String CONFIRMATION_EMAIL_TEXT =
            "To confirm your account, please click this url: ";
    private static final String EMAIL_CONFIRMATION_USER_MESSAGE = "Dear customer to proceed"
            + " your registration we sent an email to confirm it!";
    private static final String SUCCESSFUL_CONFIRMATION_MESSAGE =
            "Dear customer your account was activated successfully!";
    private static final String EMAIL_RESEND_MESSAGE = "Confirmation email was send again!";

    private final UserRepository userRepository;
    private final HashUtil hashUtil;
    private final StringRedisTemplate redis;
    private final EmailService emailService;
    private final ServiceHelper serviceHelper;

    @Value("${spring.security.email-confirmation.url}")
    private String confirmationUrl;
    @Value("${spring.security.email-token.expiration-length}")
    private Long emailTokenExpirationLength;

    @Override
    public RegistrationResponse sendEmailConfirmation(User user) {
        log.info("Sending email confirmation to user: {}", user.getEmail());
        String token = createEmailConfirmationToken(user);
        redis.opsForValue().set(token, user.getEmail(), emailTokenExpirationLength, TimeUnit.MILLISECONDS);
        sendConfirmationEmail(user, token);
        return new RegistrationResponse(EMAIL_CONFIRMATION_USER_MESSAGE);
    }

    private String createEmailConfirmationToken(User user) {
        log.debug("Creating email confirmation token for user: {}", user.getEmail());
        return hashUtil.hashToSha256(user);
    }

    private void sendConfirmationEmail(User user, String confirmationToken) {
        SimpleMailMessage confirmationEmail = new SimpleMailMessage();
        confirmationEmail.setTo(user.getEmail());
        confirmationEmail.setSubject(CONFIRMATION_EMAIL_SUBJECT);
        confirmationEmail.setText(CONFIRMATION_EMAIL_TEXT + confirmationUrl + confirmationToken);
        emailService.sendEmail(confirmationEmail);
    }

    @Override
    public ResendEmailConfirmationResponse resendConfirmationEmail(
            ResendEmailConfirmationRequest requestDto) {
        User user = serviceHelper.getUserByEmail(requestDto.email());
        sendEmailConfirmation(user);
        return new ResendEmailConfirmationResponse(EMAIL_RESEND_MESSAGE);
    }

    @Override
    public UserConfirmedRegistration confirmEmail(String confirmationToken) {
        String userEmail = validateConfirmationToken(confirmationToken);
        User user = serviceHelper.getUserByEmail(userEmail);
        enableUser(user);
        return new UserConfirmedRegistration(SUCCESSFUL_CONFIRMATION_MESSAGE);
    }

    private String validateConfirmationToken(String confirmationToken) {
        String userEmailFromToken = redis.opsForValue().get(confirmationToken);
        if (userEmailFromToken != null) {
            serviceHelper.getUserByEmail(userEmailFromToken);
        }
        return userEmailFromToken;
    }



    private void enableUser(User user) {
        user.setEnabled(true);
        userRepository.save(user);
    }
}
