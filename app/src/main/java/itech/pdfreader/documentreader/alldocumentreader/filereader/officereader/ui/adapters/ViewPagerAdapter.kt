package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.annotation.Keep
@Keep
class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    private val arrayList: ArrayList<Fragment> = ArrayList()

    fun addFragment(fragment: Fragment) {
        arrayList.add(fragment)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun createFragment(position: Int): Fragment {
        return arrayList[position]
    }
}