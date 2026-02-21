/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.marsphotos.data


import android.util.Base64
import com.example.marsphotos.network.bodyPerfil
import com.example.marsphotos.network.bodyacceso
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import android.util.Log
import com.example.marsphotos.model.ProfileStudent
import com.example.marsphotos.model.Usuario
import com.example.marsphotos.network.SICENETWService
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.security.MessageDigest


/**
 * Repository interface para SICENET
 */
interface SNRepository {
    suspend fun acceso(m: String, p: String): String
    suspend fun accesoObjeto(m: String, p: String): Usuario
    suspend fun profile(): ProfileStudent?
    fun hasSession(): Boolean
}

/**
 * Implementación de red para SICENET
 */
class NetworSNRepository(
    private val snApiService: SICENETWService
) : SNRepository {

    private var sessionCookie: String? = null
    private var currentMatricula: String? = null

    override fun hasSession(): Boolean =
        !sessionCookie.isNullOrEmpty()

    override suspend fun acceso(m: String, p: String): String {
        val soapFinal = bodyacceso.format(m.uppercase(), p)
        Log.d("SOAP_BODY_ACCESO", soapFinal) // Log del body enviado

        val body = soapFinal.toRequestBody("text/xml; charset=utf-8".toMediaType())
        val response = snApiService.acceso(body)

        //Log de respuesta
        Log.d("SOAP_RESPONSE_ACCESO", response.toString())


        val xml = response.body()?.string() ?: return "ERROR"
        // Log del XML recibido
        Log.d("SOAP_XML_ACCESO", xml)

        val resultRegex = "<accesoLoginResult>(.*?)</accesoLoginResult>".toRegex()
        val result = resultRegex.find(xml)?.groupValues?.get(1)

        return if (result != null && result.contains("true", ignoreCase = true)) {
            sessionCookie = response.headers()["Set-Cookie"]
            currentMatricula = m.uppercase()
            Log.d("LOGIN", "Resultado del repo: 'OK'")
            Log.d("COOKIE", sessionCookie ?: "sin cookie")
            "OK"
        } else {
            sessionCookie = null
            currentMatricula = null
            Log.d("LOGIN", "Resultado del repo: 'ERROR'")
            "ERROR"
        }
    }


    override suspend fun accesoObjeto(m: String, p: String): Usuario {
        return Usuario(matricula = m)
    }

    override suspend fun profile(): ProfileStudent? {
        if (!hasSession()) return null

        // Armar el body SOAP con la matrícula actual
        val bodyPerfilFormatted = bodyPerfil.format(currentMatricula ?: "")
        val body = bodyPerfilFormatted.toRequestBody("text/xml; charset=utf-8".toMediaType())

        // Llamada al servicio con la cookie de sesión
        val response = snApiService.perfil(cookie = sessionCookie ?: "", soap = body)

        val xml = response.body()?.string()
        Log.d("SOAP_PROFILE", xml ?: "sin respuesta")

        if (xml == null) return null

        // Extraer el bloque <getAlumnoAcademicoWithLineamientoResult>
        val resultRegex = "<getAlumnoAcademicoWithLineamientoResult>(.*?)</getAlumnoAcademicoWithLineamientoResult>"
            .toRegex(RegexOption.DOT_MATCHES_ALL)
        val result = resultRegex.find(xml)?.groupValues?.get(1)

        if (result.isNullOrBlank()) {
            Log.e("SOAP_PROFILE", "No se encontró el bloque de resultado")
            return null
        }

        return try {
            val jsonObj = org.json.JSONObject(result)
            ProfileStudent(
                matricula = jsonObj.optString("matricula", currentMatricula ?: ""),
                nombre = jsonObj.optString("nombre", "Alumno"),
                carrera = jsonObj.optString("carrera", ""),
                semestre = jsonObj.optString("semActual", ""),   // aquí tomamos el semestre real
                creditos = jsonObj.optString("cdtosAcumulados", "") //créditos acumulados
            )
        } catch (e: Exception) {
            Log.e("SOAP_PROFILE", "Error parseando JSON: ${e.message}")
            null
        }
    }



}
