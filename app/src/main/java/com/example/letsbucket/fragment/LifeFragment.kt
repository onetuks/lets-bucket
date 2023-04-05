package com.example.letsbucket.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsbucket.R
import com.example.letsbucket.adaptor.LifeAdapter
import com.example.letsbucket.adaptor.ThisYearAdapter
import com.example.letsbucket.databinding.FragmentLifeBinding
import com.example.letsbucket.util.DataUtil

class LifeFragment : Fragment() {

    private lateinit var binding: FragmentLifeBinding
    private lateinit var lifeAdapter: LifeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLifeBinding.inflate(inflater, container, false)
        lifeAdapter = LifeAdapter(requireContext(), DataUtil.lifeList)
        binding.lifeList.adapter = lifeAdapter
        binding.lifeList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        return binding.root
    }
}