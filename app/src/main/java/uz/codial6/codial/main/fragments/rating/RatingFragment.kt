package uz.codial6.codial.main.fragments.rating

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.*
import uz.codial6.codial.R
import uz.codial6.codial.main.adapters.RatingAdapter
import uz.codial6.codial.databinding.FragmentRatingBinding
import uz.codial6.codial.models.CourseData
import uz.codial6.codial.models.RatingData
import uz.codial6.codial.models.UserData

class RatingFragment : Fragment() {

    lateinit var binding: FragmentRatingBinding
    lateinit var context: FragmentActivity
    lateinit var realtimeDatabase: FirebaseDatabase
    lateinit var usersReference: DatabaseReference
    lateinit var coursesReference: DatabaseReference
    lateinit var users: ArrayList<UserData>
    lateinit var courses: ArrayList<CourseData>
    lateinit var ratingList: ArrayList<RatingData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentRatingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context = requireActivity()
        firebase()
        getRatingData()
        getCourseData()

        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        val callBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        context.onBackPressedDispatcher.addCallback(callBack)
    }

    private fun firebase() {
        realtimeDatabase = FirebaseDatabase.getInstance()
        coursesReference = realtimeDatabase.getReference("courses")
        usersReference = realtimeDatabase.getReference("users")
    }

    private fun getCourseData() {
        courses = ArrayList()
        try {
            coursesReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        val courseData = child.getValue(CourseData::class.java)
                        if (courseData != null) {
                            courses.add(courseData)
                        }
                    }
                    binding.ratingRv.adapter = RatingAdapter(courses, ratingList)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        } catch (e: Exception) {
            Toast.makeText(context, getString(R.string.kurslar_mavjud_emas), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun getRatingData() {
        users = ArrayList()
        ratingList = ArrayList()
        usersReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val userData = child.getValue(UserData::class.java)
                    if (userData != null) {
                        users.add(userData)
                    }
                }

                setData(users, ratingList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context,
                    "Server bilan bog`liq hatolik yuz berdi",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setData(usersList: ArrayList<UserData>, ratingList: ArrayList<RatingData>) {

        usersList.forEach {
            if (it.listRating[0].courseId != "null" || it.listRating[0].ball != "null") {
                ratingList.add(
                    RatingData(
                        course_name = it.listRating[0].courseId!!,
                        user_name = "${it.name} ${it.surname}",
                        ball = it.listRating[0].ball.toString()
                    )
                )
            }
        }
    }
}