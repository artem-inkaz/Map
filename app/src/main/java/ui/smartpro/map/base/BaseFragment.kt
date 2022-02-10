package ui.smartpro.map.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil.inflate
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.launch

abstract class BaseFragment<Binding: ViewBinding> : Fragment(){

    val TAG = javaClass.simpleName
//    protected abstract val viewModel: VM
    protected abstract val layoutId: Int

    protected lateinit var binding: Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflate(inflater, layoutId, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initViewModel()
        initEditAccount()
    }

    protected open fun initViewModel() {}
    protected open fun initViews() {}
    protected open fun initEditAccount() {}
    protected open fun initImage( data: Intent?) {}
    protected open fun initImage2() {}
    protected open fun initCheckAuth() {}

    protected fun doInScope(
        state: Lifecycle.State = Lifecycle.State.STARTED,
        action: suspend () -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(state) {
                action()
            }
        }
    }

    protected fun doInScopeResume(
        state: Lifecycle.State = Lifecycle.State.RESUMED,
        action: suspend () -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(state) {
                action()
            }
        }
    }
    //показывает тост с текстом. Принимает String. Ничего не возвращает.
    fun showMessage (message: String){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }


}