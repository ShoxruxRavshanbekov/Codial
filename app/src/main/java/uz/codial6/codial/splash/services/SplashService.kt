package uz.codial6.codial.splash.services

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class SplashService {
    val auth = Firebase.auth
    private val realTimeDatabase = FirebaseDatabase.getInstance()
    private val reference = realTimeDatabase.getReference("users")

    fun checkUser(
        context: Context,
        splashActivity: Activity,
        signInActivity: Activity,
        userDataActivity: Activity,
        mainActivity: Activity,
    ) {
        if (auth.currentUser == null) {
            splashActivity.overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out)
            splashActivity.startActivity(Intent(context, signInActivity::class.java))
            splashActivity.finish()
        } else {
            checkUserDatabase(splashActivity, userDataActivity, mainActivity)
        }
    }

    private fun checkUserDatabase(
        splashActivity: Activity,
        userDataActivity: Activity,
        mainActivity: Activity,
    ) {
        reference.child(auth.currentUser!!.phoneNumber!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null) {
                        splashActivity.overridePendingTransition(android.R.anim.fade_in,
                            android.R.anim.fade_out)
                        splashActivity.startActivity(Intent(splashActivity,
                            userDataActivity::class.java))
                        splashActivity.finish()
                    } else {
                        splashActivity.overridePendingTransition(android.R.anim.fade_in,
                            android.R.anim.fade_out)
                        splashActivity.startActivity(Intent(splashActivity,
                            mainActivity::class.java))
                        splashActivity.finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(splashActivity, error.toString(), Toast.LENGTH_SHORT).show()
                }
            })
    }
}