package esan.mendoza.pc2.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import esan.mendoza.pc2.data.remote.ConversionModel
import esan.mendoza.pc2.data.remote.FirebaseAuthManager
import kotlinx.coroutines.launch

data class Moneda(val codigo: String, val nombre: String)
data class TasaCambio(val from: String, val to: String, val tasa: Double)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionScreen() {
    var monto by remember { mutableStateOf("") }
    var monedaOrigenExpanded by remember { mutableStateOf(false) }
    var monedaDestinoExpanded by remember { mutableStateOf(false) }
    var monedaOrigen by remember { mutableStateOf(monedas[0]) }
    var monedaDestino by remember { mutableStateOf(monedas[1]) }
    var resultado by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSnackbar by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Convertidor de Monedas",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = monto,
                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) monto = it },
                label = { Text("Monto") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            ExposedDropdownMenuBox(
                expanded = monedaOrigenExpanded,
                onExpandedChange = { if (!isLoading) monedaOrigenExpanded = !monedaOrigenExpanded }
            ) {
                OutlinedTextField(
                    value = "${monedaOrigen.codigo} - ${monedaOrigen.nombre}",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Moneda origen") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    enabled = !isLoading
                )
                DropdownMenu(
                    expanded = monedaOrigenExpanded,
                    onDismissRequest = { monedaOrigenExpanded = false }
                ) {
                    monedas.forEach { moneda ->
                        DropdownMenuItem(
                            text = { Text("${moneda.codigo} - ${moneda.nombre}") },
                            onClick = {
                                monedaOrigen = moneda
                                monedaOrigenExpanded = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = monedaDestinoExpanded,
                onExpandedChange = { if (!isLoading) monedaDestinoExpanded = !monedaDestinoExpanded }
            ) {
                OutlinedTextField(
                    value = "${monedaDestino.codigo} - ${monedaDestino.nombre}",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Moneda destino") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    enabled = !isLoading
                )
                DropdownMenu(
                    expanded = monedaDestinoExpanded,
                    onDismissRequest = { monedaDestinoExpanded = false }
                ) {
                    monedas.forEach { moneda ->
                        DropdownMenuItem(
                            text = { Text("${moneda.codigo} - ${moneda.nombre}") },
                            onClick = {
                                monedaDestino = moneda
                                monedaDestinoExpanded = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val montoDouble = monto.toDoubleOrNull()
                    if (montoDouble != null) {
                        scope.launch {
                            isLoading = true
                            errorMessage = null

                            val tasa = obtenerTasaCambio(monedaOrigen.codigo, monedaDestino.codigo)
                            val montoConvertido = montoDouble * tasa

                            resultado = "%.2f %s equivalen a %.2f %s".format(
                                montoDouble,
                                monedaOrigen.codigo,
                                montoConvertido,
                                monedaDestino.codigo
                            )

                            val conversion = ConversionModel(
                                userId = FirebaseAuthManager.getCurrentUser()?.uid ?: "",
                                timestamp = System.currentTimeMillis(),
                                amount = montoDouble,
                                sourceCurrency = monedaOrigen.codigo,
                                targetCurrency = monedaDestino.codigo,
                                result = montoConvertido
                            )

                            FirebaseAuthManager.saveConversion(conversion).fold(
                                onSuccess = {
                                    snackbarHostState.showSnackbar("Conversión guardada exitosamente")
                                },
                                onFailure = { e ->
                                    errorMessage = "Error al guardar: ${e.message}"
                                }
                            )
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = monto.isNotEmpty() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Convertir")
                }
            }

            resultado?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private val monedas = listOf(
    Moneda("USD", "Dólar estadounidense"),
    Moneda("EUR", "Euro"),
    Moneda("PEN", "Sol peruano"),
    Moneda("GBP", "Libra esterlina"),
    Moneda("JPY", "Yen japonés")
)

private val tasasCambio = listOf(
    TasaCambio("USD", "EUR", 0.925),
    TasaCambio("USD", "PEN", 3.70),
    TasaCambio("USD", "GBP", 0.79),
    TasaCambio("USD", "JPY", 151.50),
    // Añade más tasas según necesites
)

private fun obtenerTasaCambio(from: String, to: String): Double {
    if (from == to) return 1.0
    return tasasCambio.find { it.from == from && it.to == to }?.tasa
        ?: tasasCambio.find { it.from == to && it.to == from }?.let { 1 / it.tasa }
        ?: 1.0
}