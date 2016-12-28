package com.dialogGator;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.voice.APIAITaskAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements FragmentOnBackClickInterface{


    public MainFragment() {
        // Required empty public constructor
    }

    public static final String PRODUCT_ID = "PRODUCT_ID";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Inflate the layout for this fragment
        //String[] items = getResources().getStringArray(R.array.clothing);

        PostTaskListener<ArrayList<Product>> postTaskListener = new PostTaskListener<ArrayList<Product>>() {
            @Override
            public void onPostTask(final ArrayList<Product> result, Context mContext) {
                //products = result;
                if (result.size() != 0) {
                    ((ListenerTask) getActivity().getApplication()).setProductList(result);
                    ProductListAdapter adapter = new ProductListAdapter(
                            getActivity(), R.layout.list_item, result);
                    ListView lv = (ListView) rootView.findViewById(R.id.listView);
                    lv.setAdapter(adapter);
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(MainFragment.this.getContext(), DetailActivity.class);
                            Product product = result.get(position);
                            intent.putExtra(PRODUCT_ID, product.getProductId());
                            startActivity(intent);
                        }
                    });
                    if (result.size() == 1) {
                        Intent intent = new Intent(MainFragment.this.getContext(), DetailActivity.class);
                        Product product = result.get(0);
                        intent.putExtra(PRODUCT_ID, product.getProductId());
                        startActivity(intent);
                    }
                    String attributes = "";
                    final HashMap productMap = ProductAttributes.productMap;
                    if (productMap != null) {
                        Iterator it = productMap.entrySet().iterator();
                        String attri = "";
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry) it.next();
                            attri = pair.getKey().toString();
//                        Log.i("Chutzpah", pair.getKey() + " = " + pair.getValue());
                        if(attri.contains("category") || attri.contains("brand") || attri.contains("color") || attri.contains("gender") || attri.contains("size") || attri.contains("priceEnd") || attri.contains("priceStart")) {
                            attributes = pair.getValue() + ", " +attributes;
                        }
                    }

                    }
                    Log.i("df", attributes);
                    Toolbar searchBar = (Toolbar) getActivity().findViewById(R.id.search_bar);
                    searchBar.setTitle("Filters: " + attributes);
                }
            }
        };


        ((ListenerTask) getActivity().getApplication()).setPostTaskListener(postTaskListener);
        DataProvider dp = new DataProvider(this.getActivity());
        dp.getProducts();

        return rootView;

    }

    @Override
    public void onClick() {

    }

    /*@Override
    public void onBackPressed()
    {
        MainFragment fragment = new MainFragment();
        FragmentTransaction fragmentTransaction =
                this.getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }*/


}
