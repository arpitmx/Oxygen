package com.ncs.o2.Domain.Delegates

import android.content.Context
import android.widget.Toast
import androidx.core.content.contentValuesOf
import com.google.api.Endpoint
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.versa.Constants.Endpoints
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import kotlin.reflect.KProperty

class firestoreDelegate(val userId: String, val context: Context) {


    private var utils: GlobalUtils.EasyElements = GlobalUtils.EasyElements(context)
    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

     operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return try {
            val document = runBlocking{
                    firestore.collection(Endpoints.USERS).document(userId)
                        .get().await()
                }
            val role = Integer.parseInt(document.get(Endpoints.User.ROLE).toString()) ?: 0
            role

        } catch (e: Exception) {
            utils.singleBtnDialog_ServerError(msg = "Firestore read error: ${e.message}") {
                Timber.d("Firestore read error: ${e.message}")
            }
            0
        }
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        try {
            val document = firestore.collection(Endpoints.USERS).document(userId)

            document.update(Endpoints.User.ROLE, value)
                .addOnSuccessListener {
                    Toast.makeText(context, "Updated field to :${value}", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener{e->
                    utils.singleBtnDialog_ServerError(msg = "Firestore read error: ${e.message}") {
                    }
                }


        } catch (e: Exception) {
            utils.singleBtnDialog_ServerError(msg = "Firestore read error: ${e.message}") {
                Timber.d("Firestore read error: ${e.message}")
            }
        }
    }


}
