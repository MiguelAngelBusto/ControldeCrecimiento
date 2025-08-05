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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items




class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val montoGuardado = prefs.getFloat("monto_inicial", 0f)

        setContent {
            ControlDeCrecimientoTheme {
                var montoActual by remember { mutableStateOf(montoGuardado) }
                // NUEVO ESTADO: controla qué pantalla se muestra
                var mostrarHistorico by remember { mutableStateOf(false) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (mostrarHistorico) {
                        // 1. Leemos el historial como una cadena de texto
                        val historialGuardadoString = prefs.getString("historial_ganancias", "") ?: ""

                        // 2. Lo dividimos en una lista, manejando el caso de que esté vacío
                        val listaHistorial = if (historialGuardadoString.isEmpty()) {
                            emptyList()
                        } else {
                            historialGuardadoString.split(";")
                        }

                        HistoricoScreen(
                            historialGanancias = listaHistorial, // Pasamos la lista ordenada
                            onVolver = { mostrarHistorico = false },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }  else if (montoActual == 0f) {
                        MontoInicialScreen(
                            onMontoGuardado = { monto ->
                                prefs.edit().putFloat("monto_inicial", monto).apply()
                                montoActual = monto
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        // Leemos el historial como una cadena de texto
                        val historialGuardadoString = prefs.getString("historial_ganancias", "") ?: ""
                        val listaHistorial = if (historialGuardadoString.isEmpty()) {
                            emptyList()
                        } else {
                            historialGuardadoString.split(";")
                        }
                        // Calculamos el tamaño de la lista
                        val cantidadDeGanancias = listaHistorial.size

                        SeguimientoDiarioScreen(
                            montoActual = montoActual,
                            cantidadGanancias = cantidadDeGanancias, // <-- NUEVO: Pasamos la cantidad
                            onMontoActualizado = { nuevoMonto ->
                                val gananciaDiaria = nuevoMonto - montoActual

                                val historialActualizado = if (historialGuardadoString.isEmpty()) {
                                    "Ganancia: $${"%.2f".format(gananciaDiaria)}"
                                } else {
                                    "$historialGuardadoString;Ganancia: $${"%.2f".format(gananciaDiaria)}"
                                }

                                prefs.edit().putString("historial_ganancias", historialActualizado).apply()
                                prefs.edit().putFloat("monto_inicial", nuevoMonto).apply()
                                montoActual = nuevoMonto
                            },
                            onReiniciar = {
                                prefs.edit().remove("monto_inicial").apply()
                                prefs.edit().remove("historial_ganancias").apply()
                                montoActual = 0f
                            },
                            onMostrarHistorico = { mostrarHistorico = true },
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
    cantidadGanancias: Int,
    onMontoActualizado: (Float) -> Unit,
    onReiniciar: () -> Unit,
    onMostrarHistorico: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estado para controlar si se muestra el diálogo de reinicio
    var mostrarDialogoDeReinicio by remember { mutableStateOf(false) }

    // Calcula el 1% de ganancia diaria
    val gananciaDiaria = montoActual * 0.01f

    // Usamos un Box como contenedor principal para poder superponer elementos
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Cantidad de veces: $cantidadGanancias",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.TopStart) // <-- Posicionamiento arriba a la izquierda
        )
        // Contenido principal de la pantalla, centrado
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center), // Centramos este Column en el Box
            horizontalAlignment = Alignment.CenterHorizontally
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

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val nuevoMonto = montoActual * 1.01f
                    onMontoActualizado(nuevoMonto)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aplicar ganancia de hoy")
            }
        }

        // Fila de botones en la parte inferior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter), // Alineamos esta fila en la parte inferior del Box
            horizontalArrangement = Arrangement.SpaceAround // Distribuye los botones con espacio alrededor
        ) {
            Button(
                onClick = { mostrarDialogoDeReinicio = true },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text("Reiniciar")
            }

            Button(
                onClick = onMostrarHistorico,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text("Histórico")
            }
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
@Composable
fun HistoricoScreen(
    historialGanancias: List<String>,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Historial de Ganancias",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // LazyColumn es un Composable que maneja listas eficientemente
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(historialGanancias) { ganancia -> // <--- YA NO NECESITAMOS .toList()
                Text(
                    text = ganancia,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}