package com.example.proyectofinalredsocial

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinalredsocial.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    private lateinit var publiRecyclerView: RecyclerView
    private lateinit var detallesPublicacionesArrayList: ArrayList<DetallesPublicaciones>


    private var db = Firebase.firestore
    private val resultLaucher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val respuesta = IdpResponse.fromResultIntent(it.data)
        Log.i("response", respuesta.toString())
        if (it.resultCode == RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                Toast.makeText(this, "Bienvenido", Toast.LENGTH_LONG).show()
            }
        } else {
            if (respuesta == null) {
                Toast.makeText(this, "Hasta pronto", Toast.LENGTH_LONG).show()
                finish()
            } else {
                respuesta.error?.let {
                    if (it.errorCode == ErrorCodes.NO_NETWORK) {
                        Toast.makeText(this, "No cuentas con internet", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        Toast.makeText(
                            this,
                            "Código de error: ${it.errorCode}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Novedades"
        val swipe = binding.refrescar
        swipe.setOnRefreshListener {
            refrescarDatos()
            swipe.isRefreshing = false
        }
        mostrarPublicaciones()
        Autentificacion()
        publicar()

    }

    private fun refrescarDatos() {

        finish()
        overridePendingTransition(0, 0)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private fun mostrarPublicaciones() {

        //probar si aqui puedo recargar el activity y no haya loop

        publiRecyclerView = binding.listapublicacionesM
        publiRecyclerView.layoutManager = LinearLayoutManager(this)
        publiRecyclerView.setHasFixedSize(true)
        detallesPublicacionesArrayList = arrayListOf<DetallesPublicaciones>()

        db.collection("publicaciones").orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    detallesPublicacionesArrayList.add(DetallesPublicaciones("${document.id}","${document.data.get("nombre")}",
                        "${document.data.get("fecha")}", "${document.data.get("contenido")}"))

                    var adapter = MyAdapter(detallesPublicacionesArrayList)
                    publiRecyclerView.adapter = adapter
                    adapter.setOnItemClickListener(object : MyAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            //Toast.makeText(this@MainActivity, "Se dio click con eito", Toast.LENGTH_LONG).show()
                        }
                    })

                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }

    }

    private fun Autentificacion() {
        firebaseAuth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            if (auth.currentUser != null) {
                supportActionBar?.title = "Novedades"
                binding.txtNombre.text =
                    "Hola, bienvenido de nuevo: ${auth.currentUser?.displayName}"
                //supportActionBar?.title = auth.currentUser?.displayName
                binding.llProgress.visibility = View.GONE
                binding.txtNombre.visibility = View.VISIBLE
                binding.listapublicacionesM.visibility = View.VISIBLE
                binding.floatingActionButton.visibility = View.VISIBLE
            } else {
                val provedores = arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build(),
                    AuthUI.IdpConfig.FacebookBuilder().build()
                )
                resultLaucher.launch(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(provedores)
                        .setLogo(R.drawable.icono_login)
                        .setIsSmartLockEnabled(false)
                        .build()
                )
            }
        }
    }
    private fun publicar(){
        binding.floatingActionButton.setOnClickListener {

            val i = Intent(this, NuevaPublicacionActivity::class.java)
            startActivity(i)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sign_out -> {
                AuthUI.getInstance().signOut(this)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Sesión cerrada con exito", Toast.LENGTH_LONG).show()
                    }
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            binding.llProgress.visibility = View.VISIBLE
                            binding.txtNombre.visibility = View.GONE
                            binding.listapublicacionesM.visibility = View.GONE
                            binding.floatingActionButton.visibility = View.GONE
                        } else {
                            Toast.makeText(this, "Error al cerrar la sesión", Toast.LENGTH_LONG).show()
                        }
                    }
            }
            R.id.action_refrescar ->{
            refrescarDatos()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

    override fun onBackPressed() {
        Toast.makeText(this, "Para cerrar sesion porfavor revisa el menu", Toast.LENGTH_SHORT).show()
    }

}