package com.dialogGator;

import android.app.Application;

import java.util.HashMap;

import javax.inject.Singleton;

/**
 * Created by yash on 12/11/16.
 */
public class ProductAttributes {
    public static HashMap productMap;

    public static void init(){
        if(productMap==null){
            productMap = new HashMap();
        }
    }
    public  ProductAttributes() {
        this.productMap = new HashMap();
    }

    public static HashMap getMyMap() {
        return productMap;
    }
}
