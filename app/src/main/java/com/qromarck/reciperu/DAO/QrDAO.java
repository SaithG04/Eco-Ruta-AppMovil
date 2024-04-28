package com.qromarck.reciperu.DAO;

import com.qromarck.reciperu.DAO.DAOImplements.QrDAOImpl;
import com.qromarck.reciperu.DAO.DAOImplements.UsuarioDAOImpl;
import com.qromarck.reciperu.Entity.QR;

public interface QrDAO extends CRUD<QR> {

    void getQROnFireBase(Object parameter, QrDAOImpl.OnQrRetrievedListener listener);
}
