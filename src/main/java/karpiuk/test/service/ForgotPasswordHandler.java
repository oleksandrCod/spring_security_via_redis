package karpiuk.test.service;

import karpiuk.test.dto.request.ForgotPasswordRequest;
import karpiuk.test.dto.response.ForgotPasswordResponse;
import karpiuk.test.dto.request.PasswordChangeRequest;
import karpiuk.test.dto.response.ResetPasswordResponse;

public interface ForgotPasswordHandler {
    ForgotPasswordResponse forgotPasswordValidation(ForgotPasswordRequest requestDto);

    ResetPasswordResponse changePassword(PasswordChangeRequest requestDto);
}
