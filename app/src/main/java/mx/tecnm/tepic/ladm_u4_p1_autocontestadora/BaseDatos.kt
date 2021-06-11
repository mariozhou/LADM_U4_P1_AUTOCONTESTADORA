package mx.tecnm.tepic.ladm_u4_p1_autocontestadora

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BaseDatos(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(p0: SQLiteDatabase) {
       // p0.execSQL("CREATE TABLE LISTA(NOMBRE VARCHAR(50),CELULAR VARCHAR(50),MENSAJE VARCHAR(2000),STATUS BOOLEAN)")
        p0.execSQL("CREATE TABLE MENSAJES(ID INTEGER PRIMARY KEY AUTOINCREMENT, MENSAJE VARCHAR(500))")
        p0.execSQL("CREATE TABLE TELEFONOS(ID INTEGER PRIMARY KEY AUTOINCREMENT, TELEFONO VARCHAR(20), TIPO VARCHAR(5))")
        p0.execSQL("CREATE TABLE LLAMADASPERDIDAS(ID INTEGER PRIMARY KEY AUTOINCREMENT, TELEFONO VARCHAR(20), STATUS BOOLEAN)")

        /*
              p0.execSQL("CREATE TABLE LLAMADAS_ENTRANTES(ID INTEGER PRIMARY KEY AUTOINCREMENT,CELULAR VARCHAR(50),RESPONDIDO VARCHAR(2))")

              p0.execSQL("CREATE TABLE MENSAJES(ID INTEGER PRIMARY KEY AUTOINCREMENT,CONTENIDO VARCHAR(250))")

              p0.execSQL("CREATE TABLE TELEFONOS_REGISTRADOS(ID INTEGER PRIMARY KEY AUTOINCREMENT,CELULAR VARCHAR(40),TIPO VARCHAR(30))")


               */
    }

    override fun onUpgrade(p0: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

}