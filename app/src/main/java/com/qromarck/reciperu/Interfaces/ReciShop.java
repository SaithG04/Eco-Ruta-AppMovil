package com.qromarck.reciperu.Interfaces;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
                precio = 5000;
                RestarPtos(precio);
                //enviarRecompensa();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                precio = 7000;
                RestarPtos(precio);
               // enviarRecompensa();
            }
        });



        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                precio = 9000;
                RestarPtos(precio);
               // enviarRecompensa();
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

//    private void enviarRecompensa() {
//        Usuario recuperarUsuario = InterfacesUtilities.recuperarUsuario(getApplicationContext());
//        Intent intent = new Intent(Intent.ACTION_SENDTO);
//        intent.setData(Uri.parse("mailto:" + recuperarUsuario.getEmail())); // Especifica el destinatario (correo electrónico)
//
//        intent.putExtra(Intent.EXTRA_SUBJECT, "RECOMPENSA CANJEADA ECOPERU!!!!"); // Asunto del correo
//
//        String message = "Hola," +recuperarUsuario.getFull_name()+"\n\n Gracias por preferir ECOPeru aqui esta tu Recompensa Canjeada. \n ENTRADA CODIGO: XXXXXXXXXXXXXXXX";
//        intent.putExtra(Intent.EXTRA_TEXT, message); // Cuerpo del correo
//
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivity(Intent.createChooser(intent, "Enviar Correo"));
//        } else {
//            Toast.makeText(this, "No se encontró ninguna aplicación de correo.", Toast.LENGTH_SHORT).show();
//        }
//    }

}