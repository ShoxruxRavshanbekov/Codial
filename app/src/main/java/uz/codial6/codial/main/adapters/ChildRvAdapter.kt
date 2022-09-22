package uz.codial6.codial.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uz.codial6.codial.databinding.ExpandableGroupChildItemBinding
import uz.codial6.codial.models.RatingData

class ChildRvAdapter(private val list: List<RatingData>) :
    RecyclerView.Adapter<ChildRvAdapter.VH>() {

    inner class VH(var binding: ExpandableGroupChildItemBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ExpandableGroupChildItemBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        with(holder.binding) {
            userPosition.text = "${position + 1}"
            userName.text = item.user_name
            ball.text = item.ball
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}