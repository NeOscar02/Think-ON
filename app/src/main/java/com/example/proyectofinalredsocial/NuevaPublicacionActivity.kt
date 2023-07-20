package com.example.proyectofinalredsocial

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinalredsocial.databinding.NuevaPublicacionBinding
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class NuevaPublicacionActivity : AppCompatActivity() {

    private lateinit var binding: NuevaPublicacionBinding
    private lateinit var auth: FirebaseAuth

    private lateinit var publiRecyclerView: RecyclerView
    private lateinit var detallesPublicacionesArrayList: ArrayList<DetallesPublicaciones>
    private lateinit var detallesPublicacionesPersonalArrayList: ArrayList<DetallesPublicaciones>
    private lateinit var identificador: String

    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        binding = NuevaPublicacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            Publicacion()
            mostrarPublicaciones()
        }
    }

    private fun refrescarDatos() {
        finish()
        val i = Intent(this, NuevaPublicacionActivity::class.java)
        startActivity(i)
    }

    private fun Publicacion() {
        binding.btnPublicar.setOnClickListener {
            agregar()
        }
    }

    private fun agregar() {
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm")
        val currentDate = sdf.format(Date())

        val user = auth.currentUser
        user?.let {
            val name = user.displayName

            val EstructuraPublicacion = hashMapOf(
                "contenido" to binding.txtEscribir.text.toString(),
                "nombre" to name,
                "fecha" to currentDate
            )
            db.collection("publicaciones")
                .add(EstructuraPublicacion)
                .addOnSuccessListener { documentReference ->
                    binding.txtEscribir.setText("")
                    val i = Intent(this, MainActivity::class.java)
                    startActivity(i)
                    Toast.makeText(this, "¡Genial, ya se Ha publicado!", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al crear tu publicación", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun mostrarPublicaciones() {

        val user = auth.currentUser
        user?.let {
            val name = user.displayName

            publiRecyclerView = binding.mispublicaciones
            publiRecyclerView.layoutManager = LinearLayoutManager(this)
            publiRecyclerView.setHasFixedSize(true)
            publiRecyclerView.addItemDecoration(
                DividerItemDecoration(
                    this,
                    DividerItemDecoration.VERTICAL
                )
            )
            detallesPublicacionesArrayList = arrayListOf<DetallesPublicaciones>()
            detallesPublicacionesPersonalArrayList = arrayListOf<DetallesPublicaciones>()

            db.collection("publicaciones").orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {

                        detallesPublicacionesArrayList.add(
                            DetallesPublicaciones(
                                "${document.id}", "${document.data.get("nombre")}",
                                "${document.data.get("fecha")}", "${document.data.get("contenido")}"
                            )
                        )
                        //publiRecyclerView.adapter = MyAdapter(detallesPublicacionesArrayList)
                    }
                    for (nuevoArreglo in detallesPublicacionesArrayList) {
                        if (nuevoArreglo.nombre.toString() == name) {
                            detallesPublicacionesPersonalArrayList.add(nuevoArreglo)
                            //println(nuevoArreglo)
                            var adapter = MyAdapter(detallesPublicacionesPersonalArrayList)

                            publiRecyclerView.adapter = adapter
                            adapter.setOnItemClickListener(object : MyAdapter.onItemClickListener {
                                override fun onItemClick(position: Int) {

                                    identificador = detallesPublicacionesPersonalArrayList[position].id.toString()
                                    binding.txtEscribir.setText(detallesPublicacionesPersonalArrayList[position].contenido.toString())
                                    binding.btnPublicar.visibility = View.GONE
                                    binding.btnEditar.visibility = View.VISIBLE

                                    val currentLayout = binding.mispublicaciones.layoutParams as ConstraintLayout.LayoutParams // btn is a View here
                                    currentLayout.topToBottom = R.id.btnEditar // resource ID of new parent field
                                    binding.mispublicaciones.layoutParams = currentLayout
                                    binding.btnEliminar.visibility = View.VISIBLE

                                    binding.btnEliminar.setOnClickListener {
                                        db.collection("publicaciones").document(identificador)
                                            .delete()
                                            .addOnSuccessListener {
                                                Toast.makeText(this@NuevaPublicacionActivity, "La publicacion se elimino con exito", Toast.LENGTH_LONG).show()
                                                refrescarDatos()
                                            }
                                            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e)}
                                    }

                                    binding.btnEditar.setOnClickListener {
                                        db.collection("publicaciones").document(identificador)
                                            .update(mapOf(
                                                "contenido" to binding.txtEscribir.text.toString(),
                                                "fecha" to "${detallesPublicacionesPersonalArrayList[position].fecha.toString()} - editado"
                                            ))
                                            .addOnSuccessListener {
                                                Toast.makeText(this@NuevaPublicacionActivity, "La publicacion se ha actualizado", Toast.LENGTH_LONG).show()
                                                refrescarDatos()
                                            }
                                            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e)}
                                    }
                                    //Toast.makeText(this@NuevaPublicacionActivity, "click ${identificador}", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                        //println("Nuevo arreglo ${detallesPublicacionesPersonalArrayList}")
                        //println("este es el nuevo arreglo ${detallesPublicacionesPersonalArrayList}")
                        //println("este es el arreglo ${nuevoA}")
                    }
                    //println("este es el arreglo ${detallesPublicacionesArrayList[0].nombre}")
                }
                .addOnFailureListener { exception -> Log.d(TAG, "Error getting documents: ", exception) }
        }
    }

    //---------------------------------------------------------------------------------------------------------//
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sign_out -> {
                AuthUI.getInstance().signOut(this)
                    .addOnSuccessListener {
                        finish()
                        Toast.makeText(this, "Sesión cerrada con exito", Toast.LENGTH_LONG).show()
                    }
                    .addOnCompleteListener {
                        if (it.isSuccessful) {

                        } else {
                            Toast.makeText(this, "Error al cerrar la sesión", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
            }
            R.id.action_refrescar -> {
                refrescarDatos()
            }
        }
        return super.onOptionsItemSelected(item)
    }


}
