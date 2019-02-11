package com.hormann.app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.hormann.app.discover.AppDatabase
import com.hormann.app.discover.Gateway

class StoreListAdapter(owner: LifecycleOwner, context: Context) : ArrayAdapter<Gateway>(context, R.layout.gateway_dropdown) {

    private val itemLayout: Int

    private val listFilter = ListFilter()

    init {
        val localDatabaseRepo = AppDatabase.getInstance(context).userDao()
        itemLayout = R.layout.gateway_dropdown

        localDatabaseRepo.getAll().observe(owner, Observer<List<Gateway>> { this.newStuff(it) })
    }

    private fun newStuff(gateways: List<Gateway>) {
        listFilter.newStuff(gateways)

    }

    override fun getItemId(position: Int): Long {
        return getItem(position)?.mac.hashCode().toLong()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var shadowView = view

        if (shadowView == null) {
            shadowView = LayoutInflater.from(parent.context)
                    .inflate(itemLayout, parent, false)
        }

        val strName = shadowView!!.findViewById<TextView>(R.id.store)
        strName.text = getItem(position)?.sourceAddress

        val couponCount = shadowView.findViewById<TextView>(R.id.coupon)
        couponCount.text = getItem(position)?.mac
        return shadowView
    }

    override fun getFilter(): Filter {
        return listFilter
    }

    inner class ListFilter : Filter() {
        private var results: FilterResults = Filter.FilterResults()
        private var prefix: CharSequence? = null

        override fun performFiltering(prefix: CharSequence): Filter.FilterResults {
            this.prefix = prefix
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults) {
            if (results.count > 0) {
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }

        internal fun newStuff(gateways: List<Gateway>) {
            clear()
            addAll(gateways)
            results.values = gateways
            results.count = gateways.size
            publishResults(prefix, results)
        }
    }
}