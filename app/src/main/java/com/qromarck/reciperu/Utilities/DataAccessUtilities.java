package com.qromarck.reciperu.Utilities;

import static com.qromarck.reciperu.Utilities.InterfacesUtilities.*;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.j2objc.annotations.AutoreleasePool;
import com.qromarck.reciperu.Entity.Usuario;

import org.checkerframework.checker.units.qual.A;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class DataAccessUtilities {

    public static final String URL = "https://reciperu2024.000webhostapp.com/";
    private static final int MY_SOCKET_TIMEOUT_MS = 5000; // 5 segundos
    public static String message = "";
    private boolean success = false;
    public static Usuario userLoggedOnSystem;
    private CollectionReference reference;


    public interface OnInsertionListener {
        void onInsertionSuccess();

        void onInsertionError(String errorMessage);
    }

    public interface OnUpdateListener {
        void onUpdateComplete();

        void onUpdateError(String errorMessage);
    }


    public void insertOnFireStore(String collectionName, String documentId, Map<String, Object> data,
                                  OnInsertionListener insertionListener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(collectionName).document(documentId).set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        if (insertionListener != null) {
                            insertionListener.onInsertionSuccess();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace(System.out);
                        if (insertionListener != null) {
                            insertionListener.onInsertionError(e.getMessage());
                        }
                    }
                });
    }

    public void updateOnFireStore(String collectionName, String documentId, Map<String, Object> data,
                                  OnUpdateListener updateListener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(collectionName).document(documentId).update(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (updateListener != null) {
                            updateListener.onUpdateComplete();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (updateListener != null) {
                            updateListener.onUpdateError(e.getMessage());
                        }
                    }
                });
    }


    public <T> Task<List<T>> getByCriteria(@NonNull Class<T> clazz, String campo, Object value) {
        CollectionReference collectionRef = FirebaseFirestore.getInstance().collection(clazz.getSimpleName().toLowerCase() + "s");

        TaskCompletionSource<List<T>> taskCompletionSource = new TaskCompletionSource<>();

        collectionRef.whereEqualTo(campo, value)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                            // Obtener todos los datos del documento como un mapa
                            Map<String, Object> data = documentSnapshot.getData();

                            // Iterar sobre las entradas del mapa e imprimir los datos
                            for (Map.Entry<String, Object> entry : data.entrySet()) {
                                String key = entry.getKey();
                                Object value = entry.getValue();
                                System.out.println(key + ": " + value);
                            }
                        }
                        List<T> entities = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                            // Convertir el documento a un objeto de la clase proporcionada (utilizando reflexión)
                            T entity = documentSnapshot.toObject(clazz);
                            // Agregar la entidad a la lista
                            entities.add(entity);
                        }
                        // Completa la tarea con la lista de entidades
                        taskCompletionSource.setResult(entities);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Completa la tarea con un error si la lectura falla
                        taskCompletionSource.setException(e);
                        e.printStackTrace(System.out);
                    }
                });

        return taskCompletionSource.getTask();
    }

    public static void insertOnFireStoreRealtime(String collectionName, String documentId, Map<String, Object> data,
                                  OnInsertionListener insertionListener) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = db.getReference(collectionName).child(documentId);

        databaseReference.setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if (insertionListener != null) {
                    insertionListener.onInsertionSuccess();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (insertionListener != null) {
                    insertionListener.onInsertionError(e.getMessage());
                    e.printStackTrace(System.err);
                }
            }
        });
    }

    @Deprecated
    @RequiresApi(api = Build.VERSION_CODES.O)
    public <T> void insertarGeneric(RequestQueue requestQueue, String tableName, T entity, OnInsertionListener listener) {
        try {
            String scriptPhp = determineScript(tableName);
            obtenerColumnasTabla(requestQueue, tableName, new OnColumnasObtenidasListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onColumnasObtenidas(ArrayList<String> columnas) {
                    try {
                        Map<String, Object> parametros = buildParameterMap(entity, columnas);
                        StringRequest stringRequest = createStringRequest(scriptPhp, parametros, listener);
                        requestQueue.add(stringRequest);
                    } catch (Exception e) {
                        e.printStackTrace(System.out);
                        if (listener != null) {
                            listener.onInsertionError("Error desconocido.");
                        }
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    System.out.println("Error: " + errorMessage);
                    if (listener != null) {
                        listener.onInsertionError("Error desconocido.");
                    }
                }
            });
        } catch (Exception exception) {
            exception.printStackTrace(System.out);
            if (listener != null) {
                listener.onInsertionError("Error desconocido.");
            }
        }
    }

    @Deprecated
    public <T> void listarGeneric(RequestQueue requestQueue, String nombreTabla, OnDataRetrievedListener<T> listener) {
        ArrayList<T> entityArrayList = new ArrayList<>(); // Declaración de la lista dentro del método
        try {
            String url = URL + "listar_tabla.php";

            // Crear una solicitud POST
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        /** @noinspection unchecked*/
                        @Override
                        public void onResponse(String response) {
                            // Imprimir la respuesta recibida antes de intentar analizarla como JSON
                            System.out.println("Response received: " + response);

                            try {
                                JSONArray jsonArray = new JSONArray(response); // response es la cadena que recibes del servidor
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    if (nombreTabla.equals("usuarios")) {
                                        Usuario usuario = new Usuario();
//                                        usuario.setId(jsonObject.getInt("id"));
                                        usuario.setFull_name(jsonObject.getString("usuario"));
                                        usuario.setEmail(jsonObject.getString("correo"));
                                        String hashedPasswordHex = jsonObject.getString("hashedPassword");
                                        String salt = jsonObject.getString("salt");
                                        byte[] hashedPasswordBytes = hexStringToByteArray(hashedPasswordHex);
                                        byte[] saltBytes = hexStringToByteArray(salt);
//                                        usuario.setHashedPassword(hashedPasswordBytes);
//                                        usuario.setSalt(saltBytes);
                                        usuario.setStatus(jsonObject.getString("status"));
                                        System.out.println(usuario.toString());
                                        entityArrayList.add((T) usuario);
                                    }
                                }
                                // Notificar al listener que se han recuperado los datos correctamente
                                listener.onDataRetrieved(entityArrayList);
                            } catch (JSONException e) {
                                // Manejar el error de análisis JSON.
                                if (listener != null) {
                                    listener.onError("Error desconocido.");
                                }
                                e.printStackTrace(System.out);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Manejar el error de la solicitud HTTP
                            if (listener != null) {
                                listener.onError("Error de red.");
                            }
                            error.printStackTrace(System.out);
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    // Agregar parámetros POST si es necesario
                    Map<String, String> params = new HashMap<>();
                    params.put("tableName", nombreTabla);
                    return params;
                }
            };

            // Agregar la solicitud a la cola de solicitudes.
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            // Manejar cualquier otro error
            if (listener != null) {
                listener.onError("Error desconocido.");
            }
            e.printStackTrace(System.out);
        }
    }

    @Deprecated
    public <T> void getEntityByParameter(RequestQueue requestQueue, String nombreTabla, String parameterName, Object parameter, String parameterType, OnDataRetrievedOneListener<T> listener) {
        try {
            String url = URL + "get_entity_by_parameter.php";

            // Crear una solicitud POST
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Imprimir la respuesta recibida antes de intentar analizarla como JSON
                            System.out.println("Response received: " + response);
                            try {
                                JSONObject jsonObject = new JSONObject(response); // response es la cadena que recibes del servidor
                                T entity = null;
                                if (nombreTabla.equals("usuarios")) {
                                    Usuario usuario = new Usuario();
//                                    usuario.setId(jsonObject.getInt("id"));
                                    usuario.setFull_name(jsonObject.getString("usuario"));
                                    usuario.setEmail(jsonObject.getString("correo"));
                                    String hashedPasswordHex = jsonObject.getString("hashedPassword");
                                    String salt = jsonObject.getString("salt");
                                    byte[] hashedPasswordBytes = hexStringToByteArray(hashedPasswordHex);
                                    byte[] saltBytes = hexStringToByteArray(salt);
//                                    usuario.setHashedPassword(hashedPasswordBytes);
//                                    usuario.setSalt(saltBytes);
                                    usuario.setStatus(jsonObject.getString("status"));
                                    System.out.println(usuario.toString());
                                    entity = (T) usuario;
                                }
                                // Notificar al listener que se han recuperado los datos correctamente
                                listener.onDataRetrieved(entity);
                            } catch (JSONException e) {
                                // Manejar el error de análisis JSON.
                                if (listener != null) {
                                    listener.onError(Objects.requireNonNull(e.getMessage()).contains("No value for id") ? "Usuario no encontrado." : "Error desconocido");
                                }
                                e.printStackTrace(System.out);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Manejar el error de la solicitud HTTP
                            if (listener != null) {
                                listener.onError("Error de red.");
                            }
                            error.printStackTrace(System.out);
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    // Agregar parámetros POST si es necesario
                    Map<String, String> params = new HashMap<>();
                    params.put("tableName", nombreTabla);
                    params.put("parameterName", parameterName);
                    params.put("parameter", String.valueOf(parameter));
                    params.put("parameterType", parameterType);
                    return params;
                }
            };

            // Agregar la solicitud a la cola de solicitudes.
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            // Manejar cualquier otro error
            if (listener != null) {
                listener.onError("Error desconocido.");
            }
            e.printStackTrace(System.out);
        }
    }

    @Deprecated
    private StringRequest createStringRequest(String scriptPhp, Map<String, Object> parametros, OnInsertionListener listener) {
        return new StringRequest(Request.Method.POST, URL + scriptPhp,
                response -> {
                    System.out.println("Response: " + response);
                    if (response.equals("Datos insertados correctamente.")) {
                        if (listener != null) {
                            listener.onInsertionSuccess();
                        }
                    } else {
                        if (listener != null) {
                            listener.onInsertionError("Error desconocido");
                        }
                    }
                },
                error -> {
                    error.printStackTrace(System.out);
                    if (listener != null) {
                        listener.onInsertionError("Error de red.");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // Aquí necesitas convertir el mapa de parámetros a un mapa de cadenas
                Map<String, String> stringParams = new HashMap<>();
                for (Map.Entry<String, Object> entry : parametros.entrySet()) {
                    stringParams.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
                return stringParams;
            }
        };
    }

    @Deprecated
    private String determineScript(String tableName) {
        switch (tableName) {
            case "usuarios":
                return "insertar_usuario.php";
            default:
                return null;
        }
    }

    @Deprecated
    @RequiresApi(api = Build.VERSION_CODES.O)
    private <T> Map<String, Object> buildParameterMap(T entity, ArrayList<String> columnas) {
        Map<String, Object> parametros = new HashMap<>();
        Object[] objects = new InterfacesUtilities().entityToObjectArray(entity);
        for (int i = 0; i < columnas.size(); i++) {
            parametros.put(columnas.get(i), objects[i]);
        }
        System.out.println("PARAMETROS: " + parametros);
        return parametros;
    }

    @Deprecated
    public void obtenerColumnasTabla(RequestQueue requestQueue, String nombreTabla, final OnColumnasObtenidasListener listener) {
        // Define la URL para la solicitud
        try {
            String url = URL + "get_columns.php";
            ArrayList<String> nameColumns = new ArrayList<>();

            // Crea una solicitud POST
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                // Verifica si la respuesta es un JSON válido.
                                JSONArray jsonArray = new JSONArray(response);
                                System.out.println(response);
                                // Itera sobre el array JSON y extrae los nombres de las columnas
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    String columnName = jsonArray.getString(i);
                                    nameColumns.add(columnName);
                                }
                                if (listener != null) {
                                    // Llama al método de callback para notificar al cliente que se han obtenido los nombres de las columnas
                                    listener.onColumnasObtenidas(nameColumns);
                                }
                            } catch (JSONException e) {
                                // Maneja el error de análisis JSON.
                                success = false;
                                message = e.getMessage();
                                e.printStackTrace(System.out);
                                if (listener != null) {
                                    listener.onError("Error desconocido.");
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace(System.out);
                            if (listener != null) {
                                listener.onError("Error de red.");
                                message = error.toString();
                                success = false;
                            }
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("tableName", nombreTabla);
                    return params;
                }
            };
            // Agrega la solicitud a la cola de solicitudes.
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            success = false;
            message = e.getMessage();
        }
    }

    @Deprecated
    public interface OnColumnasObtenidasListener {
        void onColumnasObtenidas(ArrayList<String> columnas);

        void onError(String errorMessage);
    }

    @Deprecated
    public interface OnDataRetrievedOneListener<T> {
        void onDataRetrieved(T entity);

        void onError(String errorMessage);
    }

    @Deprecated
    public interface OnDataRetrievedListener<T> {
        void onDataRetrieved(ArrayList<T> data);

        void onError(String errorMessage);
    }

}