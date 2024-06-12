package com.qromarck.reciperu.DAO;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.qromarck.reciperu.DAO.DAOImplements.QrDAOImpl;
import com.qromarck.reciperu.DAO.DAOImplements.UsuarioDAOImpl;
import com.qromarck.reciperu.Entity.QR;

import java.util.List;

public interface QrDAO extends CRUD<QR> {

    void getQROnFireBase(Object parameter, OnSuccessListener<List<QR>>onSuccessListener, OnFailureListener onFailureListener);
}
