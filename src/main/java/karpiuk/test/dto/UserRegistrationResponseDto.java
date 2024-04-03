package karpiuk.test.dto;

import lombok.Data;

@Data
public class UserRegistrationResponseDto {
    private String message = "Dear customer to proceed"
            + " your registration we sent an email to confirm it!";
}
