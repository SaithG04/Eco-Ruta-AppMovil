package com.example.reciperu.DAO;

import com.example.reciperu.Entity.Usuario;
import com.example.reciperu.Utilities.DataAccessUtilities;

public interface UsuarioDAO extends CRUD<Usuario> {
    void getByUsername(DataAccessUtilities.OnDataRetrievedOneListener<Usuario> listener);
    void getByEmail(DataAccessUtilities.OnDataRetrievedOneListener<Usuario> listener);
}
