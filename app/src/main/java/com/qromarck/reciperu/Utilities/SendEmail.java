package com.qromarck.reciperu.Utilities;

import android.os.AsyncTask;
import android.util.Log;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class SendEmail {

    // Credenciales y destinatario de correo electrónico
    private static String direccionCorreo = "reciperu4@gmail.com";
    private static String contrasenyaCorreo = "qiclpocvpqvwryxx";

    public static void enviarMensaje(final String subject, final String content , String destinatarioCorreo) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Configurar las propiedades para la conexión SMTP
                    Properties properties = new Properties();
                    properties.put("mail.smtp.host", "smtp.gmail.com");
                    properties.put("mail.smtp.socketFactory.port", "465");
                    properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                    properties.put("mail.smtp.auth", "true");
                    properties.put("mail.smtp.port", "465");

                    // Crear un mensaje MimeMessage
                    Session session = Session.getInstance(properties, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(direccionCorreo, contrasenyaCorreo);
                        }
                    });

                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(direccionCorreo));
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(destinatarioCorreo));
                    message.setSubject(subject);
                    message.setContent(content, "text/html");

                    // Enviar el mensaje
                    Transport.send(message);

                    // Mostrar mensaje de envío exitoso en el hilo principal (opcional)
                    Log.d("SendEmail", "Mensaje enviado exitosamente");

                } catch (MessagingException e) {
                    e.printStackTrace();
                    // Manejar cualquier excepción aquí, como mostrar un mensaje de error
                }
            }
        });
    }
}
