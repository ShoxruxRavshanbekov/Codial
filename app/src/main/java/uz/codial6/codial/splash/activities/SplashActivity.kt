package uz.codial6.codial.splash.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.*
import uz.codial6.codial.main.activities.MainActivity
import uz.codial6.codial.databinding.ActivitySplashBinding
import uz.codial6.codial.login.activities.SignUpActivity
import uz.codial6.codial.login.activities.UserDataActivity
import uz.codial6.codial.splash.services.SplashService

class SplashActivity : AppCompatActivity() {

    lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.splashImage.alpha = 0f
        binding.splashImage.animate().setDuration(1500).alpha(1f).withEndAction {
            SplashService().checkUser(
                this,
                this,
                SignUpActivity(),
                UserDataActivity(),
                MainActivity()
            )
        }
    }
}