package com.example.reciperu.DbHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
    // Nombre y versión de la base de datos
    private static final String DATABASE_NAME = "ReciPeru.db";
    private static final int DATABASE_VERSION = 1;

    // Constructor
    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Método que se llama la primera vez que se crea la base de datos
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE usuarios (" +
                "idUsuario INTEGER PRIMARY KEY AUTOINCREMENT," +
                "usuario TEXT," +
                "correo TEXT," +
                "contrasena TEXT)";
        db.execSQL(createTableQuery);
    }

    // Método que se llama si la base de datos necesita ser actualizada
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Puedes manejar las actualizaciones de la base de datos aquí
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        onCreate(db);
    }
}
