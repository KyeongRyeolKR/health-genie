package com.example.healthgenie.boundedContext.email.service;


import com.example.healthgenie.base.exception.Common.CommonException;
import com.example.healthgenie.boundedContext.email.EmailValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender emailSender;
    private final EmailValidator validator;

    public void sendEmail(String toEmail,
                          String title,
                          String text) throws MailException {

        if (!validator.test(toEmail)) {
            log.warn("email validateion fail");
            throw CommonException.WRONG_VALIDATE_EMAIL;
        }

        SimpleMailMessage emailForm = createEmailForm(toEmail, title, text);

        try {
            log.info("email send ,emailForm : {} ", emailForm);
            emailSender.send(emailForm);
        } catch (RuntimeException e) {
            throw CommonException.UNABLE_TO_SEND_EMAIL;
        }
    }

    // 발신할 이메일 데이터 세팅
    private SimpleMailMessage createEmailForm(String toEmail,
                                              String title,
                                              String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(title);
        message.setText(text);

        return message;
    }
}