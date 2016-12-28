package com.voice;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.dialogGator.DBHelper;
import com.dialogGator.DetailActivity;
import com.dialogGator.ListenerTask;
import com.dialogGator.MainFragment;
import com.dialogGator.PostTaskListener;
import com.dialogGator.Product;
import com.dialogGator.ProductAttributes;
import com.dialogGator.ReaderTask;

//import org.json.JSONArray;
//import org.json.JSONObject;
//import org.google.gson.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

import ai.api.AIConfiguration;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import ai.api.ui.AIDialog;

/**
 * Created by yash on 11/11/16.
 */

@Singleton
public class APIAITaskAgent {
    private final AIDialog aiDialog;
    public static int flag_ground=0;
    public static int correctASR = 0;
    public static int incorrectASR = 0;
    public static String prevAttrValue= "";
    public static int flag_prodsearch=1;
    public static int context = 0;
    public static String TAG = "APIAI";
    @Inject
    public APIAITaskAgent(final Activity activity){
        AIConfiguration aiConfiguration =  new AIConfiguration(
                "ca9ddb0dbbb64343a90b2ea4e75a41a6",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiDialog = new AIDialog(activity, aiConfiguration);
        Log.i("HashMap", String.valueOf(ProductAttributes.productMap));

//        final PostTaskListener<ArrayList<Product>> postTaskListener = new PostTaskListener<ArrayList<Product>>() {
//            @Override
//            public void onPostTask(ArrayList<Product> result, Context context ) {
//                Log.i("Result", result.toString());
//            }
//        };

        aiDialog.setResultsListener(new AIDialog.AIDialogListener() {
            @Override
            public void onResult(AIResponse response) {
                try {
                    if (!response.isError()) {
//                        final ReaderTask readerTask = new ReaderTask(activity.getApplicationContext(),postTaskListener);
                        Result result = response.getResult();
                        String speech = result.getFulfillment().getSpeech();

                        Log.i(TAG, "User : " + result.getResolvedQuery().toString());
                        if (result.getParameters().get("help") != null || result.getResolvedQuery().contains("help")) {
                            Log.i(TAG, "User: Help");
                            TTS.speak("Hello. You can say \"I want a shirt\" or you can choose from Pant, Jean, Short, " +
                                            "Shirt, Jacket, Skirt, Dress or Legging. You can start over by saying \"start over\".");
                            Log.i(TAG, "System: Help response");
                        } else if (result.getParameters().get("purpose") != null) {
                            Log.i(TAG, "System: No Purpose");
                            TTS.speak("Hi, I am Reena. I am your shopping assistant and can assist you in buying clothes." +
                                    "I can show you our collection of Pants, Shorts, Shirts and other items.");
                        }
                        else if(checkValidContext(result.getAction().toString())==false){
                            Log.i(TAG, "System: Invalid Option");
                            TTS.speak("This is not a valid option.");
                            TTS.speak(getCorrectContextDialogue(ProductAttributes.productMap.get("prevDialog").toString
                                    ()));
                            //TTS.speak(getNextDialogue());
                        }else {
                            switch (result.getAction()) {
                                case "open.prompt":
                                    Log.i(TAG, "Mode: open prompt");
                                    if(flag_ground==1 && result.getResolvedQuery().toString().equals("yes")){
                                        Log.i(TAG, "ASR: Correct Count : " + Integer.toString(correctASR));
                                        flag_ground=0;
                                        correctASR= correctASR+1;
                                        setAttrValue(prevAttrValue);
                                        PostTaskListener postTaskListener_prod = init(activity);
                                        if(checkResultsNotZero(activity,ProductAttributes.productMap)){
                                            speech = getNextDialogue();
                                            TTS.speak(speech);
                                        }
                                        final ReaderTask readerTask_prod = new ReaderTask(activity.getApplicationContext
                                                (), postTaskListener_prod);
                                        readerTask_prod.execute(ProductAttributes.productMap);
                                    }
                                    else if (flag_ground==1 && result.getResolvedQuery().toString().equals("no")){
                                        Log.i(TAG, "ASR: Incorrect Count : " + Integer.toString(incorrectASR));
                                        flag_ground=0;
                                        incorrectASR = incorrectASR+1;
                                        TTS.speak(findDialogue(ProductAttributes.productMap.get("prevDialog").toString
                                                ()));
                                    }
                                    else if(result.getResolvedQuery().toString().equals("no")){
                                        Log.i(TAG, "System: No Grounding : User response : no");
                                        setAttrValue("");
                                    }
                                    else if(context==1){
                                        //handle clothes product here!!
                                        PostTaskListener postTaskListener_prod = init(activity);
                                        final ReaderTask readerTask_prod = new ReaderTask(activity.getApplicationContext
                                                (), postTaskListener_prod);
                                        if (!result.getParameters().isEmpty()) {
                                            HashMap tempMap = new HashMap();
                                            String trainedAIString= AItrain(result.getResolvedQuery().toString());
                                            if (trainedAIString.contains("\""))
                                                tempMap.put("category", trainedAIString.replaceAll("\"", ""));
                                            else
                                                tempMap.put("category", trainedAIString);
                                            //TTS.speak(speech);
                                            if (checkResultsNotZero(activity,tempMap) && flag_prodsearch==1) {
                                                toggleProdSearchFlag();
                                                if (trainedAIString.contains("\""))
                                                    ProductAttributes.productMap.put("category",
                                                            trainedAIString.replaceAll("\"", ""));
                                                else
                                                    ProductAttributes.productMap.put("category", trainedAIString);

                                                if(checkResultsNotZero(activity,ProductAttributes.productMap) &&
                                                        flag_prodsearch==0){
                                                    toggleProdSearchFlag();
                                                    speech = getNextDialogue();
                                                    TTS.speak(speech);
                                                }
                                                readerTask_prod.execute(ProductAttributes.productMap);
                                            }
                                        } else {
                                            speech = getRandomUtterance();
                                            TTS.speak(speech);
                                        }
                                    }
                                    else {
                                        PostTaskListener postTaskListener = init(activity);
                                        final ReaderTask readerTask = new ReaderTask(activity.getApplicationContext(),
                                                postTaskListener);
                                        if (!result.getParameters().isEmpty() && ProductAttributes.productMap.get
                                                ("open_done") != "1") {
                                            String[] queryItemsList = getQueryItems(result.getParameters());
                                            if (queryItemsList != null) {
                                                Log.i(TAG, "System: Open prompt : With Item");
                                                //DBHelper.getInstance(activity.getApplicationContext()).populateMapOnOpenPrompt(queryItemsList);
                                                HashMap resultMap = DBHelper.getInstance(activity.getApplicationContext
                                                        ()).populateMapOnOpenPrompt(queryItemsList);
                                                if (checkIfItemExists(resultMap)){
                                                    ProductAttributes.productMap.put("open_done", "1");
                                                    if (checkResultsNotZero(activity,resultMap)){
                                                        speech = getNextDialogue();
                                                        TTS.speak(speech);
                                                    }
                                                    readerTask.execute(resultMap);//TODO

                                                }

                                            } else {
                                                Log.i(TAG, "System: Open prompt : Without Item");
                                                speech = getRandomUtterance();
                                                TTS.speak(speech);
                                            }
                                        } else if (ProductAttributes.productMap.get("open_done") == "1") {
                                            Log.i(TAG, "System: Open prompt : Invalid Option");
                                            TTS.speak(getCorrectContextDialogue(ProductAttributes.productMap.get
                                                    ("prevDialog").toString()));
                                            //speech = getNextDialogue();
                                            TTS.speak(speech);
                                        }
                                    }
                                    break;
                                case "clothes.product":
                                    Log.i(TAG, "System: Close Prompt : Filter : Category");
                                    PostTaskListener postTaskListener_prod = init(activity);
                                    final ReaderTask readerTask_prod = new ReaderTask(activity.getApplicationContext(),
                                            postTaskListener_prod);
                                    if (!result.getParameters().isEmpty()) {
                                        Log.i(TAG, "System: Close Prompt : Filter : Category : HasValue");
                                        HashMap tempMap = new HashMap();
                                        if (result.getResolvedQuery().toString().contains("\""))
                                            tempMap.put("category", result.getResolvedQuery().toString().replaceAll("\"",
                                                    ""));
                                        else
                                            tempMap.put("category", result.getResolvedQuery().toString());
                                        //TTS.speak(speech);
                                        if (checkResultsNotZero(activity,tempMap) && flag_prodsearch==1) {
                                            toggleProdSearchFlag();
                                            if (result.getResolvedQuery().toString().contains("\""))
                                                ProductAttributes.productMap.put("category", result.getResolvedQuery
                                                        ().toString().replaceAll("\"", ""));
                                            else
                                                ProductAttributes.productMap.put("category", result.getResolvedQuery
                                                        ().toString());

                                            if(checkResultsNotZero(activity,ProductAttributes.productMap) &&
                                                    flag_prodsearch==0){
                                                toggleProdSearchFlag();
                                                speech = getNextDialogue();
                                                TTS.speak(speech);
                                            }
                                            readerTask_prod.execute(ProductAttributes.productMap);

                                        }
                                    } else {
                                        Log.i(TAG, "System: Close Prompt : Filter : Category : NoValue");
                                        speech = getRandomUtterance();
                                        TTS.speak(speech);
                                    }
                                    break;
                                case "clothes.gender":
                                    Log.i(TAG, "System: Close Prompt : Filter : Gender");
                                    if(flag_ground==0 && randomizeGrounding()==1){
                                        Log.i(TAG, "System: Close Prompt : Filter : Gender : Ground");
                                        groundDialog(result.getParameters().get("gender").toString());
                                    }
                                    else {
                                        PostTaskListener postTaskListener_gender = init(activity);
                                        final ReaderTask readerTask_gender = new ReaderTask
                                                (activity.getApplicationContext(), postTaskListener_gender);
                                        if (!result.getParameters().isEmpty()) {
                                            Log.i(TAG, "System: Close Prompt : Filter : Gender : HasValue");
                                            if (result.getParameters().get("gender").toString().contains("\""))
                                                ProductAttributes.productMap.put("gender", result.getParameters().get
                                                        ("gender").toString().replaceAll("\"", ""));
                                            else
                                                ProductAttributes.productMap.put("gender", result.getParameters().get
                                                        ("gender").toString());

                                            if (checkResultsNotZero(activity,ProductAttributes.productMap)){
                                                speech = getNextDialogue();
                                                TTS.speak(speech);
                                            }
                                            readerTask_gender.execute(ProductAttributes.productMap);

                                        } else {
                                            Log.i(TAG, "System: Close Prompt : Filter : Gender : NoValue");
                                            speech = getRandomUtterance();
                                            TTS.speak(speech);
                                        }
                                    }
                                    break;
                                case "clothes.size":
                                    Log.i(TAG, "System: Close Prompt : Filter : Size");
                                    if(flag_ground==0 && randomizeGrounding()==1){
                                        Log.i(TAG, "System: Close Prompt : Filter : Size : Ground");
                                        groundDialog(result.getParameters().get("size").toString());
                                    }
                                    else {
                                        PostTaskListener postTaskListener_size = init(activity);
                                        final ReaderTask readerTask_size = new ReaderTask(activity.getApplicationContext
                                                (), postTaskListener_size);
                                        if (!result.getParameters().isEmpty()) {
                                            Log.i(TAG, "System: Close Prompt : Filter : Size : HasValue");
                                            if (result.getParameters().get("size").toString().contains("\""))
                                                ProductAttributes.productMap.put("size", result.getParameters().get
                                                        ("size").toString().replaceAll("\"", ""));
                                            else
                                                ProductAttributes.productMap.put("size", result.getParameters().get
                                                        ("size").toString());

                                            if (checkResultsNotZero(activity,ProductAttributes.productMap)) {
                                                speech = getNextDialogue();
                                                TTS.speak(speech);
                                            }
                                            readerTask_size.execute(ProductAttributes.productMap);

                                        } else {
                                            Log.i(TAG, "System: Close Prompt : Filter : Size : NoValue");
                                            speech = getRandomUtterance();
                                            TTS.speak(speech);
                                        }
                                    }
                                    break;
                                case "clothes.color":
                                    Log.i(TAG, "System: Close Prompt : Filter : Color");
                                    if(flag_ground==0 && randomizeGrounding()==1){
                                        Log.i(TAG, "System: Close Prompt : Filter : Color : Ground");
                                        groundDialog(result.getParameters().get("color").toString());
                                    }
                                    else {
                                        PostTaskListener postTaskListener_color = init(activity);
                                        final ReaderTask readerTask_color = new ReaderTask(activity.getApplicationContext
                                                (), postTaskListener_color);
                                        if (!result.getParameters().isEmpty()) {
                                            Log.i(TAG, "System: Close Prompt : Filter : Color : HasValue");
                                            if (result.getParameters().get("color").toString().contains("\""))
                                                ProductAttributes.productMap.put("color", result.getParameters().get
                                                        ("color").toString().replaceAll("\"", ""));
                                            else
                                                ProductAttributes.productMap.put("color", result.getParameters().get
                                                        ("color").toString());

                                            if (checkResultsNotZero(activity,ProductAttributes.productMap)){
                                                speech = getNextDialogue();
                                                TTS.speak(speech);
                                            }
                                            readerTask_color.execute(ProductAttributes.productMap);

                                        } else {
                                            Log.i(TAG, "System: Close Prompt : Filter : Color : NoValue");
                                            speech = getRandomUtterance();
                                            TTS.speak(speech);
                                        }
                                    }
                                    break;

                                case "clothes.pricevalue":
                                    Log.i(TAG, "System: Close Prompt : Filter : Price");
                                    PostTaskListener postTaskListener_price = init(activity);
                                    final ReaderTask readerTask_price = new ReaderTask(activity.getApplicationContext(),
                                            postTaskListener_price);
                                    if (!result.getParameters().isEmpty()) {
                                        if (result.getParameters().get("price") != null) {

                                            Log.i(TAG, "System: Close Prompt : Filter : Price : HasValue");
//                                                if (result.getParameters().get("price").toString().contains("\""))
//                                                    ProductAttributes.productMap.put("priceStart", result.getParameters().get("price").toString().replaceAll("\"", ""));
//                                                else
                                            ProductAttributes.productMap.put("priceStart", getPriceAttr
                                                    (result.getParameters().get("price"), "price"));

//                                                if (result.getParameters().get("price").toString().contains("\""))
//                                                    ProductAttributes.productMap.put("priceEnd", result.getParameters().get("price").toString().replaceAll("\"", ""));
//                                                else
                                            ProductAttributes.productMap.put("priceEnd", getPriceAttr
                                                    (result.getParameters().get("price"), "price"));
                                        } else {
                                            if (result.getParameters().get("rangestart") == null) {
                                                ProductAttributes.productMap.put("priceStart", "0");
                                            } //else if (result.getParameters().get("rangestart").toString().contains("\""))
//                                                    ProductAttributes.productMap.put("priceStart", result.getParameters().get("rangestart").toString().replaceAll("\"", ""));
                                            else
                                            ProductAttributes.productMap.put("priceStart", getPriceAttr
                                                    (result.getParameters().get("rangestart"), "rangestart"));

                                            if (result.getParameters().get("rangeend") == null) {
                                                ProductAttributes.productMap.put("priceEnd", "9999999");
                                            } //else if (result.getParameters().get("rangeend").toString().contains("\""))
//                                                    ProductAttributes.productMap.put("priceEnd", result.getParameters().get("rangeend").toString().replaceAll("\"", ""));
                                            else
                                            ProductAttributes.productMap.put("priceEnd", getPriceAttr
                                                    (result.getParameters().get("rangeend"), "rangeend"));
                                        }
                                        speech = getNextDialogue();
                                        TTS.speak(speech);
                                        if (checkResultsNotZero(activity,ProductAttributes.productMap)) {
                                            speech = getNextDialogue();
                                            TTS.speak(speech);
                                        }
                                        readerTask_price.execute(ProductAttributes.productMap);

                                    } else {
                                        Log.i(TAG, "System: Close Prompt : Filter : Price : NoValue");
                                        speech = getRandomUtterance();
                                        TTS.speak(speech);
                                    }
//                                    TTS.speak(speech);

                                    break;

                                case "clothes.brand":
                                    Log.i(TAG, "System: Close Prompt : Filter : Brand");
                                    if(flag_ground==0 && randomizeGrounding()==1){
                                        groundDialog(result.getParameters().get("brand").toString());
                                    }
                                    else {
                                        PostTaskListener postTaskListener_brand = init(activity);
                                        final ReaderTask readerTask_brand = new ReaderTask(activity.getApplicationContext
                                                (), postTaskListener_brand);
                                        if (!result.getParameters().isEmpty()) {
                                            Log.i(TAG, "System: Close Prompt : Filter : Brand : HasValue");
                                            if (result.getParameters().get("brand").toString().contains("\""))
                                                ProductAttributes.productMap.put("brand", result.getParameters().get
                                                        ("brand").toString().replaceAll("\"", ""));
                                            else
                                                ProductAttributes.productMap.put("brand", result.getParameters().get
                                                        ("brand").toString());
                                            speech = getNextDialogue();
                                            TTS.speak(speech);
                                            if (checkResultsNotZero(activity,ProductAttributes.productMap)) {
                                                speech = getNextDialogue();
                                                TTS.speak(speech);
                                            }
                                            readerTask_brand.execute(ProductAttributes.productMap);

                                        } else {
                                            Log.i(TAG, "System: Close Prompt : Filter : Brand : NoValue");
                                            speech = getRandomUtterance();
                                            TTS.speak(speech);
                                        }
                                    }
                                    break;
                                ///////////////////////////////////////////
                                case "clothes.repeatdialog":
                                    TTS.speak(getCorrectContextDialogue(ProductAttributes.productMap.get
                                            ("prevDialog").toString()));
                                    break;
                                case "clothes.add":
                                    TTS.speak(speech);
                                    break;
                                case "clothes.orders":
                                    TTS.speak(speech);
                                    break;
                                case "clothes.remove":
                                    TTS.speak(speech);
                                    break;
                                case "clothes.viewlogs":
                                    TTS.speak(speech);
                                    break;
                                case "clothes.more":
                                    TTS.speak(speech);
                                    break;
                                case "clothes.filters":
                                    TTS.speak(speech);
                                    break;
                                case "clothes.start-over":
                                    Log.i(TAG, "System: Start Over");
                                    clearFilters();
                                    PostTaskListener postTaskListener_clear = init(activity);
                                    final ReaderTask readerTask_clear = new ReaderTask(activity.getApplicationContext(),
                                            postTaskListener_clear);
                                    readerTask_clear.execute(getEmptyHashMap());
                                    TTS.speak("Ok, I have cleared all filters. Please start with selecting " +
                                            "an item from Pant, Jean, Short, Shirt, Jacket, Skirt, Dress or Legging.");
                                    break;
                                case "clothes.product-number":

                                    PostTaskListener postTaskListener_prodNum = init(activity);
                                    final ReaderTask readerTask_prodNum = new ReaderTask(activity.getApplicationContext(),
                                            postTaskListener_prodNum);
                                    if (!result.getParameters().isEmpty()) {
                                        //TODO
//                                        if (result.getParameters().get("productnum").toString().contains("\""))
                                        String idVal=  result.getParameters().get("productnum").toString().replaceAll
                                                ("\"", "");
                                        if(isInteger(idVal)){
                                            Log.i(TAG, "System: Item Search : HasId");
                                            ProductAttributes.productMap.put("id", result.getParameters().get
                                                    ("productnum").toString().replaceAll("\"", ""));
                                            if (checkResultsNotZero(activity,ProductAttributes.productMap)) {
                                                speech = "Ok. Sure.";
                                                TTS.speak(speech);
                                                readerTask_prodNum.execute(ProductAttributes.productMap);
                                            }
                                        }
                                        else {
                                            Log.i(TAG, "System: Item Search : NoId");
                                            TTS.speak("Sorry, that's not a valid number. Please say" +
                                                    " \"Open product number\" followed by the product number");
                                        }

//                                        else
//                                            ProductAttributes.productMap.put("id", result.getParameters().get("productnum").toString());

                                    } else {
                                        speech = getRandomUtterance();
                                        TTS.speak(speech);
                                    }
                                    break;
                                case "clothes.startwithname":
                                    TTS.speak(speech);
                                    break;
                                default:
                                    TTS.speak(speech);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("DialogError", e.toString());
                    aiDialog.close();
                }
            }


            @Override
            public void onError(AIError error) {
                Log.e("DialogError", error.toString());
//                Toast.makeText(activity, error.toString(), Toast.LENGTH_SHORT).show();
                aiDialog.close();
            }

            @Override
            public void onCancelled() {
                Log.i("tag", "here");
            }
        });
    }
    public void startRecognition(){
        aiDialog.showAndListen();
    }

    public PostTaskListener<ArrayList<Product>> init (Activity activity){
        // ListenerTask lt = new ListenerTask();
        PostTaskListener<ArrayList<Product>> postTaskListener = ((ListenerTask) activity.getApplication
                ()).getPostTaskListener();

        return postTaskListener;
    }

    public String getRandomUtterance(){
        String [] arr = {"I couldn't understand what you just said. Please say it again?",
                "I couldn't find the answer you just said. Can you repeat it?",
                "That is not a valid option. For help, please say \"help\".", "I am lost. Can you please repeat that? If you are stuck, please say \"help\". To start over, say \"start over\""};
        Random random = new Random();

        // randomly selects an index from the arr
        int select = random.nextInt(arr.length);

        // prints out the value at the randomly selected index
        return arr[select];
    }

    public String[] getQueryItems(HashMap queryMap){
        if(queryMap!=null) {
            String queryItems = queryMap.get("query").toString().toLowerCase();
            queryItems = queryItems.replace("\"","");
            queryItems = queryItems.replace("[","");
            queryItems = queryItems.replace("]","");
            queryItems = queryItems.replace("'","");
            queryItems=AItrain(queryItems);
            String[] queryItemsList = queryItems.split(" ");

            return queryItemsList;
        }
        else return null;
    }

    public String getNextDialogue(){
        String utterance = "";

        //TODO
//        ProductAttributes.productMap.put("category", "shirt");
//        ProductAttributes.productMap.put("color", "blue");

        HashMap productMap = ProductAttributes.productMap;
        if(productMap.get("category")==null){
            utterance = findDialogue("1");
            ProductAttributes.productMap.put("prevDialog", "1");
        }
        else if(productMap.get("gender")==null){
            utterance = findDialogue("2");
            ProductAttributes.productMap.put("prevDialog", "2");
        }
        else if(productMap.get("size")==null){
            utterance = findDialogue("3");
            ProductAttributes.productMap.put("prevDialog", "3");
        }
        else if(productMap.get("color")==null){
            utterance = findDialogue("4");
            ProductAttributes.productMap.put("prevDialog", "4");
        }
        else if(productMap.get("price")==null && productMap.get("priceStart")==null){
            utterance = findDialogue("5");
            ProductAttributes.productMap.put("prevDialog", "5");
        }
        else if(productMap.get("brand")==null){
            utterance = findDialogue("6");
            ProductAttributes.productMap.put("prevDialog", "6");
        }
        else{
            utterance = findDialogue("7");
            ProductAttributes.productMap.put("prevDialog", "7");
        }
        return utterance;
    }

    public static void clearFilters(){
        ProductAttributes.productMap.clear();
        ProductAttributes.productMap.put("open_done", "0");
        prevAttrValue = "";
        flag_ground = 0;
        context=0; //TODO
    }

    public String findDialogue(String value){
        String utterance = "";
        switch (value){
            case "1":
                utterance = "What product do you want? You can choose from shirts, " +
                        "shorts, pants, jeans and other items.";
                context=1;
                break;
            case "2":
                utterance = "Ok. Who do you want to buy it for? Men or Women?";
                context=2;
                break;
            case "3":
                utterance = "Great, What size do you want it in?";
                context=3;
                break;
            case "4":
                utterance = "Next, can you tell me the color you want it in?";
                context=4;
                break;
            case "5":
                utterance = "Ok. What price range are you looking for?";
                context=5;
                break;
            case "6":
                utterance = "Ok. Do you have a brand in mind?";
                context=6;
                break;
            case "7":
                utterance = "These are the filtered items. To open a product say \"Open product\" followed by product number.";
                break;

        }
        return utterance;
    }

    public boolean checkIfItemExists(HashMap modifiedHashMap){
        if (modifiedHashMap==null){
            TTS.speak("Sorry, I couldn't find that item. Please select an item from " +
                    "Pants, Jeans, Shorts, Shirts and other items. ");
            return false;
        }
        else {
            populateModifiedHashMap(modifiedHashMap);
            return true;
        }

    }
    public void populateModifiedHashMap(HashMap modifiedHashMap){
        Iterator it = modifiedHashMap.entrySet().iterator();
        if(!it.hasNext()){
            TTS.speak(getRandomUtterance());
        }
        else {
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                ProductAttributes.productMap.put(pair.getKey(), pair.getValue());
                //it.remove(); // avoids a ConcurrentModificationException
            }
        }
    }

    public void setAttrValue(String value){
        value = value.replace("\"","");
        if (ProductAttributes.productMap.get("prevDialog") == "2") {
            ProductAttributes.productMap.put("gender", value);
        } else if (ProductAttributes.productMap.get("prevDialog") == "3") {
            ProductAttributes.productMap.put("size", value);
        } else if (ProductAttributes.productMap.get("prevDialog") == "4") {
            ProductAttributes.productMap.put("color", value);
        } else if (ProductAttributes.productMap.get("prevDialog") == "5") {
            ProductAttributes.productMap.put("priceStart", value);
            ProductAttributes.productMap.put("priceEnd", value);
        } else if (ProductAttributes.productMap.get("prevDialog") == "6") {
            ProductAttributes.productMap.put("brand", value);
        }
    }

    public void groundDialog(String value){
        flag_ground=1;
        prevAttrValue = value;
        TTS.speak("Did you say "+ value + " ?");
    }

    public int randomizeGrounding(){
        Random random = new Random();
        // randomly selects an index from the arr
        int select = random.nextInt(4);
        return select;
    }

    public void setDialogContext(int dialogContext){
        context= dialogContext;
    }

    public boolean checkValidContext(String APIAIContext){
        //1 gender
        //2 size
        //3 color
        //4 price
        //5 brand
//        boolean test = APIAIContext.toString().contains("open.prompt");
//        APIAIContext = APIAIContext.replace("\\u0000", "");
        if(APIAIContext.equals("clothes.product") && context!=1){
            return false;
        }
        if(APIAIContext.contains("clothes.gender") && context!=2){
            return false;
        }
        else if(APIAIContext.contains("clothes.size") && context!=3){
            return false;
        }
        else if(APIAIContext.contains("clothes.color") && context!=4){
            return false;
        }
        else if(APIAIContext.contains("clothes.pricevalue") && context!=5){
            return false;
        }
        else if(APIAIContext.contains("clothes.brand") && context!=6){
            return false;
        }
        return true;
    }

    public String getPriceAttr(JsonElement priceJSONElement, String param){
        if(priceJSONElement.getAsJsonArray().size()!=0){
            //JsonObject priceObj = priceJSONElement.getAsJsonObject();
            JsonArray testArray = priceJSONElement.getAsJsonArray();
            JsonObject testObj = (JsonObject) testArray.get(0);
            String test = testObj.get("amount").toString();
            return priceJSONElement.getAsString();
        }
        else return "";

    }

    public boolean isInteger(String id){
        try{
            int num = Integer.parseInt(id);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public HashMap getEmptyHashMap(){
        HashMap emptyMap = new HashMap();
        return emptyMap;
    }

    public boolean checkResultsNotZero(Activity activity, HashMap productMap){
        if(DBHelper.getInstance(activity).GetResultSize(productMap)>0){
            return true;
        }
        if (context!=1)
            TTS.speak("Sorry, there are no filtered items. Please start over by saying \"start over\"");
        else if(context==1){
            if(flag_prodsearch==0)
                TTS.speak("Sorry, there are no filtered items. Please start over by saying \"start over\"");
            else if (flag_prodsearch==1)
                TTS.speak("Sorry that's not a valid item. You can choose from Shirts, Pants, Shorts and other items.");
        }
        //else
        return false;
    }

    public void toggleProdSearchFlag(){
        if (flag_prodsearch==1)
            flag_prodsearch=0;
        else if (flag_prodsearch==0)
            flag_prodsearch=1;
    }

    public String AItrain(String userQuery){
        if(userQuery.toLowerCase().contains("t-shirt")){
            userQuery = userQuery.toLowerCase().replace("t-shirt","shirt");
        }
        if(userQuery.toLowerCase().contains("tshirt")){
            userQuery = userQuery.toLowerCase().replace("tshirt","shirt");
        }
        if(userQuery.toLowerCase().contains("mens")){
            userQuery = userQuery.toLowerCase().replace("mens","men");
        }
        if(userQuery.toLowerCase().contains("shark")){
            userQuery = userQuery.toLowerCase().replace("shark","shirt");
        }
         if(userQuery.toLowerCase().contains("shot")){
             userQuery = userQuery.toLowerCase().replace("shot","short");
        }
         if(userQuery.toLowerCase().contains("sharp")){
             userQuery = userQuery.toLowerCase().replace("sharp","shirts");
        }
        if(userQuery.toLowerCase().contains("jaket")){
            userQuery = userQuery.toLowerCase().replace("jaket","jacket");
        }
        if(userQuery.toLowerCase().contains("record")){
            userQuery = userQuery.toLowerCase().replace("record","jacket");
        }
        if(userQuery.toLowerCase().contains("red")){
            userQuery = userQuery.toLowerCase().replace("red","red ");
        }
        if(userQuery.toLowerCase().contains("blue")){
            userQuery = userQuery.toLowerCase().replace("blue","blue ");
        }
        if(userQuery.toLowerCase().contains("bend")){
            userQuery = userQuery.toLowerCase().replace("bend","pant");
        }
        return userQuery;
    }

    public String getCorrectContextDialogue(String value){
        String invalidPrompt = "That is not a valid option. ";
        String utterance = "";
        switch (value){
            case "1":
                utterance = "What product do you want? You can choose from shirts, " +
                        "shorts, pants, jeans and other items.";
                context=1;
                break;
            case "2":
                utterance = "Who do you want to buy it for? Men or Women?";
                context=2;
                break;
            case "3":
                utterance = "What size do you want it in?";
                context=3;
                break;
            case "4":
                utterance = "Can you tell me the color you want it in?";
                context=4;
                break;
            case "5":
                utterance = "What price range are you looking for?";
                context=5;
                break;
            case "6":
                utterance = "Do you have a brand in mind?";
                context=6;
                break;
            case "7":
                utterance = "These are the filtered items. To open a product say \"Open product\" followed by product number.";
                break;

        }
        utterance = invalidPrompt+utterance;
        return utterance;
    }
}

//try {
//        JSONObject testList = (JSONObject) priceJSONArray.get(0);
//        return testList.get(param).toString();
//
////            if(){
////                JsonObject priceObj = priceJSONArray.getAsJsonObject();
////                return priceObj.get(param).getAsString();
////                }
////            else return "";
//        } catch (JSONException e) {
//        e.printStackTrace();
//        return "";
//        }
