package com.example.proyectofinalredsocial

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val listapublicacionesM: ArrayList<DetallesPublicaciones>): RecyclerView.Adapter<MyAdapter.MyViewHolder>(){

    private lateinit var mListener : onItemClickListener

    interface  onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }

    fun deleteItem(i: Int){
        listapublicacionesM.removeAt(i)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.activity_lista_publicaciones,
            parent,false)
        return MyViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem = listapublicacionesM[position]

        holder.nombre.text = currentitem.nombre
        holder.fecha.text = currentitem.fecha
        holder.contenido.text = currentitem.contenido
    }


    override fun getItemCount(): Int {
        return listapublicacionesM.size
    }

    class MyViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val nombre : TextView = itemView.findViewById(R.id.lblNomPrincipal)
        val fecha : TextView = itemView.findViewById(R.id.lblFechaPrincipal)
        val contenido : TextView = itemView.findViewById(R.id.lblPublicacionPrincipal)

        init{
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)

            }
        }


    }
}