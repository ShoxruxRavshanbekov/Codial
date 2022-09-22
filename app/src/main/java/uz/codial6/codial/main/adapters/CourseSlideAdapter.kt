package uz.codial6.codial.main.adapters

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import uz.codial6.codial.R
import uz.codial6.codial.databinding.ViewPagerItemBinding
import uz.codial6.codial.main.interfaces.RvItemClickListener
import uz.codial6.codial.models.CourseData

class CourseSlideAdapter(
    val list: List<CourseData>,
    val context: Context,
    val itemClickListener: RvItemClickListener,
) :
    RecyclerView.Adapter<CourseSlideAdapter.VH>() {

    inner class VH(var binding: ViewPagerItemBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ViewPagerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.binding.courseName.ellipsize = TextUtils.TruncateAt.MARQUEE
        holder.binding.courseName.isSelected = true
        holder.binding.courseName.text = item.name
        holder.binding.courseInfo.text = item.about
        Glide.with(context).load(item.imageLink).into(holder.binding.courseImage)


        holder.binding.joinToCourse.setOnClickListener {
            itemClickListener.viewPagerItemClickListener(item)
        }
    }

    override fun getItemCount(): Int = list.size
}
