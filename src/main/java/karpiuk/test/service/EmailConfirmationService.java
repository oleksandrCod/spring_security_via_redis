package karpiuk.test.service;

import karpiuk.test.dto.response.RegistrationResponse;
import karpiuk.test.dto.request.ResendEmailConfirmationRequest;
import karpiuk.test.dto.response.ResendEmailConfirmationResponse;
import karpiuk.test.dto.response.UserConfirmedRegistrationResponse;
import karpiuk.test.model.User;

public interface EmailConfirmationService {
    RegistrationResponse sendEmailConfirmation(User user);

    ResendEmailConfirmationResponse resendConfirmationEmail(ResendEmailConfirmationRequest requestDto);

    UserConfirmedRegistrationResponse confirmEmail(String confirmationToken);
}
