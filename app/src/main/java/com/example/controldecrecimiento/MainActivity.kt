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
        val montoGuardado = prefs.getFloat("monto_inicial", 0f)

        setContent {
            ControlDeCrecimientoTheme {
                var montoActual by remember { mutableStateOf(montoGuardado) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (montoActual == 0f) {
                        MontoInicialScreen(
                            onMontoGuardado = { monto ->
                                prefs.edit().putFloat("monto_inicial", monto).apply()
                                montoActual = monto
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        // AQUÍ ESTÁ EL CAMBIO: agregamos el callback onReiniciar
                        SeguimientoDiarioScreen(
                            montoActual = montoActual,
                            onMontoActualizado = { nuevoMonto ->
                                prefs.edit().putFloat("monto_inicial", nuevoMonto).apply()
                                montoActual = nuevoMonto
                            },
                            onReiniciar = {
                                // Borramos el valor en SharedPreferences
                                prefs.edit().remove("monto_inicial").apply()
                                // Ponemos el monto en 0 para que Compose cambie la pantalla
                                montoActual = 0f
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

// Modificamos la firma para que acepte un callback para reiniciar
@Composable
fun SeguimientoDiarioScreen(
    montoActual: Float,
    onMontoActualizado: (Float) -> Unit,
    onReiniciar: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estado para controlar si se muestra el diálogo de reinicio
    var mostrarDialogoDeReinicio by remember { mutableStateOf(false) }

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
            text = "$${"%.2f".format(montoActual)}",
            style = MaterialTheme.typography.displayMedium
        )

        Text(
            text = "Ganancia de hoy: $${"%.2f".format(gananciaDiaria)}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val nuevoMonto = montoActual * 1.01f
                onMontoActualizado(nuevoMonto)
            }
        ) {
            Text("Aplicar ganancia de hoy")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { mostrarDialogoDeReinicio = true } // <-- Cambio: ahora solo muestra el diálogo
        ) {
            Text("Reiniciar")
        }
    }

    // Aquí está el nuevo AlertDialog
    if (mostrarDialogoDeReinicio) {
        AlertDialog(
            onDismissRequest = {
                // Se llama si el usuario hace clic fuera del diálogo
                mostrarDialogoDeReinicio = false
            },
            title = {
                Text(text = "Confirmar reinicio")
            },
            text = {
                Text("¿Está seguro de que desea realizar el reinicio? Todos los datos se perderán.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReiniciar() // Llama a la lógica de reinicio
                        mostrarDialogoDeReinicio = false // Oculta el diálogo
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoDeReinicio = false // Solo oculta el diálogo
                    }
                ) {
                    Text("No")
                }
            }
        )
    }
}
