package karpiuk.test.service;

import karpiuk.test.dto.ForgotPasswordRequest;
import karpiuk.test.dto.ForgotPasswordResponse;
import karpiuk.test.dto.PasswordChangeRequest;
import karpiuk.test.dto.ResetPasswordResponse;

public interface PasswordService {
    ForgotPasswordResponse forgotPasswordValidation(ForgotPasswordRequest requestDto);

    ResetPasswordResponse changePassword(PasswordChangeRequest requestDto);
}
