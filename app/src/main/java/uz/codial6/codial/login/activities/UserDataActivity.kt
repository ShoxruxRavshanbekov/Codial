package uz.codial6.codial.login.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil
import uz.codial6.codial.BuildConfig
import uz.codial6.codial.R
import uz.codial6.codial.databinding.ActivityUserDataBinding
import uz.codial6.codial.databinding.BottomSheetDialogItemBinding
import uz.codial6.codial.main.activities.MainActivity
import uz.codial6.codial.models.Rating
import uz.codial6.codial.models.UserData
import uz.codial6.codial.utils.LocaleManager
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class UserDataActivity : AppCompatActivity() {

    lateinit var binding: ActivityUserDataBinding
    lateinit var realtimeDatabase: FirebaseDatabase
    lateinit var reference: DatabaseReference        //path lar bilan ishlash uchun
    lateinit var storageRef: StorageReference
    lateinit var uploadUrl: String
    lateinit var photoUri: Uri
    var selectedImagePath = ""

    // for permission
    private val requestPermissionLauncher =
        registerForActivityResult(RequestPermission()) { isGranted ->
            if (isGranted) {
                photoUri = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID,
                    createImageFile()
                )
                getTakeImageContent.launch(photoUri)
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.progressBar.isGone = true
        realtimeDatabase = FirebaseDatabase.getInstance()
        reference = realtimeDatabase.getReference("users")
        storageRef = FirebaseStorage.getInstance().getReference("UsersImage")

        binding.done.setOnClickListener {
            UIUtil.hideKeyboard(this)
            binding.done.isGone = true
            binding.progressBar.isVisible = true
            if (binding.TIEName.text!!.isNotEmpty() || binding.TIESurname.text!!.isNotEmpty()) {
                writeData()
            } else {
                Toast.makeText(this, "Berilgan maydonlarni to`ldiring!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.addImage.setOnClickListener {
            openBottomSheetDialog()
        }
    }

    private fun openBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
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
                val inputStream = contentResolver?.openInputStream(photoUri)
                val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    File(filesDir, "${LocalDateTime.now()}.jpg")
                } else {
                    File(filesDir, "${Date().time}.jpg")
                }
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
        val externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_$format", ".jpg", externalFilesDir).apply {}
    }
// / - / - / - / - /

    // for Gallery
    private val result = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it ?: return@registerForActivityResult
        binding.image.setImageURI(it)
        val inputStream = contentResolver?.openInputStream(it)
        val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            File(filesDir, "${LocalDateTime.now()}.jpg")
        } else {
            File(filesDir, "${Date().time}.jpg")
        }
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
                    reference.push().key!!,
                    binding.TIEName.text.toString().trim(),
                    binding.TIESurname.text.toString().trim(),
                    uploadUrl,
                    "${Firebase.auth.currentUser!!.phoneNumber}",
                    arrayListOf(Rating("null", "null"))
                )
                reference.child("${Firebase.auth.currentUser!!.phoneNumber}").setValue(user)
                // - / - / - / - /
                binding.progressBar.isGone = true
                binding.done.isVisible = true
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        } else {
            val user = UserData(
                reference.push().key!!,
                binding.TIEName.text.toString().trim(),
                binding.TIESurname.text.toString().trim(),
                "null",
                "${Firebase.auth.currentUser!!.phoneNumber}",
                arrayListOf(Rating("null", "null"))
            )
            reference.child("${Firebase.auth.currentUser!!.phoneNumber}").setValue(user)

            binding.progressBar.isGone = true
            binding.done.isVisible = true
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    // Permission
    private fun requestPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) -> {
                photoUri = FileProvider.getUriForFile(
                    this,
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

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase!!))
    }
}