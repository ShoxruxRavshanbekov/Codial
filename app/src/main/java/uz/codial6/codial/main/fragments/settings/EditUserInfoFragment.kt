package uz.codial6.codial.main.fragments.settings

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import uz.codial6.codial.BuildConfig
import uz.codial6.codial.R
import uz.codial6.codial.databinding.BottomSheetDialogItemBinding
import uz.codial6.codial.databinding.FragmentEditUserInfoBinding
import uz.codial6.codial.models.UserData
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.O)    // LocalDateTime.now()  uchun
class EditUserInfoFragment : Fragment() {

    lateinit var binding: FragmentEditUserInfoBinding
    lateinit var context: FragmentActivity
    lateinit var auth: FirebaseAuth
    lateinit var realtimeDatabase: FirebaseDatabase
    lateinit var userDataReference: DatabaseReference
    lateinit var storageRef: StorageReference
    lateinit var uploadUrl: String
    lateinit var photoUri: Uri
    lateinit var storageImage: String
    lateinit var imagePlaceholder: Any
    lateinit var users: ArrayList<UserData>
    var selectedImagePath = ""

    // for permission
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                photoUri = FileProvider.getUriForFile(
                    context,
                    BuildConfig.APPLICATION_ID,
                    createImageFile()
                )
                getTakeImageContent.launch(photoUri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentEditUserInfoBinding.inflate(layoutInflater)
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

        binding.done.setOnClickListener {
            if (binding.TIEName.text!!.isNotEmpty() || binding.TIESurname.text!!.isNotEmpty()) {
                writeData()
                Toast.makeText(context, getString(R.string.saqlandi), Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(context,
                    getString(R.string.berilgan_maydonlarni_toldiring),
                    Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.addImage.setOnClickListener {
            openBottomSheetDialog()
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
        storageRef = FirebaseStorage.getInstance().getReference("UsersImage")
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
                    if (it.phoneNumber == auth.currentUser!!.phoneNumber) {
                        if (it.imageUrl != "null") {
                            storageImage = it.imageUrl!!
                            imagePlaceholder =
                                Glide.with(context).load(it.imageUrl).into(binding.image)
                        } else {
                            storageImage = it.imageUrl!!
                        }
                        binding.TIEName.setText(it.name)
                        binding.TIESurname.setText(it.surname)
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

    private fun openBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
        val bindingDialog = BottomSheetDialogItemBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bindingDialog.root)

        bindingDialog.camera.setOnClickListener {
            addFromCamera()
            bottomSheetDialog.dismiss()
        }

        bindingDialog.gallery.setOnClickListener {
            addFromGallery()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }


    private fun addFromCamera() {
        requestPermission()
    }

    private fun addFromGallery() {
        result.launch("image/*")
    }

    // for Camera
    private val getTakeImageContent =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) {
                binding.image.setImageURI(photoUri)
                val inputStream = context.contentResolver?.openInputStream(photoUri)
                val file = File(context.filesDir, "${LocalDateTime.now()}.jpg")
                val fileOutputStream = FileOutputStream(file)
                inputStream?.copyTo(fileOutputStream)
                inputStream?.close()
                fileOutputStream.close()
                selectedImagePath = file.absolutePath
            }
        }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val format = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_$format", ".jpg", externalFilesDir).apply {}
    }
// / - / - / - / - /

    // for Gallery
    private val result = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it ?: return@registerForActivityResult
        binding.image.setImageURI(it)
        val inputStream = context.contentResolver?.openInputStream(it)
        val file = File(context.filesDir, "${LocalDateTime.now()}.jpg")
        val fileOutputStream = FileOutputStream(file)
        inputStream?.copyTo(fileOutputStream)
        inputStream?.close()
        fileOutputStream.close()
        selectedImagePath = file.absolutePath
    }
// / - / - / - / - /

    private fun writeData() {
        if (selectedImagePath != "") {
            val bitmap = binding.image.drawable.toBitmap()
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val storageRefChild =
                storageRef.child("${Firebase.auth.currentUser!!.phoneNumber}_user_image")
            val uploadTask = storageRefChild.putBytes(byteArray)

            val task = uploadTask.continueWithTask { task ->
                storageRefChild.downloadUrl
            }.addOnCompleteListener { task ->
                uploadUrl = task.result.toString()

                // RealtimeDatabase ga malumot yozish
                val user = UserData(
                    userDataReference.push().key!!,
                    binding.TIEName.text.toString().trim(),
                    binding.TIESurname.text.toString().trim(),
                    uploadUrl
                )
                userDataReference.child("${Firebase.auth.currentUser!!.phoneNumber}").setValue(user)
                // - / - / - / - /
            }
        } else {
            val user = UserData(
                userDataReference.push().key!!,
                binding.TIEName.text.toString().trim(),
                binding.TIESurname.text.toString().trim(),
                storageImage
            )
            userDataReference.child("${Firebase.auth.currentUser!!.phoneNumber}").setValue(user)
        }
    }

    // Permission
    private fun requestPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                photoUri = FileProvider.getUriForFile(
                    context,
                    BuildConfig.APPLICATION_ID,
                    createImageFile()
                )
                getTakeImageContent.launch(photoUri)
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}