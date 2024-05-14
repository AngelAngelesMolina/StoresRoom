package com.example.stores

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.stores.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), OnClickListener {
    private lateinit var mBinding: ActivityMainBinding

    private lateinit var mAdapter: StoreAdapter
    private lateinit var mGridLayout: GridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.btnSave.setOnClickListener {
            val store = Store(name = mBinding.etName.text.toString().trim())
            mAdapter.add(store)
        }

        setupRv()
    }

    private fun setupRv() {
        mAdapter = StoreAdapter(mutableListOf(), this)
        mGridLayout = GridLayoutManager(this, 2)
        mBinding.rv.apply {
            setHasFixedSize(true)
            adapter = mAdapter
            layoutManager = mGridLayout
        }

    }

    override fun onClick(store: Store) {
        TODO("Not yet implemented")
    }
}