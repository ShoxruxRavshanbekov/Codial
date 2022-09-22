package uz.codial6.codial.login.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil
import uz.codial6.codial.databinding.ActivityEnterCodeBinding
import uz.codial6.codial.databinding.CheckNumberDialogForEnterCodeBinding
import uz.codial6.codial.main.activities.MainActivity
import uz.codial6.codial.utils.LocaleManager

class EnterCodeActivity : AppCompatActivity() {

    lateinit var binding: ActivityEnterCodeBinding
    lateinit var verificationId: String
    lateinit var phoneNumber: String
    lateinit var auth: FirebaseAuth
    lateinit var realtimeDatabase: FirebaseDatabase
    lateinit var reference: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnterCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        realtimeDatabase = FirebaseDatabase.getInstance()
        reference = realtimeDatabase.getReference("users")
        verificationId = intent.getStringExtra("verificationId")!!
        phoneNumber = intent.getStringExtra("phoneNumber")!!

        binding.done.setOnClickListener {
            UIUtil.hideKeyboard(this)
            binding.progressBar.isVisible = true
            binding.done.isGone = true
            if (binding.otpView.otp.toString()
                    .trim().length == 6 || verificationId == binding.otpView.otp
            ) {
                val credential = PhoneAuthProvider
                    .getCredential(verificationId, binding.otpView.otp!!)
                signInWithPhoneAuthCredential(credential)
            }
        }

        binding.back.setOnClickListener {
            callDialog()
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Firebase.auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                binding.progressBar.isGone = true
                binding.otpView.showSuccess()
                checkUserDatabase()
            } else {
                binding.progressBar.isGone = true
                binding.done.isVisible = true
                binding.otpView.showError()
            }
        }
    }

    private fun checkUserDatabase() {
        reference.child(auth.currentUser!!.phoneNumber!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null) {
                        startActivity(Intent(this@EnterCodeActivity, UserDataActivity::class.java))
                        finish()
                    } else {
                        startActivity(Intent(this@EnterCodeActivity, MainActivity::class.java))
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EnterCodeActivity, error.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun callDialog() {
        val customDialog = AlertDialog.Builder(this).create()
        val bindingDialog = CheckNumberDialogForEnterCodeBinding.inflate(layoutInflater)
        customDialog.setView(bindingDialog.root)
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        bindingDialog.number.text = phoneNumber

        bindingDialog.edit.setOnClickListener {
            customDialog.dismiss()
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        bindingDialog.dismiss.setOnClickListener {
            customDialog.dismiss()
        }

        customDialog.show()
    }

    override fun onBackPressed() {
        val customDialog = AlertDialog.Builder(this).create()
        val bindingDialog = CheckNumberDialogForEnterCodeBinding.inflate(layoutInflater)
        customDialog.setView(bindingDialog.root)
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        bindingDialog.number.text = phoneNumber

        bindingDialog.edit.setOnClickListener {
            customDialog.dismiss()
            startActivity(Intent(this, SignUpActivity::class.java))
            super.onBackPressed()
        }

        bindingDialog.dismiss.setOnClickListener {
            customDialog.dismiss()
        }

        customDialog.show()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase!!))
    }

}