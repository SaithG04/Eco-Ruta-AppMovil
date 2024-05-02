const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.agregarUsuario = functions.firestore
    .document('usuarios/{usuarioId}')
    .onCreate((snap, context) => {
        // Obtiene los datos del nuevo usuario
        const nuevoUsuario = snap.data();
        
        // Verifica si el campo de fecha de registro es nulo
        if (!nuevoUsuario.fechaRegistro) {
            // Asigna la fecha y hora del servidor al campo de fecha de registro
            return snap.ref.update({
                fechaRegistro: admin.firestore.FieldValue.serverTimestamp()
            });
        } else {
            // El campo de fecha de registro ya tiene un valor, no es necesario actualizarlo
            return null;
        }
    });