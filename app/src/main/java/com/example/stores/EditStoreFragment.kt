package com.example.stores

import android.content.Context
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.stores.databinding.FragmentEditStoreBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import java.util.concurrent.LinkedBlockingQueue


class EditStoreFragment : Fragment() {
    private lateinit var mBinding: FragmentEditStoreBinding
    private var mActivity: MainActivity? = null
    private var mIsEditMode: Boolean = false
    private var mStoreEntity: StoreEntity? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentEditStoreBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = arguments?.getLong(getString(R.string.arg_id), 0)
        if (id != null && id != 0L) {
            mIsEditMode = true
            getStore(id)
        } else {
            mIsEditMode = false
            mStoreEntity = StoreEntity(name = "", phone = "", photoUrl = "")
        }

        setupActionBar()
        setupTextFields()
    }

    private fun setupActionBar() {
        mActivity = activity as? MainActivity  //conseguir actividad y castearla
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (mIsEditMode) mActivity?.supportActionBar?.title =
            if (mIsEditMode) getString(R.string.edit_store_title_edit)
            else getString(R.string.edit_store_title_add)
        setHasOptionsMenu(true) //tener acceso al menu
    }

    private fun loadImage(url: String) {
        Glide.with(this).load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().into(mBinding.imgPhoto)
    }

    private fun setupTextFields() {
        with(mBinding) {
            etName.addTextChangedListener() {
                validateFields(tilName)
            }
            etPhone.addTextChangedListener() {
                validateFields(tilPhone)
            }
            etPhotoUrl.addTextChangedListener() {
                loadImage(etPhotoUrl.text.toString())
                validateFields(tilPhotoUrl)
            }
        }
    }

    private fun getStore(id: Long) {
        val queue = LinkedBlockingQueue<StoreEntity?>()
        Thread {
            mStoreEntity = StoreApplication.database.storeDao().getStoreById(id)
            queue.add(mStoreEntity)
        }.start()
        queue.take()?.let {
            setUiStore(it)
        }
    }

    private fun setUiStore(storeEntity: StoreEntity) {
        with(mBinding) {
            etName.setText(storeEntity.name)
            etPhone.setText(storeEntity.phone)
            etWebsite.setText(storeEntity.website)
            etPhotoUrl.setText(storeEntity.photoUrl)
        }
    }

    private fun String.editable(): Editable = Editable.Factory.getInstance().newEditable(this)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_save, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    MaterialAlertDialogBuilder(requireActivity())
                        .setTitle(R.string.dialog_exit_title)
                        .setMessage(R.string.dialog_exit_message)
                        .setPositiveButton(R.string.dialog_exit_ok) { _, _ ->
                            if (isEnabled) {
                                isEnabled = false
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                        }
                        .setNegativeButton(R.string.dialog_delete_cancel, null)
                        .show()
                }
            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                hideKeyboard()
                requireActivity().onBackPressedDispatcher.onBackPressed()
                true
            }

            R.id.action_save -> {
                if (mStoreEntity != null && validateFields(
                        mBinding.tilPhotoUrl,
                        mBinding.tilPhone,
                        mBinding.tilName
                    )
                ) {/*val store = StoreEntity(
                    name = mBinding.etName.text.toString().trim(),
                    phone = mBinding.etPhone.toString().trim(),
                    website = mBinding.etWebsite.toString().trim(),
                    photoUrl = mBinding.etPhotoUrl.toString().trim()
                )*/
                    with(mStoreEntity!!) {
                        name = mBinding.etName.text.toString().trim()
                        phone = mBinding.etPhone.text.toString().trim()
                        website = mBinding.etWebsite.text.toString().trim()
                        photoUrl = mBinding.etPhotoUrl.text.toString().trim()
                    }
                    val queue = LinkedBlockingQueue<StoreEntity>()
                    Thread {
                        if (mIsEditMode) StoreApplication.database.storeDao()
                            .updateStore(mStoreEntity!!)
                        else mStoreEntity!!.id =
                            StoreApplication.database.storeDao().addStore(mStoreEntity!!)
                        queue.add(mStoreEntity)
                    }.start()
                    with(queue.take()) {
                        if (mIsEditMode) {
                            mActivity?.updateStore(this)
                            Snackbar.make(
                                mBinding.root,
                                R.string.edit_store_message_update_success,
                                Snackbar.LENGTH_SHORT
                            ).show()
                        } else {
                            mActivity?.addStore(this)
                            Toast.makeText(
                                mActivity,
                                R.string.edit_store_message_save_success,
                                Toast.LENGTH_SHORT
                            ).show()
                            hideKeyboard()
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true
        if (mBinding.etPhotoUrl.text.toString().trim().isEmpty()) {
            mBinding.tilPhotoUrl.error = getString(R.string.helper_required)
            mBinding.etPhotoUrl.requestFocus()
            isValid = false;
        }
        if (mBinding.etPhone.text.toString().trim().isEmpty()) {
            mBinding.tilPhone.error = getString(R.string.helper_required)
            mBinding.etPhone.requestFocus()
            isValid = false;
        }
        if (mBinding.etName.text.toString().trim().isEmpty()) {
            mBinding.tilName.error = getString(R.string.helper_required)
            mBinding.etName.requestFocus()
            isValid = false;
        }
        return isValid
    }

    private fun validateFields(vararg textFields: TextInputLayout): Boolean {
        var isValid = true;
        for (textField in textFields) {
            if (textField.editText?.text.toString().trim().isEmpty()) {
                textField.error = getString(R.string.helper_required)
                //textField.editText.requestFocus()
                isValid = false
            } else {
                textField.error = null
            }
        }
        if (!isValid) Snackbar.make(
            mBinding.root,
            R.string.edit_store_message_valid,
            Snackbar.LENGTH_SHORT
        ).show()
        return isValid;
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