package com.dialogGator;

import android.app.Application;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

/**
 * Created by 1234567 on 13-11-2016.
 */
@Singleton
public class ListenerTask extends Application{
    private PostTaskListener<ArrayList<Product>> postTaskListener;
    private List<Product> productList;
    public PostTaskListener<ArrayList<Product>> getPostTaskListener() {
        return postTaskListener;
    }

    public void setPostTaskListener(PostTaskListener<ArrayList<Product>> postTaskListener) {
        this.postTaskListener = postTaskListener;
    }

    public void setProductList(List<Product> productList){
        this.productList = productList;
    }

    public List<Product> getProductList(){
        return productList;
    }
}
