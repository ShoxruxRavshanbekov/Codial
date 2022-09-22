package uz.codial6.codial.main.fragments.courses

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import uz.codial6.codial.R
import uz.codial6.codial.databinding.*
import uz.codial6.codial.login.activities.SignUpActivity
import uz.codial6.codial.main.adapters.CourseSlideAdapter
import uz.codial6.codial.main.interfaces.RvItemClickListener
import uz.codial6.codial.models.CourseData
import uz.codial6.codial.models.DataOfTheUserWhoWantsToJoinNewGroups
import uz.codial6.codial.models.Links
import uz.codial6.codial.models.UserData
import uz.codial6.codial.utils.MySmsManager
import kotlin.math.abs

@SuppressLint("SetTextI18n")
class ListOfCoursesFragment : Fragment(), RvItemClickListener {

    lateinit var binding: FragmentListOfCoursesBinding
    lateinit var navHeaderBinding: NavHeaderBinding
    lateinit var context: FragmentActivity
    lateinit var auth: FirebaseAuth
    lateinit var realtimeDatabase: FirebaseDatabase
    lateinit var userDataReference: DatabaseReference
    lateinit var courseDataReference: DatabaseReference
    lateinit var addUseToNewGroupReference: DatabaseReference        //path lar bilan ishlash uchun
    lateinit var linkReferens: DatabaseReference
    lateinit var list: ArrayList<CourseData>
    lateinit var users: ArrayList<UserData>
    lateinit var links: Links

