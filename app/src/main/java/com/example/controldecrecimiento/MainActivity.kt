package com.example.controldecrecimiento

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.controldecrecimiento.ui.theme.ControlDeCrecimientoTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        // Creamos una variable de estado que va a guardar el monto actual de la app.
        // Su valor inicial es el que está guardado en SharedPreferences.
        val montoGuardado = prefs.getFloat("monto_inicial", 0f)

        setContent {
            ControlDeCrecimientoTheme {
                // El estado principal de la aplicación.
                var montoActual by remember { mutableStateOf(montoGuardado) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (montoActual == 0f) {
                        // Si el monto es 0, significa que es la primera vez.
                        MontoInicialScreen(
                            onMontoGuardado = { monto ->
                                // Guardamos el monto en SharedPreferences
                                prefs.edit().putFloat("monto_inicial", monto).apply()
                                // Y actualizamos nuestra variable de estado principal
                                montoActual = monto
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        // Si el monto es > 0, mostramos la pantalla de seguimiento.
                        SeguimientoDiarioScreen(
                            montoActual = montoActual,
                            onMontoActualizado = { nuevoMonto ->
                                // Guardamos el nuevo monto en SharedPreferences
                                prefs.edit().putFloat("monto_inicial", nuevoMonto).apply()
                                // Y actualizamos nuestra variable de estado, lo que refresca la UI
                                montoActual = nuevoMonto
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

// Modificamos la función para que reciba una función de callback
@Composable
fun MontoInicialScreen(onMontoGuardado: (Float) -> Unit, modifier: Modifier = Modifier) {
    var montoIngresado by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Ingresa tu Monto Inicial",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = montoIngresado,
            onValueChange = { nuevoValor -> montoIngresado = nuevoValor },
            label = { Text("Monto") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Verificamos si el valor es un número válido
                val monto = montoIngresado.toFloatOrNull()
                if (monto != null && monto > 0) {
                    onMontoGuardado(monto)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Comenzar")
        }
    }
}

@Composable
fun SeguimientoDiarioScreen(montoActual: Float, onMontoActualizado: (Float) -> Unit, modifier: Modifier = Modifier) {
    // No necesitamos crear un nuevo estado aquí.
    // El valor que usamos (montoActual) ya viene como parámetro desde MainActivity
    // y es una variable de estado allí.

    // Calcula el 1% de ganancia diaria
    val gananciaDiaria = montoActual * 0.01f

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Monto Actual:",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "$${"%.2f".format(montoActual)}", // Usamos el parámetro que recibimos
            style = MaterialTheme.typography.displayMedium
        )

        Text(
            text = "Ganancia de hoy: $${"%.2f".format(gananciaDiaria)}",
            style = MaterialTheme.typography.bodyLarge
        )

        Button(
            onClick = {
                val nuevoMonto = montoActual * 1.01f
                onMontoActualizado(nuevoMonto)
            }
        ) {
            Text("Aplicar ganancia de hoy")
        }
    }
}