package com.example.stores

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.stores.databinding.ActivityMainBinding
import java.util.concurrent.LinkedBlockingQueue

class MainActivity : AppCompatActivity(), OnClickListener {
    private lateinit var mBinding: ActivityMainBinding

    private lateinit var mAdapter: StoreAdapter
    private lateinit var mGridLayout: GridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.btnSave.setOnClickListener {
            val storeEntity = StoreEntity(name = mBinding.etName.text.toString().trim())
            Thread {
                StoreApplication.database.storeDao().addStore(storeEntity)
            }.start()

            mAdapter.add(storeEntity)
        }

        setupRv()

    }

    private fun getStores() {
        //MANEJO DE HILOS NATIVOS EN KT
        val queue = LinkedBlockingQueue<MutableList<StoreEntity>>()
        Thread {
            val stores = StoreApplication.database.storeDao().getAllStores()
            queue.add(stores)
        }.start()
        mAdapter.setStores(queue.take())
    }

    private fun setupRv() {
        mAdapter = StoreAdapter(mutableListOf(), this)
        mGridLayout = GridLayoutManager(this, 2)
        getStores()
        mBinding.rv.apply {
            setHasFixedSize(true)
            adapter = mAdapter
            layoutManager = mGridLayout
        }

    }

    override fun onClick(storeEntity: StoreEntity) {
        TODO("Not yet implemented")
    }

    override fun onFavoriteStore(storeEntity: StoreEntity) {
        storeEntity.isFavorite = !storeEntity.isFavorite
        val queue = LinkedBlockingQueue<StoreEntity>()
        Thread {
            StoreApplication.database.storeDao().updateStore(storeEntity)
            queue.add(storeEntity)
        }.start()
        mAdapter.update(queue.take())
    }

    override fun onDeleteStore(storeEntity: StoreEntity) {
        val queue = LinkedBlockingQueue<StoreEntity>()
        Thread{
            StoreApplication.database.storeDao().deleteStore(storeEntity)
            queue.add(storeEntity)
        }.start()
        mAdapter.delete(queue.take())
    }
}