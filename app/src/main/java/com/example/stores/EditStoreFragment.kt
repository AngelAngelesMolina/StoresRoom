package com.example.stores

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.example.stores.databinding.FragmentEditStoreBinding
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.LinkedBlockingQueue


class EditStoreFragment : Fragment() {
    private lateinit var mBinding: FragmentEditStoreBinding
    private var mActivity: MainActivity? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = FragmentEditStoreBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mActivity = activity as? MainActivity  //conseguir actividad y castearla
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mActivity?.supportActionBar?.title = getString(R.string.edit_store_title_add)
        setHasOptionsMenu(true) //tener acceso al menu
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_save, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                mActivity?.onBackPressedDispatcher?.onBackPressed()
                true
            }

            R.id.action_save -> {
                val store = StoreEntity(
                    name = mBinding.etName.text.toString().trim(),
                    phone = mBinding.etPhone.toString().trim(),
                    website = mBinding.etWebsite.toString().trim()
                )
                val queue = LinkedBlockingQueue<Long?>()
                Thread {
                    val id = StoreApplication.database.storeDao().addStore(store)
                    queue.add(id)
                }.start()
                queue.take()?.let {
                    hideKeyboard()
                    Snackbar.make(
                        mBinding.root,
                        getString(R.string.edit_store_message_save_success),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    mActivity?.onBackPressedDispatcher?.onBackPressed()
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun hideKeyboard() {
        val imm = mActivity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    override fun onDestroyView() {
        hideKeyboard()
        super.onDestroyView()
    }
    override fun onDestroy() {
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        mActivity?.supportActionBar?.title = getString(R.string.app_name)
        setHasOptionsMenu(false)
        mActivity?.hideFab(true)
        super.onDestroy()
    }

}