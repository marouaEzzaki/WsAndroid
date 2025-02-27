package com.example.a12_rest_01;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a12_rest_01.adaptador.AdaptadorUsuario;
import com.example.a12_rest_01.modelo.entidad.Usuario;
import com.example.a12_rest_01.modelo.negocio.GestorUsuario;
import com.example.a12_rest_01.modelo.servicio.GoRestUsuarioApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
Para hacer llamadas a un API Rest debemos de importar alguna librería de terceros,
que en nuestro caso será RetroFit.

Para ello agregamos seguimos los sigientes pasos:
1- En el fichero "libs.versions.toml" agregamos:

    retrofit = "2.11.0" en la parte de [versions]
    retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" } en la parte de [libraries]

Notese que podemos cambiar la versión por la más actual

2- En el fichero "build.gradle.kts" agregamos:

    implementation(libs.retrofit)

Retrofit es un cliente REST extremadamente simple de configurar.
Nos permitirá tratar las llamadas a la API como funciones Java, así definiremos
solamente las URLs que queremos llamar y los tipos de petición y respuesta que
esperamos. Retrofit requiere un de Java 8+ o Android API 21+.

https://square.github.io/retrofit/

Tambien agregaremos alguna librería que nos permita trabajar con Json. En este
caso usaremos Gson, una libreria de google muy popular para serializar y
deserializar objetos en formato Json.

    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.google.code.gson:gson:2.8.9'

Tambien debemos dar permisos en nuestra aplicacion para contectarnos a internet.
Tenemos que agregar la siguiente sentencia en el AndroidManifest.xml

    <uses-permission android:name="android.permission.INTERNET"/>

Siempre que agregamos liberías a nuestro proyecto, debemos de construirlo de
nuevo (Make proyect).

Para nuestro ejemplo vamos a usar un servicio Rest remoto llamado

    https://gorest.co.in/

 */

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewUser;
    private AdaptadorUsuario adaptadorUsuario;
    private Button botonRefrescar;
    private TextView tvVacio;
    ProgressDialog mDefaultDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        GestorUsuario.getInstance().inicializar();

        recyclerViewUser = findViewById(R.id.rViewUsuario);
        recyclerViewUser.setHasFixedSize(true);

        tvVacio = findViewById(R.id.tvVacio);

        recyclerViewUser.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.VERTICAL,
                        false));

        botonRefrescar = findViewById(R.id.botonRefrescar);

        botonRefrescar.setOnClickListener(v -> {
            obtenerListaUsuariosRest();
        });
    }

    //Cada vez que mostramos esta activity, volvemos a cargar los datos
    @Override
    protected void onResume() {
        super.onResume();
        obtenerListaUsuariosRest();
    }

    public void obtenerListaUsuariosRest(){
        mostrarEspera();

        GoRestUsuarioApiService goRestUsuarioApiService =
                GestorUsuario.getInstance().getGoRestUserApiService();

        Call<List<Usuario>> call = goRestUsuarioApiService.getUsuarios();

        //Como no sabemos el tiempo que va a tardar en responder el servicio
        //rest, no podemos bloquear la aplicacion movil al usuario.
        //Por ello, debemos de hacer siempre las peticiones a servicios de manera
        //asincrona, es decir, no bloqueamos la pantalla del movil y cuando
        //el servidor conteste, entonces se ejecutara un determinado método.
        //Este método se suele llamar función de callback o retro llamada

        //En este ejemplo de aqui, con el método enqueue estamos haciendo
        //la solicitud al servidio rest
        call.enqueue(new Callback<List<Usuario>>() {
            //Este método se ejecutará cuando el servidor HTTP nos responda
            //satisfactoriamente
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                if (response.isSuccessful()) {
                    Log.d("Success", "Datos traidos del servicio");
                    //Gracias a Gson, me convierte los json a objetos Usuario
                    List<Usuario> listaUsuarios = response.body();

                    adaptadorUsuario = new AdaptadorUsuario(listaUsuarios);
                    recyclerViewUser.setAdapter(adaptadorUsuario);

                    //En caso de que la lista que nos traemos este vacia
                    //ocultamos la vista y mostramos un textview de vacio
                    if (listaUsuarios.isEmpty()) {
                        recyclerViewUser.setVisibility(View.GONE);
                        tvVacio.setVisibility(View.VISIBLE);
                    }
                    else {
                        recyclerViewUser.setVisibility(View.VISIBLE);
                        tvVacio.setVisibility(View.GONE);
                    }
                } else {
                    Log.d("Error", response.code() + " " + response.message());
                    return;
                }

                cancelarEspera();
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d("Error", t.toString());
                cancelarEspera();
            }
        });
    }

    public void mostrarEspera() {
        mDefaultDialog = new ProgressDialog(this);
        // El valor predeterminado es la forma de círculos pequeños
        mDefaultDialog.setProgressStyle (android.app.ProgressDialog.STYLE_SPINNER);
        mDefaultDialog.setMessage("Solicitando datos ...");
        mDefaultDialog.setCanceledOnTouchOutside(false);// Por defecto true
        mDefaultDialog.show();
    }

    public void cancelarEspera(){
        mDefaultDialog.cancel();
    }
}