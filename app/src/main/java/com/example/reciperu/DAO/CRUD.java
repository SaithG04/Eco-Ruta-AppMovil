package com.example.reciperu.DAO;

import com.example.reciperu.Entity.Usuario;
import com.example.reciperu.Utilities.DataAccessUtilities;

import java.util.ArrayList;
import java.util.Arrays;

public interface CRUD<T> {
    void listar(DataAccessUtilities.OnDataRetrievedListener<T> listener);

    void insertar();

    boolean actualizar();

    boolean eliminar();
}
