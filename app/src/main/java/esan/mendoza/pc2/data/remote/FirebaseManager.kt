package esan.mendoza.pc2.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.text.get
import kotlin.text.set
data class RateModel(
    val code: String = "",
    val name: String = "",
    val rate: Double = 0.0
)
data class ConversionModel(
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val amount: Double = 0.0,
    val sourceCurrency: String = "",
    val targetCurrency: String = "",
    val result: Double = 0.0
)
object FirebaseAuthManager {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun loginUser(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Agregar método para verificar el estado de la sesión
    fun getCurrentUser() = auth.currentUser

    fun signOut() = auth.signOut()
    // Guardar tasa de cambio
    suspend fun saveRate(rate: RateModel): Result<Unit> {
        return try {
            firestore.collection("rates")
                .document(rate.code)
                .set(rate)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener todas las tasas
    suspend fun getRates(): Result<List<RateModel>> {
        return try {
            val snapshot = firestore.collection("rates").get().await()
            val rates = snapshot.toObjects(RateModel::class.java)
            Result.success(rates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Guardar conversión
    suspend fun saveConversion(conversion: ConversionModel): Result<Unit> {
        return try {
            firestore.collection("conversions")
                .add(conversion)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener conversiones del usuario actual
    suspend fun getUserConversions(): Result<List<ConversionModel>> {
        return try {
            val userId = getCurrentUser()?.uid ?: throw Exception("Usuario no autenticado")
            val snapshot = firestore.collection("conversions")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            val conversions = snapshot.toObjects(ConversionModel::class.java)
            Result.success(conversions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }






}
