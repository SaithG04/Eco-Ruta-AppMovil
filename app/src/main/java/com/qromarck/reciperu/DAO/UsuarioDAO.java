package com.qromarck.reciperu.DAO;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.qromarck.reciperu.DAO.DAOImplements.UsuarioDAOImpl;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.Utilities.DataAccessUtilities;

import java.util.List;

public interface UsuarioDAO extends CRUD<Usuario> {

    void getUserOnFireBase(Object parameter, OnSuccessListener<List<Usuario>> onSuccessListener, OnFailureListener onFailureListener);
}
