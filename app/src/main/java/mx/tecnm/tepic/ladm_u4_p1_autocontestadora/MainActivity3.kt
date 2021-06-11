package mx.tecnm.tepic.ladm_u4_p1_autocontestadora

import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity3 : AppCompatActivity() {
    val btn_GuardarMensajeD= findViewById<Button>(R.id.btn_GuardarMensajeD)
    val btn_GuardarTelefonoD= findViewById<Button>(R.id.btn_GuardarTelefonoD)
    val lista2= findViewById<ListView>(R.id.lista2)
    val txt_MensajeD= findViewById<EditText>(R.id.txt_MensajeD)
    val txt_TelefonoD= findViewById<EditText>(R.id.txt_TelefonoD)
    val nombreBaseDatos = "contesta"
    var listaID = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        cargarLista()
        cargarMensaje()

        btn_GuardarMensajeD.setOnClickListener {
            if(txt_MensajeD.text.isEmpty()) {
                mensaje("Favor de escribir un mensaje")
                return@setOnClickListener
            }

            actualizarMensaje(txt_MensajeD.text.toString())
            cerrarTeclado(txt_MensajeD)
        }

        btn_GuardarTelefonoD.setOnClickListener {
            if(txt_TelefonoD.text.isEmpty()){
                mensaje("Número de teléfono invalido")
                return@setOnClickListener
            }
            agregarTelefono(txt_TelefonoD.text.toString())
            txt_TelefonoD.setText("")
            cargarLista()

            cerrarTeclado(txt_TelefonoD)
        }
    }

    fun cerrarTeclado(editT : EditText) {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editT.getWindowToken(), 0)
    }

    fun cargarMensaje() {
        try {
            var baseDatos = BaseDatos(this, nombreBaseDatos, null, 1)
            var select = baseDatos.readableDatabase
            var SQL = "SELECT * FROM MENSAJES WHERE ID = 2"
            var cursor = select.rawQuery(SQL, null)

            if(cursor.moveToFirst()){
                //SI HAY RESULTADO
                txt_MensajeD.setText(cursor.getString(1))
            } else {
                //NO HAY RESULTADO
            }
            select.close()
            baseDatos.close()
        } catch (error : SQLiteException){ }
    }

    fun cargarLista() {
        try {
            var baseDatos = BaseDatos(this, nombreBaseDatos, null, 1)
            var select = baseDatos.readableDatabase
            var SQL = "SELECT * FROM TELEFONOS"
            var cursor = select.rawQuery(SQL, null)

            if(cursor.count > 0) {
                var arreglo = ArrayList<String>()
                this.listaID.clear()
                cursor.moveToFirst()
                var cantidad = cursor.count-1

                (0..cantidad).forEach {
                    if(cursor.getString(2) == "2"){
                        var data = "Teléfono: ${cursor.getString(1)} "
                        arreglo.add(data)
                        listaID.add(cursor.getString(0))
                    }
                    cursor.moveToNext()
                }

                lista2.adapter = ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, arreglo)
                lista2.setOnItemClickListener { parent, view, position, id ->
                    AlertDialog.Builder(this)
                            .setTitle("ATENCIÓN")
                            .setMessage("¿Qué deseas hacer con este teléfono?")
                            .setPositiveButton("Eliminar") {d, i ->
                                eliminarPorID(listaID[position])
                            }
                            .setNegativeButton("Cancelar") {d, i -> }
                            .show()
                }
            }

            select.close()
            baseDatos.close()
        } catch (error : SQLiteException){
            mensaje(error.message.toString())
        }
    }

    fun eliminarPorID(id : String) {
        try {
            var baseDatos = BaseDatos(this, nombreBaseDatos, null, 1)
            var eliminar = baseDatos.writableDatabase
            var SQL = "DELETE FROM TELEFONOS WHERE ID = ?"
            var parametros = arrayOf(id)

            eliminar.execSQL(SQL,parametros)
            eliminar.close()
            baseDatos.close()
            mensaje("SE ELIMINO CORRECTAMENTE")
            cargarLista()
        } catch (error : SQLiteException) {
            mensaje(error.message.toString())
        }
    }

    fun agregarTelefono(numero : String) {
        var baseDatos = BaseDatos(this, nombreBaseDatos, null, 1)
        var insertar = baseDatos.writableDatabase
        var SQL = "INSERT INTO TELEFONOS VALUES(NULL, '${numero}', '2')"

        insertar.execSQL(SQL)
        insertar.close()
        baseDatos.close()

        mensaje("SE INSERTO CORRECTAMENTE")
    }

    fun actualizarMensaje(mensaje : String) {
        try {
            var baseDatos = BaseDatos(this, nombreBaseDatos, null, 1)
            var actualizar = baseDatos.writableDatabase
            var SQL = "UPDATE MENSAJES SET MENSAJE='${mensaje}' WHERE ID=?"
            var parametros = arrayOf(2)

            actualizar.execSQL(SQL, parametros)
            actualizar.close()
            baseDatos.close()

            mensaje("SE ACTUALIZO CORRECTAMENTE")
        } catch (error : SQLiteException) {
            mensaje(error.message.toString())
        }
    }

    fun mensaje(mensaje : String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG)
                .show()
    }
}