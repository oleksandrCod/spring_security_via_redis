package karpiuk.test.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;

public interface EmailSender {

    @Async
    void sendEmail(SimpleMailMessage email);
}
