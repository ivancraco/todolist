package com.ivandev.todolist.core.adapter.state

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ivandev.todolist.ui.introduction.IntroductionFragment

class FragmentAdapter(
    fm: FragmentManager,
    lc: Lifecycle
) : FragmentStateAdapter(fm, lc) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return IntroductionFragment.newInstance(position + 1)
    }
}