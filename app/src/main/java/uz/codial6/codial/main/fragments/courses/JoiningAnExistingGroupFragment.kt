package uz.codial6.codial.main.fragments.courses

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.*
import uz.codial6.codial.R
import uz.codial6.codial.databinding.FragmentJoiningAnExistingGroupBinding

class JoiningAnExistingGroupFragment : Fragment() {

    lateinit var binding: FragmentJoiningAnExistingGroupBinding
    lateinit var realtimeDatabase: FirebaseDatabase
    lateinit var testsReference: DatabaseReference
    lateinit var context: FragmentActivity
    lateinit var course_name_to_join: String
    lateinit var course_id: String
    var courseId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentJoiningAnExistingGroupBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context = requireActivity()
        init()
        firebase()
        getQuestionsKeys()

        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.startTest.setOnClickListener {
            findNavController().navigate(R.id.action_joiningAnExistingGroupFragment_to_testingFragment,
                bundleOf(
                    "course_name" to binding.actionBarTitle.text,
                    "course_id" to courseId
                )
            )
        }

        val callBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        context.onBackPressedDispatcher.addCallback(callBack)
    }

    private fun init() {
        course_name_to_join = arguments?.getString("course_name_to_join")!!
        course_id = arguments?.getString("course_id")!!
        binding.actionBarTitle.text = course_name_to_join
    }

    private fun firebase() {
        realtimeDatabase = FirebaseDatabase.getInstance()
        testsReference = realtimeDatabase.getReference("questions")
    }

    private fun getQuestionsKeys(): ArrayList<String> {
        val questions = arrayListOf<String>()
        testsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val question = child.key
                    questions.add(question!!)
                }

                questions.forEach {
                    if (it == course_id) {
                        courseId = it
                        binding.startTest.isEnabled = true
                        binding.testsNotFound.isGone = true
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        return questions
    }
}