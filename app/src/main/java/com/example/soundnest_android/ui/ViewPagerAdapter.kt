package com.example.soundnest_android.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.soundnest_android.ui.home.HomeFragment
import com.example.soundnest_android.ui.playlists.PlaylistsFragment
import com.example.soundnest_android.ui.profile.ProfileFragment
import com.example.soundnest_android.ui.search.SearchFragment

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val fragments = listOf(
        HomeFragment(),
        SearchFragment(),
        PlaylistsFragment(),
        ProfileFragment()
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}
