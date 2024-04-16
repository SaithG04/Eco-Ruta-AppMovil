package com.qromarck.reciperu.DAO;

import com.qromarck.reciperu.DAO.DAOImplements.UsuarioDAOImpl;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.Utilities.DataAccessUtilities;

public interface UsuarioDAO extends CRUD<Usuario> {

    void getUserBy(Object parameter, DataAccessUtilities.OnDataRetrievedOneListener<Usuario> listener);

    void getUserOnFireBase(Object parameter, UsuarioDAOImpl.OnUserRetrievedListener listener);
}
