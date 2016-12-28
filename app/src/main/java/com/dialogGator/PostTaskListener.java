package com.dialogGator;

import android.content.Context;

public interface PostTaskListener<K> {

    void onPostTask(K result, Context context);
}
