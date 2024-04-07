package karpiuk.test.service;

import karpiuk.test.dto.RegistrationResponse;
import karpiuk.test.dto.ResendEmailConfirmationRequest;
import karpiuk.test.dto.ResendEmailConfirmationResponse;
import karpiuk.test.dto.UserConfirmedRegistration;
import karpiuk.test.model.User;

public interface EmailConfirmationService {
    RegistrationResponse sendEmailConfirmation(User user);

    ResendEmailConfirmationResponse resendConfirmationEmail(ResendEmailConfirmationRequest requestDto);

    UserConfirmedRegistration confirmEmail(String confirmationToken);
}
