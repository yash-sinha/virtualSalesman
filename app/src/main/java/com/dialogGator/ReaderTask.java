package com.dialogGator;

import android.content.Context;
import android.os.AsyncTask;

import java.util.*;

public class ReaderTask extends AsyncTask< HashMap<String, String>, Void, ArrayList<Product> >
{
    protected static final String TAG = "DataAdapter";

    private final Context mContext;
    private DBHelper mDbHelper;

    private PostTaskListener<ArrayList<Product> > postTaskListener;

    public ReaderTask(Context context, PostTaskListener<ArrayList<Product> > postTaskListener)
    {
        this.mContext = context;
        mDbHelper = DBHelper.getInstance(mContext);
        this.postTaskListener = postTaskListener;
    }

    @Override
    protected ArrayList<Product> doInBackground(HashMap<String, String>... searchBox)
    {
        return mDbHelper.Query(searchBox[0]);
    }

    @Override
    protected void onPostExecute(ArrayList<Product> result) {
        super.onPostExecute(result);

        if (result != null && postTaskListener != null)
            postTaskListener.onPostTask(result, mContext);
    }
}
