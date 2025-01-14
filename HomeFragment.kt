package com.threedev.appconvertor.ui.fragments.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.*
import com.threedev.appconvertor.R
import com.threedev.appconvertor.adapters.AppAdapter
import com.threedev.appconvertor.data.models.WebBuilderApkInfo
import com.threedev.appconvertor.databinding.FragmentHomeBinding
import com.threedev.appconvertor.data.models.MainViewModel
import com.threedev.appconvertor.ui.utils.Constants

class HomeFragment : Fragment(), AppAdapter.AppClickListener {

    private lateinit var binding:FragmentHomeBinding
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var appAdapter: AppAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appAdapter = AppAdapter(this)
        binding.rvAppStatus.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = appAdapter
        }
        observeAppData()

        binding.btnConvertapp.setOnClickListener {
            findNavController().navigate(R.id.action_Home_to_Convert)
        }

        binding.rdoGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.tab_pending -> {
                    filterAppData(Constants.STATUS_PENDING)
                }

                R.id.tab_processing -> {
                    filterAppData(Constants.STATUS_PROCESSING_APK)
                }

                R.id.tab_completed -> {
                    filterAppData(Constants.STATUS_COMPLETE_APK)
                }

                R.id.tab_prcsng_bundle -> {
                    filterAppData(Constants.STATUS_PROCESSING_BUNDLE)
                }

                R.id.completed_bundle -> {
                    filterAppData(Constants.STATUS_COMPLETE_BUNDLE)
                }

                R.id.deploy_playstore -> {
                    filterAppData(Constants.STATUS_DEPLOY_TO_PLAY_STORE)
                }
            }
        }

        // Handle button clicks for filtering
        /*binding.tabPending.setOnClickListener { filterAppData(0) }
        binding.tabProcessing.setOnClickListener { filterAppData(1) }
        binding.tabCompleted.setOnClickListener { filterAppData(2) }
        binding.tabPrcsngBundle.setOnClickListener { filterAppData(3) }
        binding.completedBundle.setOnClickListener { filterAppData(4) }
        binding.deployPlaystore.setOnClickListener { filterAppData(5) }*/

    }

    private fun observeAppData() {
        viewModel.getAllData().observe(requireActivity()) { resource ->
            when (resource.code) {
                500 -> {
                    //loading show loading
                }
                200 -> {
                    //success hide loader
                    filterAppData(Constants.STATUS_PENDING)

                }
                else -> {
                    //error hide loader and show message
                }
            }
        }
        /*viewModel.appData.observe(viewLifecycleOwner) { appData ->
            allApps = appData
            filterAppData(0)
        }

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            Log.e("Firestore", error)
        })*/
    }

    private fun filterAppData(status: Int) {
        if (viewModel.getAllData().value?.data == null) return
        val filteredApps = viewModel.getAllData().value!!.data?.builderData?.filter { it.status == status }
        appAdapter.submitList(filteredApps)

        if (filteredApps != null) {
            if (filteredApps.isEmpty()) {
                binding.contentPlaceholder.visibility = View.VISIBLE
                binding.contentPlaceholder.text = when (status) {
                    0 -> getString(R.string.no_pending_apps)
                    1 -> getString(R.string.no_processing_apk)
                    2 -> getString(R.string.no_complete_apk)
                    3 -> getString(R.string.no_processing_bundle)
                    4 -> getString(R.string.no_complete_bundle)
                    5 -> getString(R.string.no_deploy_playstore)
                    else -> ""
                }
            } else {
                binding.contentPlaceholder.visibility = View.GONE
            }
        }
    }

    override fun onInstructionClick(app: WebBuilderApkInfo) {
        Toast.makeText(requireContext(), "Instruction clicked for ${app.appName}", Toast.LENGTH_SHORT).show()
        // Add your specific action here
    }

    override fun onGetStarted(app: WebBuilderApkInfo) {
        val action = HomeFragmentDirections.actionHomeToGetStarted(app)
        findNavController().navigate(action)
    }

}