    private val requestPermissionLauncherForSendSMS =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                sendSms(course_name_for_granted_perission)
            }
        }

    companion object {
        var user_name = ""
        var user_surname = ""
        var user_image = ""
        var course_name_for_granted_perission = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentListOfCoursesBinding.inflate(layoutInflater)
        navHeaderBinding = NavHeaderBinding.bind(binding.navView.getHeaderView(0))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context = requireActivity()
        binding.progressBar.isVisible = true
        binding.viewPager.isGone = true
        firebase()
        getLink()
        navigationActions()
        getCourseData()
        achievements()

        val callBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                context.finish()
            }
        }
        context.onBackPressedDispatcher.addCallback(callBack)
    }

    private fun firebase() {
        auth = Firebase.auth
        realtimeDatabase = FirebaseDatabase.getInstance()
        userDataReference = realtimeDatabase.getReference("users")
        courseDataReference = realtimeDatabase.getReference("courses")
        addUseToNewGroupReference =
            realtimeDatabase.getReference("users who want to join new groups")
        linkReferens = realtimeDatabase.getReference("links")
    }

    private fun navigationActions() {
        binding.menu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.navView.setNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.rating -> {
                    findNavController().navigate(R.id.action_listOfCoursesFragment_to_ratingFragment)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.written -> {
                    gotoUrl(links.instruction!!)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.settings -> {
                    findNavController().navigate(R.id.action_listOfCoursesFragment_to_settingsFragment)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.about -> {
                    openBottomSheetDialog()
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.logout -> {
                    callDialog()
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }
            }

            true
        }

        navHeaderBinding.userImage.setOnClickListener {
            findNavController().navigate(R.id.action_listOfCoursesFragment_to_settingsFragment)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        getUserData()
    }

    private fun achievements() {
        binding.telegram.setOnClickListener {
            gotoUrl(links.telegram2!!)
        }

        binding.instagram.setOnClickListener {
            gotoUrl(links.instagram2!!)
        }

        binding.youtube.setOnClickListener {
            gotoUrl(links.youtube2!!)
        }
    }

    private fun callDialog() {
        val customDialog = AlertDialog.Builder(context).create()
        val bindingDialog = LogoutDialogBinding.inflate(layoutInflater)
        customDialog.setView(bindingDialog.root)
        customDialog.setCancelable(false)
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        bindingDialog.no.setOnClickListener {
            customDialog.dismiss()
        }

        bindingDialog.yes.setOnClickListener {
            startActivity(Intent(context, SignUpActivity::class.java))
            auth.signOut()
            customDialog.dismiss()
            context.finish()
        }

        customDialog.show()
    }

    private fun openBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
        val bindingDialog = AboutDialogItemBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bindingDialog.root)

        bindingDialog.phone.setOnClickListener {
            callForInfo()
        }

        bindingDialog.telegram.setOnClickListener {
            gotoUrl(links.telegram1!!)
            bottomSheetDialog.dismiss()
        }

        bindingDialog.instagram.setOnClickListener {
            gotoUrl(links.instagram1!!)
            bottomSheetDialog.dismiss()
        }

        bindingDialog.youtube.setOnClickListener {
            gotoUrl(links.youtube1!!)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun callForInfo() {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel: ${links.number!!}"))
        startActivity(intent)
    }

    private fun gotoUrl(url: String) {
        val uri = Uri.parse(url)
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    private fun getLink() {
        linkReferens.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                links = snapshot.getValue(Links::class.java) as Links
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getUserData() {
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
                            if (it.imageUrl == "null") {
                                navHeaderBinding.userImage.setImageResource(R.drawable.default_user)
                            } else {
                                Glide.with(context).load(it.imageUrl)
                                    .into(navHeaderBinding.userImage)
                            }
                            navHeaderBinding.userName.text = "${it.name} ${it.surname}"
                            navHeaderBinding.userNumber.text = auth.currentUser!!.phoneNumber

                            if (it.name != null && it.surname != null) {
                                user_name = it.name!!
                                user_surname = it.surname!!
                                user_image = it.imageUrl!!
                            }
                        }
                    } catch (e: Exception) {

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context,
                    "Server bilan bog`liq hatolik yuz berdi",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getCourseData() {
        list = ArrayList()
        try {
            courseDataReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        val courseData = child.getValue(CourseData::class.java) as CourseData
                        list.add(courseData)
                    }
                    binding.progressBar.isGone = true
                    binding.viewPager.isVisible = true
                    viewPager(list)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        } catch (e: Exception) {
            Toast.makeText(context, getString(R.string.kurslar_mavjud_emas), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun viewPager(list: ArrayList<CourseData>) {
        binding.viewPager.clipToPadding = false
        binding.viewPager.clipChildren = false
        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        binding.viewPager.adapter =
            CourseSlideAdapter(list, context, this@ListOfCoursesFragment)
        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(20))
        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.8f + r * 0.2f
        }

        binding.viewPager.setPageTransformer(compositePageTransformer)
    }

    override fun viewPagerItemClickListener(data: CourseData) {
        joinToGroup(data.name!!, data.uid!!)
    }

    private fun joinToGroup(course_name: String, course_id: String) {
        val customDialog = AlertDialog.Builder(context).create()
        val bindingDialog = JoiningGroupDialogBinding.inflate(layoutInflater)
        customDialog.setView(bindingDialog.root)
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        bindingDialog.joiningAnExistGroup.setOnClickListener {
            findNavController().navigate(R.id.action_listOfCoursesFragment_to_joiningAnExistingGroupFragment,
                bundleOf(
                    "course_name_to_join" to course_name,
                    "course_id" to course_id
                )
            )
            customDialog.dismiss()
        }

        bindingDialog.joiningANewGroup.setOnClickListener {
            course_name_for_granted_perission = course_name
            addUserToNewGroup(course_name)
            requestPermissionForSendSMS(course_name)
            weWillCallYou()
            customDialog.dismiss()
        }

        customDialog.show()
    }

    private fun addUserToNewGroup(course_name: String) {
        val user = DataOfTheUserWhoWantsToJoinNewGroups(
            addUseToNewGroupReference.push().key!!,
            user_name,
            user_surname,
            course_name
        )
        addUseToNewGroupReference.child("${auth.currentUser!!.phoneNumber}").setValue(user)
    }

    private fun weWillCallYou() {
        val customDialog = AlertDialog.Builder(context).create()
        val bindingDialog = WeWillCallYouDialogBinding.inflate(layoutInflater)
        customDialog.setView(bindingDialog.root)
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        bindingDialog.userName.text = "$user_name $user_surname"

        customDialog.show()
    }

    private fun requestPermissionForSendSMS(course_name: String) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) -> {
                sendSms(course_name)
            }

            else -> {
                requestPermissionLauncherForSendSMS.launch(Manifest.permission.SEND_SMS)
            }
        }
    }

    private fun sendSms(course_name: String) {
        MySmsManager(context).sentSmsMessage(
            "${links.number}",
            "$user_name $user_surname",
            "${auth.currentUser!!.phoneNumber}",
            course_name
        )
    }
}