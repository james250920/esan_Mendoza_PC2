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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun Long.formatDateTime(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("es", "PE"))
        return formatter.format(Date(this))
    }

    fun Double.format(decimales: Int) = "%.${decimales}f".format(this)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "Convertidor de Monedas",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = monto,
                        onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) monto = it },
                        label = { Text("Monto") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = monedaOrigenExpanded,
                            onExpandedChange = { if (!isLoading) monedaOrigenExpanded = !monedaOrigenExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = monedaOrigen.codigo,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("De") },
                                modifier = Modifier.menuAnchor(),
                                enabled = !isLoading,
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
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

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.CenterVertically),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        ExposedDropdownMenuBox(
                            expanded = monedaDestinoExpanded,
                            onExpandedChange = { if (!isLoading) monedaDestinoExpanded = !monedaDestinoExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = monedaDestino.codigo,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("A") },
                                modifier = Modifier.menuAnchor(),
                                enabled = !isLoading,
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
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
                    }

                    Button(
                        onClick = {
                            val montoDouble = monto.toDoubleOrNull()
                            if (montoDouble != null) {
                                scope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    val currentTimestamp = System.currentTimeMillis()
                                    val tasa = obtenerTasaCambio(monedaOrigen.codigo, monedaDestino.codigo)
                                    val montoConvertido = montoDouble * tasa

                                    resultado = """
                                        Fecha: ${currentTimestamp.formatDateTime()}
                                        ${montoDouble.format(2)} ${monedaOrigen.codigo} = ${montoConvertido.format(2)} ${monedaDestino.codigo}
                                    """.trimIndent()

                                    val conversion = ConversionModel(
                                        userId = FirebaseAuthManager.getCurrentUser()?.uid ?: "",
                                        timestamp = currentTimestamp,
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
                        enabled = monto.isNotEmpty() && !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
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
                }
            }

            resultado?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            errorMessage?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
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
    TasaCambio("USD", "JPY", 151.50)
)

private fun obtenerTasaCambio(from: String, to: String): Double {
    if (from == to) return 1.0
    return tasasCambio.find { it.from == from && it.to == to }?.tasa
        ?: tasasCambio.find { it.from == to && it.to == from }?.let { 1 / it.tasa }
        ?: 1.0
}