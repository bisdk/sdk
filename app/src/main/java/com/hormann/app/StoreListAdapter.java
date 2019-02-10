package com.hormann.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.hormann.app.discover.AppDatabase;
import com.hormann.app.discover.Gateway;
import com.hormann.app.discover.UserDao;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

public class StoreListAdapter extends ArrayAdapter {

    private List<Gateway> dataList = new ArrayList<>();
    private int itemLayout;

    private LifecycleOwner owner;
    private UserDao localDatabaseRepo;

    private StoreListAdapter.ListFilter listFilter = new StoreListAdapter.ListFilter();

    public StoreListAdapter(@NonNull LifecycleOwner owner, @NonNull Context context) {
        super(context, R.layout.gateway_dropdown);
        this.owner = owner;
        localDatabaseRepo = AppDatabase.Companion.getInstance(context).userDao();
        itemLayout = R.layout.gateway_dropdown;

        localDatabaseRepo.getAll().observe(owner, gateways -> listFilter.newStuff(gateways));
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Gateway getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {

        if (view == null) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(itemLayout, parent, false);
        }

        TextView strName = view.findViewById(R.id.store);
        strName.setText(getItem(position).getHost());

        TextView couponCount = view.findViewById(R.id.coupon);
        couponCount.setText("" + getItem(position).getPort());
        return view;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return listFilter;
    }

    public class ListFilter extends Filter {
        FilterResults results = new FilterResults();
        private CharSequence prefix;

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            this.prefix = prefix;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.values != null) {
                dataList = (ArrayList<Gateway>) results.values;
            } else {
                dataList = null;
            }
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

        void newStuff(List<Gateway> gateways) {
            results.values = gateways;
            results.count = gateways.size();
            publishResults(prefix, results);
        }
    }
}