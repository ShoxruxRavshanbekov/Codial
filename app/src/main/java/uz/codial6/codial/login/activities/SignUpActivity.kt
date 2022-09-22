package uz.codial6.codial.login.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil
import uz.codial6.codial.databinding.ActivitySignUpBinding
import uz.codial6.codial.databinding.CheckNumberDialogBinding
import uz.codial6.codial.utils.LocaleManager
import java.util.concurrent.TimeUnit


class SignUpActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignUpBinding
    lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        binding.done.isGone = true

        binding.METPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.done.isVisible = true
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.done.setOnClickListener {
            if (binding.METPhoneNumber.text!!.isNotEmpty()) {
                callDialog("+998${binding.METPhoneNumber.rawText}")
            } else if (binding.METPhoneNumber.rawText.toString().trim().length != 9) {
                Toast.makeText(this, "Raqam noto`g`ri kiritildi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        binding.progressBar.isVisible = true
        binding.done.isGone = true
        auth.setLanguageCode("uz")
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

            }

            override fun onVerificationFailed(e: FirebaseException) {
                binding.progressBar.isGone = true
                binding.done.isVisible = true
                Toast.makeText(this@SignUpActivity, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                binding.progressBar.isGone = true
                binding.done.isVisible = true
                val intent = Intent(this@SignUpActivity, EnterCodeActivity::class.java)
                intent.putExtra("verificationId", verificationId)
                intent.putExtra("phoneNumber", "+998${binding.METPhoneNumber.rawText}")
                startActivity(intent)
                finish()
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun callDialog(phoneNumber: String) {
        val customDialog = AlertDialog.Builder(this).create()
        val bindingDialog = CheckNumberDialogBinding.inflate(layoutInflater)
        customDialog.setView(bindingDialog.root)
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        bindingDialog.number.text = phoneNumber

        bindingDialog.edit.setOnClickListener {
            customDialog.dismiss()
        }

        bindingDialog.yes.setOnClickListener {
            customDialog.dismiss()
            UIUtil.hideKeyboard(this)
            sendVerificationCode("+998${binding.METPhoneNumber.rawText}")
        }

        customDialog.show()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase!!))
    }
}