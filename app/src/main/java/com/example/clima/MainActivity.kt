package com.example.clima

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {

    private lateinit var tvResult: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Registra o contrato que lida com a resposta do usuário (Aceitou ou Negou a permissão)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Requisito: Tratar permissão concedida
                buscarClimaPorLocalizacao()
            } else {
                // Requisito: Tratar permissão negada sem quebrar o app, mostrando aviso explicativo
                tvResult.text = "Permissão de localização negada.\nVocê ainda pode buscar digitando o nome da cidade."
                Toast.makeText(this, "Acesso ao GPS foi negado.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etCityName = findViewById<EditText>(R.id.etCityName)
        val btnSearch = findViewById<Button>(R.id.btnSearch)
        val btnLocation = findViewById<Button>(R.id.btnLocation)
        tvResult = findViewById(R.id.tvResult)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Fluxo 1: Busca pelo nome da cidade (Mantido da Parte 1)
        btnSearch.setOnClickListener {
            val cidade = etCityName.text.toString().trim()
            if (cidade.isEmpty()) {
                Toast.makeText(this, "Por favor, digite o nome de uma cidade.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            buscarClimaNaApi(cidade)
        }

        // Fluxo 2: Busca usando permissão de Localização (Nova funcionalidade)
        btnLocation.setOnClickListener {
            verificarPermissaoDeLocalizacao()
        }
    }

    private fun verificarPermissaoDeLocalizacao() {
        // Requisito: Verificar se a permissão já foi concedida antes de pedir
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Já tem permissão
                buscarClimaPorLocalizacao()
            }
            else -> {
                // Requisito: Solicitar a permissão em tempo de execução (Runtime)
                tvResult.text = "Solicitando permissão de localização..."
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun buscarClimaPorLocalizacao() {
        tvResult.text = "Buscando localização atual..."
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    // A API wttr.in aceita coordenadas diretamente
                    val coordenadas = "$lat,$lon"
                    buscarClimaNaApi(coordenadas)
                } else {
                    tvResult.text = "Não foi possível obter a localização. Verifique se o GPS está ligado."
                }
            }
        } catch (e: SecurityException) {
            tvResult.text = "Erro de segurança ao acessar a localização."
        }
    }

    private fun buscarClimaNaApi(parametroBusca: String) {
        tvResult.text = "Buscando o clima..."

        // Troca espaços em branco por %20 caso o usuário tenha digitado cidade com espaço
        val parametroFormatado = parametroBusca.replace(" ", "%20")
        val url = "https://wttr.in/${parametroFormatado}?format=j1"

        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val current = response.getJSONArray("current_condition").getJSONObject(0)
                    val temp = current.getString("temp_C")
                    val umidade = current.getString("humidity")
                    val vento = current.getString("windspeedKmph")

                    val areaInfo = response.getJSONArray("nearest_area").getJSONObject(0)
                    val nomeLocal = areaInfo.getJSONArray("areaName").getJSONObject(0).getString("value")

                    val resultado = """
                        Local: $nomeLocal
                        Temperatura: $temp °C
                        Umidade: $umidade%
                        Vento: $vento km/h
                    """.trimIndent()

                    tvResult.text = resultado
                } catch (e: Exception) {
                    tvResult.text = "Erro ao processar os dados do clima."
                }
            },
            { erro ->
                tvResult.text = "Falha na conexão. Cidade não encontrada ou sem internet."
            })

        queue.add(request)
    }
}