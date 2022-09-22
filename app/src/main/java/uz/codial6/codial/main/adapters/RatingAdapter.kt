package uz.codial6.codial.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import uz.codial6.codial.R
import uz.codial6.codial.databinding.ExpandableGroupItemBinding
import uz.codial6.codial.models.CourseData
import uz.codial6.codial.models.RatingData

class RatingAdapter(
    private val coursesList: List<CourseData>,
    private val usersList: List<RatingData>,
) : RecyclerView.Adapter<RatingAdapter.VH>() {

    inner class VH(var binding: ExpandableGroupItemBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ExpandableGroupItemBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = coursesList[position]
        val users = ArrayList<RatingData>()
        var isExpandable = false

        usersList.forEach {
            if (it.course_name == item.name) {
                users.add(it)
            }
        }

        with(holder.binding) {
            courseName.text = item.name

            root.setOnClickListener {
                isExpandable = !isExpandable
                if (isExpandable) {
                    arrowRight.setImageResource(R.drawable.ic_arrow_down)
                    expandableLayout.isVisible = true
                    childRv.adapter = ChildRvAdapter(users)
                } else {
                    arrowRight.setImageResource(R.drawable.ic_arrow_right)
                    expandableLayout.isGone = true
                }
            }
        }
    }

    override fun getItemCount(): Int = coursesList.size
}