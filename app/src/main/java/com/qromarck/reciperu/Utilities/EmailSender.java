package com.qromarck.reciperu.Utilities;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "EmailSender";

    private final String username; // Tu dirección de correo electrónico
    private final String password; // Tu contraseña de correo electrónico
    private final String recipientEmail; // El destinatario del correo electrónico
    private final String subject; // El asunto del correo electrónico
    private final String messageBody; // El cuerpo del correo electrónico

    public EmailSender(String username, String password, String recipientEmail, String subject, String messageBody) {
        this.username = username;
        this.password = password;
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.messageBody = messageBody;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(messageBody);

            Transport.send(message);

            Log.d(TAG, "Correo enviado exitosamente!");

        } catch (MessagingException e) {
            Log.e(TAG, "Error al enviar el correo.", e);
        }

        return null;
    }
}
