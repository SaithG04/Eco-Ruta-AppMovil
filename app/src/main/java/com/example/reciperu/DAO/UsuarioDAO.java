package com.example.reciperu.DAO;

import com.example.reciperu.Entity.Usuario;
import com.example.reciperu.Utilities.DataAccessUtilities;

import java.lang.reflect.Method;

public interface UsuarioDAO extends CRUD<Usuario> {
    void getUserBy(Object parameter, DataAccessUtilities.OnDataRetrievedOneListener<Usuario> listener);
}
