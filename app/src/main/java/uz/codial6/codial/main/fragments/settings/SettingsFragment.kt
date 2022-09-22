package uz.codial6.codial.main.fragments.settings

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.orhanobut.hawk.Hawk
import uz.codial6.codial.R
import uz.codial6.codial.databinding.ChangeLanguageDialogItemBinding
import uz.codial6.codial.databinding.FragmentSettingsBinding
import uz.codial6.codial.main.activities.MainActivity
import uz.codial6.codial.models.UserData

class SettingsFragment : Fragment() {

    lateinit var binding: FragmentSettingsBinding
    lateinit var context: FragmentActivity
    lateinit var auth: FirebaseAuth
    lateinit var realtimeDatabase: FirebaseDatabase
    lateinit var userDataReference: DatabaseReference
    lateinit var users: ArrayList<UserData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context = requireActivity()
        firebase()
        showUserData()

        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.editUserInfo.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_editUserSettingsFragment)
        }

        binding.changeLanguage.setOnClickListener {
            changeLanguage()
        }

        val callBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        context.onBackPressedDispatcher.addCallback(callBack)
    }

    private fun firebase() {
        auth = Firebase.auth
        realtimeDatabase = FirebaseDatabase.getInstance()
        userDataReference = realtimeDatabase.getReference("users")
    }

    private fun showUserData() {
        users = ArrayList()
        userDataReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val userData = child.getValue(UserData::class.java)
                    if (userData != null) {
                        users.add(userData)
                    }
                }

                users.forEach {
                    try {
                        if (it.phoneNumber == auth.currentUser!!.phoneNumber) {
                            if (it.imageUrl != "null") {
                                Glide.with(context).load(it.imageUrl).into(binding.image)
                            }
                            binding.userName.ellipsize = TextUtils.TruncateAt.MARQUEE
                            binding.userName.isSelected = true
                            binding.userName.text = "${it.name} ${it.surname}"
                        }
                    }catch (e:Exception){

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context,
                    getString(R.string.server_bilan_bogliq_hatolik_yuz_berdi),
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun changeLanguage() {
        val bottomSheetDialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
        val bindingDialog = ChangeLanguageDialogItemBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bindingDialog.root)

        bindingDialog.uzbek.setOnClickListener {
            Hawk.put("pref_lang", "uz")
            context.finish()
            startActivity(Intent(context, MainActivity::class.java))
        }

        bindingDialog.russian.setOnClickListener {
            Hawk.put("pref_lang", "ru")
            context.finish()
            startActivity(Intent(context, MainActivity::class.java))
        }

        bindingDialog.english.setOnClickListener {
            Hawk.put("pref_lang", "en")
            context.finish()
            startActivity(Intent(context, MainActivity::class.java))
        }

        bottomSheetDialog.show()
    }
}