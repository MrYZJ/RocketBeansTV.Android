package de.markhaehnel.rbtv.rocketbeanstv.ui.serviceinfo

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import de.markhaehnel.rbtv.rocketbeanstv.AppExecutors
import de.markhaehnel.rbtv.rocketbeanstv.R
import de.markhaehnel.rbtv.rocketbeanstv.binding.FragmentDataBindingComponent
import de.markhaehnel.rbtv.rocketbeanstv.databinding.FragmentServiceInfoBinding
import de.markhaehnel.rbtv.rocketbeanstv.di.Injectable
import de.markhaehnel.rbtv.rocketbeanstv.ui.common.ClickCallback
import de.markhaehnel.rbtv.rocketbeanstv.ui.common.RetryCallback
import de.markhaehnel.rbtv.rocketbeanstv.ui.common.SharedViewModel
import de.markhaehnel.rbtv.rocketbeanstv.util.autoCleared
import kotlinx.android.synthetic.main.fragment_service_info.*
import javax.inject.Inject
import kotlin.math.roundToInt

class ServiceInfoFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var appExecutors: AppExecutors

    private var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    var binding by autoCleared<FragmentServiceInfoBinding>()

    private lateinit var serviceInfoViewModel: ServiceInfoViewModel
    private lateinit var sharedViewModel: SharedViewModel

    private lateinit var autoCloseRunnable: Runnable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val dataBinding = DataBindingUtil.inflate<FragmentServiceInfoBinding>(
            inflater,
            R.layout.fragment_service_info,
            container,
            false,
            dataBindingComponent
        )

        dataBinding.retryCallback = object : RetryCallback {
            override fun retry() {
                serviceInfoViewModel.retry()
            }
        }

        dataBinding.onScheduleClickCallback = object : ClickCallback {
            override fun click () {
                parentFragmentManager.popBackStack()
                sharedViewModel.showSchedule()
            }
        }

        dataBinding.onChatClickCallback = object : ClickCallback {
            override fun click() {
                sharedViewModel.toggleChat()
            }
        }

        dataBinding.onSettingsClickCallback = object : ClickCallback {
            override fun click() {
                findNavController().navigate(R.id.action_global_preferencesFragment)
            }
        }

        binding = dataBinding
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        serviceInfoViewModel = ViewModelProvider(this, viewModelFactory).get(ServiceInfoViewModel::class.java)
        binding.lifecycleOwner = viewLifecycleOwner

        sharedViewModel = activity?.run {
            ViewModelProvider(this).get(SharedViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        binding.serviceInfo = serviceInfoViewModel.serviceInfo

        autoCloseRunnable = Runnable {
            parentFragmentManager.popBackStack()
        }

        initServiceInfo()
        serviceInfoScheduleButton.requestFocus()
    }

    private fun initServiceInfo() {
        serviceInfoViewModel.serviceInfo.observe(viewLifecycleOwner, Observer { serviceInfo ->
            if (serviceInfo.data != null) {
                progressBar.apply {
                    isIndeterminate = false
                }
                val prog = serviceInfo.data.service.streamInfo.showInfo.progress.roundToInt() * 100
                val progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, prog)
                progressAnimator.duration = 1000
                progressAnimator.interpolator = DecelerateInterpolator()
                progressAnimator.start()
            }
        })
    }
}
