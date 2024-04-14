package com.example.reciperu.DAO.DAOImplements;

import android.content.Context;

import com.example.reciperu.DAO.UsuarioDAO;
import com.example.reciperu.Entity.Usuario;
import com.example.reciperu.Utilities.DataAccessUtilities;

public class UsuarioDAOImpl extends DataAccessUtilities implements UsuarioDAO {

    private Usuario usuario;
    private Context context;

    public UsuarioDAOImpl(Usuario usuario, Context context) {
        this.usuario = usuario;
        this.context = context;
    }

    @Override
    public Usuario listar() {

        return null;
    }

    @Override
    public boolean insertar() {

        return insertarGeneric("insertar_.php", new Object[]
                {0, usuario.getNombre(), usuario.getCorreo(), usuario.getHashedPassword(),
                        usuario.getSalt(), usuario.getStatus()}, context);

    }

    @Override
    public boolean actualizar() {
        return false;
    }

    @Override
    public boolean eliminar() {
        return false;
    }


}
