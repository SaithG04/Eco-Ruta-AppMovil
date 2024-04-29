package com.qromarck.reciperu.Interfaces;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.qromarck.reciperu.DAO.DAOImplements.UsuarioDAOImpl;
import com.qromarck.reciperu.DAO.UsuarioDAO;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.DataAccessUtilities;
import com.qromarck.reciperu.Utilities.EmailSender;
import com.qromarck.reciperu.Utilities.InterfacesUtilities;

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

    //SMS
    private static final String PHONE_NUMBER = "902207108"; // Número de teléfono de destino


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
                    //enviarRecompensaCorreo();
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
                    //enviarRecompensaCorreo();
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
                    //enviarRecompensaCorreo();
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

//    private void enviarRecompensaCorreo() {
//        //Obtener usuario logeado en sistema en general
//        Usuario recuperarUsuario = InterfacesUtilities.recuperarUsuario(getApplicationContext());
//        //Recuperar ptos usuarios
//        String Correousuario = recuperarUsuario.getEmail();
//        String NombreUsuario = recuperarUsuario.getFull_name();
//
//        String senderEmail = "reciperu4@gmail.com";
//        String senderPassword = "reciperu2024";
//        String recipientEmail = Correousuario;
//        String emailSubject = "RECOMPENSA CANJEADA!!!!";
//        String emailMessage = "Hola, " + NombreUsuario +"\n\n Gracias por Canjear la recompensa , Sigue ASI!!!. \n CODIGO: Z4237642843283426734267823443";
//
//        EmailSender emailSender = new EmailSender(senderEmail, senderPassword, recipientEmail, emailSubject, emailMessage);
//        emailSender.execute();
//    }



}