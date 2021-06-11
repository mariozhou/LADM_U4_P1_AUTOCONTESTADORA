package mx.tecnm.tepic.ladm_u4_p1_autocontestadora

import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.*
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    val nombreBaseDatos = "contesta"
    val siPermisoLLamada = 10
    val siPermisoLeerLLamada = 11
    val siPermisoEnviarMSM = 12
    val btn_NumerosAgradables= findViewById<Button>(R.id.btn_NumerosAgradables)
    val btn_NumerosDesagradables= findViewById<Button>(R.id.btn_NumerosDesagradables)
    val listaLLamadas= findViewById<ListView>(R.id.listaLLamadas)
    var hiloControl : HiloControl?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_CALL_LOG), siPermisoLLamada)
        }

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_PHONE_STATE), siPermisoLeerLLamada)
        }

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.SEND_SMS), siPermisoEnviarMSM)
        }
        hiloControl = HiloControl(this)
        hiloControl?.start()

        verificarBD()
        //cargarLLamadasPerdidas()

        btn_NumerosAgradables.setOnClickListener {
            var otraVentana = Intent(this, MainActivity2::class.java)
            startActivity(otraVentana)
        }

        btn_NumerosDesagradables.setOnClickListener {
            var otraVentana = Intent(this, MainActivity3::class.java)
            startActivity(otraVentana)
        }
    }

    fun actuaizarStatus(ID : String){
        try {
            var baseDatos = BaseDatos(this, nombreBaseDatos, null, 1)
            var actualizar = baseDatos.writableDatabase
            var SQL = "UPDATE LLAMADASPERDIDAS SET STATUS='true' WHERE ID=?"
            var parametros = arrayOf(ID)

            actualizar.execSQL(SQL, parametros)
            actualizar.close()
            baseDatos.close()
        } catch (error : SQLiteException) {
            mensaje(error.message.toString())
        }
    }

    fun enviarSMS() {
        try {
            var i = 0
            var baseDatos = BaseDatos(this, nombreBaseDatos, null, 1)
            var select = baseDatos.readableDatabase
            var SQL = "SELECT * FROM LLAMADASPERDIDAS"
            var cursor = select.rawQuery(SQL, null)

            if(cursor.count > 0) {
                cursor.moveToFirst()
                var cantidad = cursor.count-1

                (0..cantidad).forEach {
                    i++
                    if(cursor.getString(2) == "false" && (i % 2) == 0) {
                        if(statusPersona(cursor.getString(1)) == "AGRADABLE") {
                            actuaizarStatus(cursor.getString(0))
                            actuaizarStatus((cursor.getInt(0) + 1).toString())
                            SmsManager.getDefault().sendTextMessage(cursor.getString(1), null, obtenerMensaje(1), null, null)
                        } else if(statusPersona(cursor.getString(1)) == "NO AGRADABLE") {
                            actuaizarStatus(cursor.getString(0))
                            actuaizarStatus((cursor.getInt(0) + 1).toString())
                            SmsManager.getDefault().sendTextMessage(cursor.getString(1), null, obtenerMensaje(2), null, null)
                        }
                    }
                    cursor.moveToNext()
                }
            }

            select.close()
            baseDatos.close()
        } catch (error : SQLiteException){
            mensaje(error.message.toString())
        }
    }

    fun statusPersona(tel : String) : String {
        try {
            var baseDatos = BaseDatos(this, nombreBaseDatos, null, 1)
            var select = baseDatos.readableDatabase
            var SQL = "SELECT * FROM TELEFONOS"
            var cursor = select.rawQuery(SQL, null)

            if(cursor.count > 0) {
                cursor.moveToFirst()
                var cantidad = cursor.count-1

                (0..cantidad).forEach {
                    if(cursor.getString(1) == tel) {
                        if(cursor.getString(2) == "1") {
                            return "AGRADABLE"
                        } else if(cursor.getString(2) == "2") {
                            return "NO AGRADABLE"
                        }
                    } else {
                        return "IGNORADA"
                    }

                    cursor.moveToNext()
                }
            }

            select.close()
            baseDatos.close()
        } catch (error : SQLiteException){ }
        return "IGNORADA"
    }

    fun obtenerMensaje(tipo : Int) : String {
        /*
            tipo 1 = MENSAJE AMISTOSO
            tipo 2 = MENSAJE NO AMISTOSO
         */
        try {
            var baseDatos = BaseDatos(this, nombreBaseDatos, null, 1)
            var select = baseDatos.readableDatabase
            var SQL = "SELECT * FROM MENSAJES WHERE ID = ?"
            var parametros = arrayOf(tipo.toString())
            var cursor = select.rawQuery(SQL, parametros)

            if(cursor.moveToFirst()){
                return cursor.getString(1)
            }
            select.close()
            baseDatos.close()
        } catch (error : SQLiteException){ }
        return "ERROR"
    }
/*
    fun cargarLLamadasPerdidas() {
        try {
            var i = 0
            var baseDatos = BaseDatos(this, nombreBaseDatos, null, 1)
            var select = baseDatos.readableDatabase
            var SQL = "SELECT * FROM LLAMADASPERDIDAS ORDER BY ID DESC"
            var cursor = select.rawQuery(SQL, null)

            if(cursor.count > 0) {
                var arreglo = ArrayList<String>()
                cursor.moveToFirst()
                var cantidad = cursor.count-1
                (0..cantidad).forEach {
                    i++
                    if((i % 2) == 0){
                        var data = "ID: ${cursor.getString(0)} \nTeléfono: ${cursor.getString(1)} \nStatus: ${statusPersona(cursor.getString(1))} \nEstado: ${cursor.getString(2)}"
                        arreglo.add(data)
                    }
                    cursor.moveToNext()
                }
                listaLLamadas.adapter = ArrayAdapter<String>(this, R.layout.adaptadorlista, R.id.list_content, arreglo)
            }

            select.close()
            baseDatos.close()
        } catch (error : SQLiteException){
            mensaje(error.message.toString())
        }
    }
*/
    fun verificarBD(){
        try {
            var baseDatos = BaseDatos(this, nombreBaseDatos, null, 1)
            var select = baseDatos.readableDatabase
            var SQL = "SELECT * FROM MENSAJES WHERE ID = 1"
            var cursor = select.rawQuery(SQL, null)

            if(cursor.moveToFirst()){
                //SI HAY RESULTADO
            } else {
                //NO HAY RESULTADO
                agregarMensaje()
                agregarMensaje()
            }

            select.close()
            baseDatos.close()
        } catch (error : SQLiteException){ }
    }

    fun agregarMensaje() {
        var baseDatos = BaseDatos(this, nombreBaseDatos, null, 1)
        var insertar = baseDatos.writableDatabase
        var SQL = "INSERT INTO MENSAJES VALUES(NULL, '')"

        insertar.execSQL(SQL)
        insertar.close()
        baseDatos.close()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == siPermisoLLamada){
           // cargarLLamadasPerdidas()
        }
    }

    fun mensaje(mensaje : String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG)
                .show()
    }
}