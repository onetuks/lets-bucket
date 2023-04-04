package com.example.letsbucket.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsbucket.ThisYearItem
import com.example.letsbucket.adaptor.ThisYearAdapter
import com.example.letsbucket.databinding.FragmentThisyearBinding
import com.example.letsbucket.db.ThisYearBucketDB
import com.example.letsbucket.util.DataUtil

class ThisYearFragment : Fragment() {

    private lateinit var binding: FragmentThisyearBinding
    private lateinit var thisYearAdapter: ThisYearAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentThisyearBinding.inflate(inflater, container, false)
        setupBinding()
        return binding.root
    }

    private fun setupBinding() {
        binding.fab.setOnClickListener(View.OnClickListener {

        })

        binding.thisYearBucketList.apply {
            thisYearAdapter = ThisYearAdapter(DataUtil.thisYearBucketList)
            adapter = thisYearAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }
}