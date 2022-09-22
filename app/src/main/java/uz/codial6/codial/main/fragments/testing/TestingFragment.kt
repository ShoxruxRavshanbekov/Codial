package uz.codial6.codial.main.fragments.testing

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import uz.codial6.codial.databinding.BackDialogBinding
import uz.codial6.codial.databinding.FragmentTestingBinding
import uz.codial6.codial.databinding.TestingEndDialogBinding
import uz.codial6.codial.main.fragments.courses.ListOfCoursesFragment
import uz.codial6.codial.models.*

class TestingFragment : Fragment() {

    lateinit var binding: FragmentTestingBinding
    lateinit var context: FragmentActivity
    lateinit var realtimeDatabase: FirebaseDatabase
    lateinit var testsReference: DatabaseReference
    lateinit var usersReference: DatabaseReference
    lateinit var questionsList: ArrayList<Questions>
    var correct = false
    var currentPosition = 0
    var course_id = ""

    companion object {
        var ball = 0
        var course = ""
        var count_of_correct_answers = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentTestingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        firebase()
        getTestData()
        testFunctionality()

        binding.back.setOnClickListener {
            callDialog()
        }

        val callBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                callDialog()
            }
        }
        context.onBackPressedDispatcher.addCallback(callBack)
    }

    private fun init() {
        context = requireActivity()
        course = arguments?.getString("course_name")!!
        course_id = arguments?.getString("course_id")!!
    }

    private fun firebase() {
        realtimeDatabase = FirebaseDatabase.getInstance()
        testsReference = realtimeDatabase.getReference("questions")
        usersReference = realtimeDatabase.getReference("users")
    }

    private fun callDialog() {
        val customDialog = AlertDialog.Builder(context).create()
        val bindingDialog = BackDialogBinding.inflate(layoutInflater)
        customDialog.setView(bindingDialog.root)
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        bindingDialog.no.setOnClickListener {
            customDialog.dismiss()
        }

        bindingDialog.yes.setOnClickListener {
            findNavController().popBackStack()
            count_of_correct_answers = 0
            customDialog.dismiss()
        }

        customDialog.show()
    }

    private fun getTestData() {
        questionsList = ArrayList()
        testsReference.child(course_id)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        val question = child.getValue(Questions::class.java)
                        if (question != null) {
                            questionsList.add(question)
                        }
                    }
                    binding.countOfSolvedTests.text = "${currentPosition + 1}"
                    binding.countOfTests.text = questionsList.size.toString()
                    binding.question.text = questionsList[currentPosition].question
                    binding.answerA.text =
                        questionsList[currentPosition].listAnswer[0].answer
                    binding.answerB.text =
                        questionsList[currentPosition].listAnswer[1].answer
                    binding.answerC.text =
                        questionsList[currentPosition].listAnswer[2].answer
                    binding.answerD.text =
                        questionsList[currentPosition].listAnswer[3].answer
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun testFunctionality() {
        binding.answerA.setOnClickListener {
            if (correct) {
                correct = false
            }

            if (questionsList[currentPosition].listAnswer[0].correct) {
                correct = true
            }
            binding.answerA.isChecked = true
            binding.answerB.isChecked = false
            binding.answerC.isChecked = false
            binding.answerD.isChecked = false
            binding.next.isEnabled = true
            binding.finish.isEnabled = true


        }

        binding.answerB.setOnClickListener {
            if (correct) {
                correct = false
            }

            if (questionsList[currentPosition].listAnswer[1].correct) {
                correct = true
            }
            binding.answerB.isChecked = true
            binding.answerA.isChecked = false
            binding.answerC.isChecked = false
            binding.answerD.isChecked = false
            binding.next.isEnabled = true
            binding.finish.isEnabled = true

        }

        binding.answerC.setOnClickListener {
            if (correct) {
                correct = false
            }

            if (questionsList[currentPosition].listAnswer[2].correct) {
                correct = true
            }
            binding.answerC.isChecked = true
            binding.answerA.isChecked = false
            binding.answerB.isChecked = false
            binding.answerD.isChecked = false
            binding.next.isEnabled = true
            binding.finish.isEnabled = true

        }

        binding.answerD.setOnClickListener {
            if (correct) {
                correct = false
            }

            if (questionsList[currentPosition].listAnswer[3].correct) {
                correct = true
            }
            binding.answerD.isChecked = true
            binding.answerA.isChecked = false
            binding.answerB.isChecked = false
            binding.answerC.isChecked = false
            binding.next.isEnabled = true
            binding.finish.isEnabled = true

        }

        binding.next.setOnClickListener {
            if (correct) {
                count_of_correct_answers++
            }

            binding.answerA.isChecked = false
            binding.answerB.isChecked = false
            binding.answerC.isChecked = false
            binding.answerD.isChecked = false
            binding.next.isEnabled = false

            if (currentPosition != questionsList.size - 1) {
                currentPosition++
                binding.countOfSolvedTests.text = "${currentPosition + 1}"
                binding.question.text = questionsList[currentPosition].question
                binding.answerA.text = questionsList[currentPosition].listAnswer[0].answer
                binding.answerB.text = questionsList[currentPosition].listAnswer[1].answer
                binding.answerC.text = questionsList[currentPosition].listAnswer[2].answer
                binding.answerD.text = questionsList[currentPosition].listAnswer[3].answer

                if (currentPosition == questionsList.size - 1) {
                    binding.finish.isEnabled = false
                    binding.finish.isVisible = true
                    binding.next.isGone = true
                }
            }

            if (questionsList.size == 1) {
                testingEnd()
            }
        }

        binding.finish.setOnClickListener {
            if (correct) {
                count_of_correct_answers++
            }
            testingEnd()
        }
    }

    private fun testingEnd() {
        val customDialog = AlertDialog.Builder(context).create()
        val bindingDialog = TestingEndDialogBinding.inflate(layoutInflater)
        customDialog.setView(bindingDialog.root)
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        bindingDialog.close.setOnClickListener {
            findNavController().popBackStack()
            customDialog.dismiss()
            writeData()
            count_of_correct_answers = 0
        }

        bindingDialog.countOfCorrectAnswers.text = count_of_correct_answers.toString()

        customDialog.show()
    }

    private fun writeData() {
        val user = UserData(
            usersReference.push().key!!,
            ListOfCoursesFragment.user_name,
            ListOfCoursesFragment.user_surname,
            ListOfCoursesFragment.user_image,
            "${Firebase.auth.currentUser!!.phoneNumber}",
            arrayListOf(
                Rating(
                    "${count_of_correct_answers * 5}",
                    course
                )
            )
        )
        usersReference.child("${Firebase.auth.currentUser!!.phoneNumber}").setValue(user)
    }
}