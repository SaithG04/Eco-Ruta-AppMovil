package com.qromarck.reciperu.Interfaces;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.qromarck.reciperu.DAO.DAOImplements.UsuarioDAOImpl;
import com.qromarck.reciperu.DAO.UsuarioDAO;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.DataAccessUtilities;
import com.qromarck.reciperu.Utilities.InterfacesUtilities;
import com.qromarck.reciperu.Utilities.SendEmail;

import javax.mail.MessagingException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;


public class ReciShop extends AppCompatActivity {

    private TextView ptos;

    public static String typeChange = "";

    private FrameLayout loadingLayout;
    private ProgressBar loadingIndicator;

    public TextView getPtos() {
        return ptos;
    }

    int precio = 0;

    Button btn1,btn2,btn3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reci_shop);

        //Recuperamos usuario logeado
        Usuario userLoggedOnSystem = InterfacesUtilities.recuperarUsuario(ReciShop.this);

        //Otener puntos de usuario logeado en sistema
        int recipoints = userLoggedOnSystem.getPuntos();
        ptos = findViewById(R.id.txvEcoPoints);
        //Colocar ReciPoints:
        ptos.setText(String.valueOf(recipoints));

        //PUNTOS
        //BOTONES
        btn1 = findViewById(R.id.btnProd1);
        btn2 = findViewById(R.id.btnProd2);
        btn3 = findViewById(R.id.btnProd3);

        btn1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Recuperamos puntos usuario logeado
                Usuario userLoggedOnSystem = InterfacesUtilities.recuperarUsuario(ReciShop.this);
                //Otener puntos de usuario logeado en sistema
                int recipointsstatus = userLoggedOnSystem.getPuntos();
                precio = 5000;
                if(recipointsstatus>=precio){
                    RestarPtos(precio);
                    try {
                        enviarEmail();
                    } catch (MessagingException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    Toast.makeText(ReciShop.this,"NO CUENTA CON ECOPOINTS SUFICIENTES!!!!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Recuperamos puntos usuario logeado
                Usuario userLoggedOnSystem = InterfacesUtilities.recuperarUsuario(ReciShop.this);
                //Otener puntos de usuario logeado en sistema
                int recipointsstatus = userLoggedOnSystem.getPuntos();
                precio = 7000;
                if(recipointsstatus>=precio){
                    RestarPtos(precio);
                    try {
                        enviarEmail();
                    } catch (MessagingException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    Toast.makeText(ReciShop.this,"NO CUENTA CON ECOPOINTS SUFICIENTES!!!!",Toast.LENGTH_SHORT).show();
                }
            }
        });



        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Recuperamos puntos usuario logeado
                Usuario userLoggedOnSystem = InterfacesUtilities.recuperarUsuario(ReciShop.this);
                //Otener puntos de usuario logeado en sistema
                int recipointsstatus = userLoggedOnSystem.getPuntos();
                precio = 9000;
                if(recipointsstatus>=precio){
                    RestarPtos(precio);
                    try {
                        enviarEmail();
                    } catch (MessagingException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    Toast.makeText(ReciShop.this,"NO CUENTA CON ECOPOINTS SUFICIENTES!!!!",Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public void RestarPtos(int puntos){
        //Obtener usuario logeado en sistema en general
        Usuario recuperarUsuario = InterfacesUtilities.recuperarUsuario(getApplicationContext());
        //Recuperar ptos usuarios
        int ptosactuales = recuperarUsuario.getPuntos();
        ptosactuales -= puntos;
        //Actualizar ptos en usuario
        recuperarUsuario.setPuntos(ptosactuales);
        //Creamos usuario DAO
        UsuarioDAO usuarioDAO = new UsuarioDAOImpl(recuperarUsuario, ReciShop.this);
        typeChange = "restaptos";
        //Actualiza en firestore
        usuarioDAO.updateOnFireStore();
    }

    private void enviarEmail() throws MessagingException {

        Usuario userLoggedOnSystem = InterfacesUtilities.recuperarUsuario(ReciShop.this);
        String nombre = userLoggedOnSystem.getFull_name();

        String destinatarioCorreo = userLoggedOnSystem.getEmail();

        String subject = "RECOMPENSA CANJEADA!!!!";
        String content = "Hola : " + nombre +  "\n" + " Tu recompensa ah sido Canjeada Correctamente !!!" + "\n" +
                "Este es tu codigo:" +"834JGHF76HJDHX67834FVDASFSD";
        SendEmail.enviarMensaje(subject, content,destinatarioCorreo);
    }
}