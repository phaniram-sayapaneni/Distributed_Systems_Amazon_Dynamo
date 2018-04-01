package edu.buffalo.cse.cse486586.simpledynamo;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.List;

import static android.content.ContentValues.TAG;


public class SimpleDynamoProvider extends ContentProvider {



    private final Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledynamo.provider");
    public static Context context;
    static String portnum = "";
    static final int SERVER_PORT = 10000;
    static List<String> nodes = new ArrayList<String>();
    static String [] succs = new String[2];
    static String [] actual_nodes = new String[5];
    String [] misses = new String[5];


    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }



    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Context context = getContext();
        Log.d("inside_delete", " deleting this file" + selection);
        File fin = new File(context.getFilesDir(), (selection + ".txt"));
        fin.delete();
        String del_park = find_key_parking(selection);
        String [] su2 = return_succs(del_park);
        String del = "delete" + " " + selection;

        String [] dell = new String [3];
        try {

            dell[0] = new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, del, del_park).get();
        } catch( Exception ex) { dell[0] = "deleted";}

        try {
            dell[1] = new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, del, su2[0]).get();
        } catch( Exception ex) { dell[1] = "deleted";}

            try {
            dell[2] = new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, del, su2[1]).get();
            } catch( Exception ex) { dell[2] = "deleted";}
         if(dell[0].equals("deleted")&& dell[1].equals("deleted") && dell[2].equals("deleted")) return 0;

        else {
                    while(1==1)
                    {
                        //Thread.sleep(100);
                        if(dell[0].equals("deleted")&& dell[1].equals("deleted") && dell[2].equals("deleted") ) { break; }
                    return 0;}
         }
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub

        generate_succs();
        Log.v("insert", values.toString());
        String key = (String) values.get("key");
        String value = (String) values.get("value");
        String pred = find_prev_node(portnum);
        try {
            String haskey = genHash(key);
            String pred_2 = Integer.toString( Integer.parseInt(pred)/2);
            String predhash = genHash(pred_2);
            String portnum_2 = Integer.toString( Integer.parseInt(portnum)/2);
            String myhash = genHash(portnum_2);
            String park_node = find_key_parking(key);

            Log.d("park_node", "park_node :" + park_node +  " " + "key :" + key);

            String filename = key + ".txt";
            String string = value;
            if((haskey.compareTo(myhash)<=0 && haskey.compareTo(predhash)>0)  ||  park_node.equals(portnum))
            {
                String for_park_node =  "blindinsert" + " " + "key" + " "+ key + " "+ string;
                String []   replicas = return_succs(park_node);
                String inserted0= "";
                String inserted1= "";


                FileOutputStream outputStream;
                try {
                    // outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                    //OutputStream fo = new FileOutputStream(filename);
                    if (context == null)
                        Log.e("context", "null");
                    Log.e("path", context.getFilesDir().getAbsolutePath());
                    File file = new File(context.getFilesDir(), filename);
                    FileWriter fw =  new FileWriter(file);
                    Log.d("filewrite" , "key:" + key + "value:" + string );
                    fw.write(string);
                    fw.close();
                    Log.d("insert", "parknode:" + park_node + "its succs:" + replicas[0] + " ," + replicas[1]);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                     inserted0 = new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, for_park_node, replicas[0]).get();
                    if(inserted0.equals("inserted")) {Log.d("inserted", "replica[0] got inserted:" + replicas[0] + "for parknode is:" + for_park_node);}

                    else
                    {

                        Log.d("failure", "failure detected for node:" + replicas[1]);
                        String miss = key + " " + string + " ";
                        if(replicas[0].equals("11108") ) {misses[0] = misses[0]+ miss; File file = new File(context.getFilesDir(), "misses0" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write(misses[0]);
                            fw.close();}
                        else if(replicas[0].equals("11112") ) {misses[1] = misses[1]+ miss; File file = new File(context.getFilesDir(), "misses1" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write(misses[1]);
                            fw.close();}
                        else if(replicas[0].equals("11116") ) {misses[2] = misses[2]+ miss; File file = new File(context.getFilesDir(), "misses2" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write(misses[2]);
                            fw.close();}
                        else if(replicas[0].equals("11120") ) {misses[3] = misses[3]+ miss; File file = new File(context.getFilesDir(), "misses3" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write(misses[3]);
                            fw.close();}
                        else if(replicas[0].equals("11124") ) {misses[4] = misses[4]+ miss; File file = new File(context.getFilesDir(), "misses4" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write(misses[4]);
                            fw.close();}

                    }

                } catch ( Exception ex11)
                {
                    Log.d("failure", "failure detected for node:" + replicas[1] + ex11);
                    String miss = key + " " + string + " ";
                    if(replicas[0].equals("11108") ) {misses[0] = misses[0]+ miss; File file = new File(context.getFilesDir(), "misses0" + ".txt");
                        FileWriter fw =  new FileWriter(file);
                        Log.d("filewrite" , "key:" + key + "value:" + string );
                        fw.write(misses[0]);
                        fw.close();}
                    else if(replicas[0].equals("11112") ) {misses[1] = misses[1]+ miss; File file = new File(context.getFilesDir(), "misses1" + ".txt");
                        FileWriter fw =  new FileWriter(file);
                        Log.d("filewrite" , "key:" + key + "value:" + string );
                        fw.write(misses[1]);
                        fw.close();}
                    else if(replicas[0].equals("11116") ) {misses[2] = misses[2]+ miss; File file = new File(context.getFilesDir(), "misses2" + ".txt");
                        FileWriter fw =  new FileWriter(file);
                        Log.d("filewrite" , "key:" + key + "value:" + string );
                        fw.write(misses[2]);
                        fw.close();}
                    else if(replicas[0].equals("11120") ) {misses[3] = misses[3]+ miss; File file = new File(context.getFilesDir(), "misses3" + ".txt");
                        FileWriter fw =  new FileWriter(file);
                        Log.d("filewrite" , "key:" + key + "value:" + string );
                        fw.write(misses[3]);
                        fw.close();}
                    else if(replicas[0].equals("11124") ) {misses[4] = misses[4]+ miss; File file = new File(context.getFilesDir(), "misses4" + ".txt");
                        FileWriter fw =  new FileWriter(file);
                        Log.d("filewrite" , "key:" + key + "value:" + string );
                        fw.write(misses[4]);
                        fw.close();}
                }

                try {
                     inserted1 = new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, for_park_node, replicas[1]).get();
                    if(inserted1.equals("inserted")) {Log.d("inserted", "replica[1] got inserted:" +  replicas[1] + "for parknode is:" + for_park_node);}
                    else
                    {
                            Log.d("failure", "failure detected for node:" + replicas[1]);
                            String miss = key + " " + string + " ";
                            if(replicas[1].equals("11108") ) {misses[0] = misses[0]+ miss;
                                misses[0] = misses[0]+ miss; File file = new File(context.getFilesDir(), "misses0" + ".txt");
                                FileWriter fw =  new FileWriter(file);
                                Log.d("filewrite" , "key:" + key + "value:" + string );
                                fw.write(misses[0]);
                                fw.close();
                            }
                            else if(replicas[1].equals("11112") ) {misses[1] = misses[1]+ miss;
                                misses[1] = misses[2]+ miss; File file = new File(context.getFilesDir(), "misses1" + ".txt");
                                FileWriter fw =  new FileWriter(file);
                                Log.d("filewrite" , "key:" + key + "value:" + string );
                                fw.write(misses[1]);
                                fw.close();}
                            else if(replicas[1].equals("11116") ) {misses[2] = misses[2]+ miss;
                                misses[2] = misses[2]+ miss; File file = new File(context.getFilesDir(), "misses2" + ".txt");
                                FileWriter fw =  new FileWriter(file);
                                Log.d("filewrite" , "key:" + key + "value:" + string );
                                fw.write(misses[2]);
                                fw.close();}
                            else if(replicas[1].equals("11120") ) {misses[3] = misses[3]+ miss;
                                misses[3] = misses[3]+ miss; File file = new File(context.getFilesDir(), "misses3" + ".txt");
                                FileWriter fw =  new FileWriter(file);
                                Log.d("filewrite" , "key:" + key + "value:" + string );
                                fw.write(misses[3]);
                                fw.close();}
                            else if(replicas[1].equals("11124") ) {misses[4] = misses[4]+ miss;
                                misses[4] = misses[4]+ miss; File file = new File(context.getFilesDir(), "misses4" + ".txt");
                                FileWriter fw =  new FileWriter(file);
                                Log.d("filewrite" , "key:" + key + "value:" + string );
                                fw.write(misses[4]);
                                fw.close();}
                    }


                } catch ( Exception ex2)
                {
                    Log.d("failure", "failure detected for node:" + replicas[1]);
                    String miss = key + " " + string + " ";
                    if(replicas[1].equals("11108") ) {misses[0] = misses[0]+ miss;
                        misses[0] = misses[0]+ miss; File file = new File(context.getFilesDir(), "misses0" + ".txt");
                        FileWriter fw =  new FileWriter(file);
                        Log.d("filewrite" , "key:" + key + "value:" + string );
                        fw.write(misses[0]);
                        fw.close();
                    }
                    else if(replicas[1].equals("11112") ) {misses[1] = misses[1]+ miss;
                        misses[1] = misses[1]+ miss; File file = new File(context.getFilesDir(), "misses1" + ".txt");
                        FileWriter fw =  new FileWriter(file);
                        Log.d("filewrite" , "key:" + key + "value:" + string );
                        fw.write(misses[1]);
                        fw.close();}
                    else if(replicas[1].equals("11116") ) {misses[2] = misses[2]+ miss;
                        misses[2] = misses[2]+ miss; File file = new File(context.getFilesDir(), "misses2" + ".txt");
                        FileWriter fw =  new FileWriter(file);
                        Log.d("filewrite" , "key:" + key + "value:" + string );
                        fw.write(misses[2]);
                        fw.close();}
                    else if(replicas[1].equals("11120") ) {misses[3] = misses[3]+ miss;
                        misses[3] = misses[3]+ miss; File file = new File(context.getFilesDir(), "misses3" + ".txt");
                        FileWriter fw =  new FileWriter(file);
                        Log.d("filewrite" , "key:" + key + "value:" + string );
                        fw.write(misses[3]);
                        fw.close();}
                    else if(replicas[1].equals("11124") ) {misses[4] = misses[4]+ miss;
                        misses[4] = misses[4]+ miss; File file = new File(context.getFilesDir(), "misses4" + ".txt");
                        FileWriter fw =  new FileWriter(file);
                        Log.d("filewrite" , "key:" + key + "value:" + string );
                        fw.write(misses[4]);
                        fw.close();}
                }
                if(inserted0.equals("inserted") && inserted1.equals("inserted"))  return uri;
                return uri;
                ////write ends here

            }

            else
            {
                String for_park_node =  "blindinsert" + " " + "key" + " "+ key + " "+ string;
                ////// Log.d("insert_forwarding_blindly", "yo, im sending:" + key + "value:" + value + "to :" + succ);
                new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, for_park_node, park_node);


                /// adding for replicas too
                String []   replicas = return_succs(park_node);
                 Log.d("insert", "parknode:" + park_node + "its succs:" + replicas[0] + " ," + replicas[1]);

                 String inserted0 = ""; String inserted1 = ""; String inserted2 = "";

                try {
                    inserted0 = new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, for_park_node, park_node).get();
                    if(inserted0.equals("inserted")) {Log.d("inserted", "parknode got inserted:" + park_node + "for parknode is:" + for_park_node);}

                    else {
                        Log.d("failure", "failure detected for node:" + park_node);
                        String miss = key + " " + string + " ";
                        if(park_node.equals("11108") ) {misses[0] = misses[0]+ miss;
                            misses[0] = misses[0]+ miss; File file = new File(context.getFilesDir(), "misses0" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write(misses[0]);
                            fw.close();}
                        else if(park_node.equals("11112") ) {misses[1] = misses[1]+ miss;
                            File file = new File(context.getFilesDir(), "misses1" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write(misses[1]);
                            fw.close();}
                        else if(park_node.equals("11116") ) {misses[2] = misses[2]+ miss;
                            File file = new File(context.getFilesDir(), "misses2" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write(misses[2]);
                            fw.close();}
                        else if(park_node.equals("11120") ) {misses[3] = misses[3]+ miss;
                            misses[3] = misses[3]+ miss; File file = new File(context.getFilesDir(), "misses3" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write(misses[3]);
                            fw.close();}
                        else if(park_node.equals("11124") ) {misses[4] = misses[4]+ miss;
                            File file = new File(context.getFilesDir(), "misses4" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write(misses[4]);
                            fw.close();}
                    }

                } catch ( Exception ex0)
                {
                    Log.d("failure", "failure detected for node:" + park_node);
                    String miss = key + " " + string + " ";
                    if(park_node.equals("11108") ) {misses[0] = misses[0]+ miss;}
                    else if(park_node.equals("11112") ) {misses[1] = misses[1]+ miss;}
                    else if(park_node.equals("11116") ) {misses[2] = misses[2]+ miss;}
                    else if(park_node.equals("11120") ) {misses[3] = misses[3]+ miss;}
                    else if(park_node.equals("11124") ) {misses[4] = misses[4]+ miss;}


                }

                try {
                    inserted1 = new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, for_park_node, replicas[0]).get();
                    if(inserted1.equals("inserted")) {Log.d("inserted", "replica[0] got inserted:" + replicas[0] + "for parknode is:" + for_park_node);}
                    else{
                        Log.d("failure", "failure detected for node:" + park_node);
                        String miss = key + " " + string + " ";
                        if(replicas[0].equals("11108") ) {misses[0] = misses[0]+ miss;
                            misses[0] = misses[0]+ miss; File file = new File(context.getFilesDir(), "misses0" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write(misses[0]);
                            fw.close();}
                        else if(replicas[0].equals("11112") ) {misses[1] = misses[1]+ miss;
                            File file = new File(context.getFilesDir(), "misses1" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write(misses[1]);
                            fw.close();}
                        else if(replicas[0].equals("11116") ) {misses[2] = misses[2]+ miss;
                            File file = new File(context.getFilesDir(), "misses2" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write(misses[2]);
                            fw.close();}
                        else if(replicas[0].equals("11120") ) {misses[3] = misses[3]+ miss;
                            misses[3] = misses[3]+ miss; File file = new File(context.getFilesDir(), "misses3" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write(misses[3]);
                            fw.close();}
                        else if(replicas[0].equals("11124") ) {misses[4] = misses[4]+ miss;
                            File file = new File(context.getFilesDir(), "misses4" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write(misses[4]);
                            fw.close();}
                    }

                } catch ( Exception ex1)
                {
                    Log.d("failure", "failure detected for node:" + replicas[0]);
                    String miss = key + " " + string + " ";
                    if(replicas[0].equals("11108") ) {misses[0] = misses[0]+ miss;}
                    else if(replicas[0].equals("11112") ) {misses[1] = misses[1]+ miss;}
                    else if(replicas[0].equals("11116") ) {misses[2] = misses[2]+ miss;}
                    else if(replicas[0].equals("11120") ) {misses[3] = misses[3]+ miss;}
                    else if(replicas[0].equals("11124") ) {misses[4] = misses[4]+ miss;}


                }

                try {
                    inserted2 = new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, for_park_node, replicas[1]).get();
                    if(inserted2.equals("inserted")) {Log.d("inserted", "replica[1] got inserted:" +  replicas[1] + "for parknode is:" + for_park_node);}
                    else
                    {
                        Log.d("failure", "failure detected for node:" + park_node);
                        String miss = key + " " + string + " ";
                        if(replicas[1].equals("11108") ) {misses[0] = misses[0]+ miss;
                            misses[0] = misses[0]+ miss; File file = new File(context.getFilesDir(), "misses0" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            fw.write(misses[0]);
                            fw.close();}
                        else if(replicas[1].equals("11112") ) {misses[1] = misses[1]+ miss;
                            File file = new File(context.getFilesDir(), "misses1" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write(misses[1]);
                            fw.close();}
                        else if(replicas[1].equals("11116") ) {misses[2] = misses[2]+ miss;
                            File file = new File(context.getFilesDir(), "misses2" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write(misses[2]);
                            fw.close();}
                        else if(replicas[1].equals("11120") ) {misses[3] = misses[3]+ miss;
                            misses[3] = misses[3]+ miss; File file = new File(context.getFilesDir(), "misses3" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write(misses[3]);
                            fw.close();}
                        else if(replicas[1].equals("11124") ) {misses[4] = misses[4]+ miss;
                            File file = new File(context.getFilesDir(), "misses4" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write(misses[4]);
                            fw.close();}
                    }
                } catch ( Exception ex2)
                {
                    Log.d("failure", "failure detected for node:" + replicas[1]);
                    String miss = key + " " + string + " ";
                    if(replicas[1].equals("11108") ) {misses[0] = misses[0]+ miss;}
                    else if(replicas[1].equals("11112") ) {misses[1] = misses[1]+ miss;}
                    else if(replicas[1].equals("11116") ) {misses[2] = misses[2]+ miss;}
                    else if(replicas[1].equals("11120") ) {misses[3] = misses[3]+ miss;}
                    else if(replicas[1].equals("11124") ) {misses[4] = misses[4]+ miss;}
                }
                if(inserted0.equals("inserted") && inserted1.equals("inserted") && inserted2.equals("inserted"))  return uri;
            }
        }
        catch(Exception exp ) {Log.d("key_forwarding", "error in sending key to the right location");}
        return null;
    }

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub

        context = getContext();
        TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        portnum = myPort;

        //////
        actual_nodes[0]= "11124"; actual_nodes[1]= "11112"; actual_nodes[2]= "11108"; actual_nodes[3]= "11116"; actual_nodes[4]= "11120";
        //////////
        misses[0] =""; misses[1] = ""; misses[2] = ""; misses[3] = ""; misses[4]= "";

        try {
            Context context = getContext();
            File fin = new File(context.getFilesDir().getPath());
            FileReader fr = new FileReader(fin + "/" + "misses0" + ".txt");
            Log.e("file", "inside file: path: " + fin + "/" + "misses0");
            BufferedReader br = new BufferedReader(fr);
            misses[0] = br.readLine();
            br.close();
        } catch (Exception e) {
            Log.e("readerror11", "unable to read the file");
        }

        try {
        Context context = getContext();
        File fin1 = new File(context.getFilesDir().getPath());
        FileReader fr1 = new FileReader(fin1 + "/" + "misses1" + ".txt");
        Log.e("file", "inside file: path: " + fin1+ "/" + "misses1");
        BufferedReader br1 = new BufferedReader(fr1);
        misses[1] = br1.readLine();
        br1.close();
    } catch (Exception e2) {
        Log.e("readerror11", "unable to read the file");
    }

        try {
            Context context = getContext();
            File fin1 = new File(context.getFilesDir().getPath());
            FileReader fr1 = new FileReader(fin1 + "/" + "misses2" + ".txt");
            Log.e("file", "inside file: path: " + fin1+ "/" + "misses2");
            BufferedReader br1 = new BufferedReader(fr1);
            misses[2] = br1.readLine();
            br1.close();
        } catch (Exception e2) {
            Log.e("readerror11", "unable to read the file");
        }
        try {
            Context context = getContext();
            File fin1 = new File(context.getFilesDir().getPath());
            FileReader fr1 = new FileReader(fin1 + "/" + "misses3" + ".txt");
            Log.e("file", "inside file: path: " + fin1+ "/" + "misses3");
            BufferedReader br1 = new BufferedReader(fr1);
            misses[3] = br1.readLine();
            br1.close();
        } catch (Exception e2) {
            Log.e("readerror11", "unable to read the file");
        }
        //////////////////
        try {
            Context context = getContext();
            File fin1 = new File(context.getFilesDir().getPath());
            FileReader fr1 = new FileReader(fin1 + "/" + "misses4" + ".txt");
            Log.e("file", "inside file: path: " + fin1+ "/" + "misses4");
            BufferedReader br1 = new BufferedReader(fr1);
            misses[4] = br1.readLine();
            br1.close();
        } catch (Exception e2) {
            Log.e("readerror11", "unable to read the file");
        }
        try {
            Log.d("test", "so the problem is with server task1");
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            Log.d("test", "so the problem is with server task2");
            new SimpleDynamoProvider.ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
            Log.d("test", "so the problem is with server task3");



            Log.d("recovery","portnum: " + portnum);
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");}
        String [] all_f = new String[5];
        String didimiss = "dmiss" + " "+ portnum;

        Log.d("recovery", "trying recovery");






            try {
                all_f[0] = new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, didimiss, "11108").get();

                //Thread.sleep(100);
                Log.d("recovery", "here is all_f[0]:" + all_f[0]);

            }
            catch (Exception ee0)
            {}

            try{
            all_f[1] = new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, didimiss, "11112").get();
                //Thread.sleep(100);
                Log.d("recovery", "here is all_f[1]:" + all_f[1]);
            }
            catch (Exception ee1)
            {}

            try{
            all_f[2] = new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, didimiss, "11116").get();
                //Thread.sleep(100);
                Log.d("recovery", "here is all_f[2]:" + all_f[2]);
            }
            catch (Exception ee2)
            {}
            try{
            all_f[3] = new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, didimiss, "11120").get();
                //Thread.sleep(100);
                Log.d("recovery", "here is all_f[3]:" + all_f[3]);
            }
            catch (Exception ee2)
            {}

            try {
                all_f[4] = new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, didimiss, "11124").get();
                //Thread.sleep(100);
                Log.d("recovery", "here is all_f[4]:" + all_f[4]);
            }
            catch (Exception ee3)
            {}

           try{



            for (int i = 0; i < 5; i++) {
                if ((!all_f[i].isEmpty()) && (!all_f[i].equals("")) && all_f[i].length() > 2) {
                    String[] allmsgs = all_f[i].split(" ");
                    //Log.d("starquery", "here is the msg from s[i]" + s[i]);
                    for (int j = 0; j < allmsgs.length; j += 2) {


                        String[] rw = new String[2];
                        rw[0] = allmsgs[j];//selctions
                        rw[1] = allmsgs[j + 1];
                        Log.d("starquery", "here is key:" + allmsgs[j] + "here is value:" + allmsgs[j + 1]);
                        if(rw[0].substring(0,4).equals("null")) rw[0] = rw[0].substring(4);
                            String filename = rw[0] + ".txt";
                            String string = rw[1];
                            File file = new File(context.getFilesDir(), filename);
                            FileWriter fw = new FileWriter(file);
                            Log.d("filewrite", "key:" + filename + "value:" + string);
                            fw.write(string);
                            fw.close();
                        //}



                    }
                    //return mc;
                }
            }


           }
        catch(Exception excc) {}
        return false;
    }

    public String find_key_parking(String key)
    {   //Collections.sort(nodes, new seqcomparator());
            int size = actual_nodes.length;

        try {



            for (int i = 0; i < size; i++)
                {
                    String curr_node = actual_nodes[i];
                    Log.d("find_key_parking", "parking key is:" + key);
                    Log.d("find_key_parking", "curr_node:" + curr_node);
                    String haskey = genHash(key);
                    //String pred = find_preds();//// here is the problem
                    String pred =  find_prev_node(curr_node);
                    Log.d("find_key_parking", "prednode:" + pred);
                    String pred_2 = Integer.toString( Integer.parseInt(pred)/2);
                    String curr_2 = Integer.toString( Integer.parseInt(curr_node)/2);
                    String predhash = genHash(pred_2);
                    String myhash = genHash(curr_2);
                    if(curr_node.equals("11124") && haskey.compareTo(myhash)>=0 &&  haskey.compareTo(predhash)>0) {Log.d("find_key_parking", "return value for else:" + curr_node); return curr_node;}
                    else if(haskey.compareTo(myhash)<=0 && haskey.compareTo(predhash)>0) { Log.d("find_key_parking", "return value:" + curr_node); return curr_node;}
                    //else if(i ==0 && haskey.compareTo(myhash)>=0 &&  haskey.compareTo(predhash)>0) {Log.d("find_key_parking", "return value for else:" + curr_node); return curr_node;}
                }
            return actual_nodes[0];


        } catch(Exception exp)
        {Log.d("problem_finding" ,"key parking");}

             return "";

    }

    public String find_prev_node(String curr_node)
    {
        if(curr_node.equals("11124")) return "11120";
        if(curr_node.equals("11112")) return "11124";
        if(curr_node.equals("11108")) return "11112";
        if(curr_node.equals("11116")) return "11108";
        if(curr_node.equals("11120")) return "11116";
        return "";
    }

    public String [] two_level_find_prev_node(String curr_node)
    {
        String ans [] = new String[2];
        if(curr_node.equals("11124")) { ans[0] = "11120" ; ans[1] = "11116";  return ans;}
        if(curr_node.equals("11112")) { ans[0] = "11124" ; ans[1] = "11120";  return ans;}
        if(curr_node.equals("11108")) { ans[0] = "11112" ; ans[1] = "11124";  return ans;}
        if(curr_node.equals("11116")) { ans[0] = "11108" ; ans[1] = "11112";  return ans;}
        if(curr_node.equals("11120")) { ans[0] = "11116" ; ans[1] = "11108";  return ans;}
        return ans;
    }

    public String find_preds()
    {   String ans= "";

            for (int i = 0; i < 5; i++) {
            String nd = nodes.get(i);
            if (nd.equals(portnum))
            {
                if(i == 0) return nodes.get(nodes.size()-1);
                else return nodes.get(i-1);
             }
            }
        return ans;
    }
    /// for generating client tasks
    public void generate_clienttask(String msg)
    {
        String [] msgr = msg.split(" ");
        if((msgr.length  ==  4) && msgr[0].equals("replica_forwarding"))
        {

            generate_succs();
            new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg,  succs[0]);
            new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg,  succs[1]);
            // doubt here will change in future if required
        }
    }

    public String [] return_succs(String portnum2) {
        String [] succs2 = new String[2];
        if (portnum2.equals("11108")) {
            succs2[0] = "11116";
            succs2[1] = "11120";
        } else if (portnum2.equals("11116")) {
            succs2[0] = "11120";
            succs2[1] = "11124";
        } else if (portnum2.equals("11120")) {
            succs2[0] = "11124";
            succs2[1] = "11112";
        } else if (portnum2.equals("11124")) {
            succs2[0] = "11112";
            succs2[1] = "11108";
        } else if (portnum2.equals("11112")) {
            succs2[0] = "11108";
            succs2[1] = "11116";
        }

        return succs2;

    }


/// generate succs
    public void generate_succs()
     {

         if(portnum.equals("11108"))  {succs[0] = "11116"; succs[1] = "11120";}
         else if(portnum.equals("11116"))  {succs[0] = "11120"; succs[1] = "11124";}
         else if(portnum.equals("11120"))  {succs[0] = "11124"; succs[1] = "11112";}
         else if(portnum.equals("11124"))  {succs[0] = "11112"; succs[1] = "11108";}
         else if(portnum.equals("11112"))  {succs[0] = "11108"; succs[1] = "11116";}

     }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        // TODO Auto-generated method stub



        if (!selection.equals("*") && !selection.equals("@"))
        try {
            // it will find key parking
            String parked_node = find_key_parking(selection);
            if(parked_node.equals(portnum))
            {
                Log.d("non_special1", " regular query" + "key:" +  selection + "parkednode" + parked_node);
                String valuenow = "";
                try {
                    Context context = getContext();
                    File fin = new File(context.getFilesDir().getPath());
                    FileReader fr = new FileReader(fin + "/" + selection + ".txt");
                    Log.e("file", "inside file: path: " + fin + "/" + selection);
                    BufferedReader br = new BufferedReader(fr);
                    valuenow = br.readLine();
                    br.close();
                } catch (Exception e) {
                    Log.e("readerror11", "unable to read the file");
                }
                String[] s = new String[2];
                s[0] = "key";
                s[1] = "value";
                MatrixCursor mc = new MatrixCursor(s);
                String[] rw = new String[2];
                rw[0] = selection;
                Log.d("query_regular11", "so it is querying selesction" + selection);
                rw[1] = valuenow;
                Log.d("query_regular11", "so it is querying value" + valuenow);
                mc.addRow(rw);
                return mc;

            }
            /// if parked some where else


            else
            {
                //String ask_parkednode = "query" + " " + selection;
                Log.d("non_special2", " regular query" + "key:" +  selection + "parkednode" + parked_node);

                String ask_others = "query" + " " + selection;
                String[] s = new String[5];// since you might want to take output from 3 nodes

                String[] sc = new String[2];
                sc[0] = "key";
                sc[1] = "value";
                MatrixCursor mc = new MatrixCursor(sc);
                String [] replicas = return_succs(parked_node);
                List<String> returns3 = new ArrayList<String>();

                try {
                    s[0] = new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ask_others, parked_node).get();
                    s[1] = new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ask_others, replicas[0]).get();
                    s[2] = new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ask_others, replicas[1]).get();
                    returns3.add(s[0]); returns3.add(s[1]); returns3.add(s[2]);

                    int f1 =  Collections.frequency(returns3, s[0]); int f2 =  Collections.frequency(returns3, s[1]); int f3 =  Collections.frequency(returns3, s[2]);
                    Log.d("query","for key:" + selection +  "f1:" +f1 + "s[0]" + s[0] + "f2:" + f2 + "s[1]"  + s[1] +  "f3" + f3 + "s[2]" + s[2]);
                    Log.d("query" , "for key:" +  selection + "info recived from server is: s[0]"  + s[0]);
                    Log.d("query" , "for key:" +  selection + "info recived from server is: s[1]"  + s[1]);
                    Log.d("query" , "for key:" +  selection + "info recived from server is: s[2]"  + s[2]);

                    if ((!s[0].isEmpty()) && (!s[0].equals("")) && (!s[1].isEmpty()) && (!s[1].equals("")) && (!s[2].isEmpty()) && (!s[2].equals("")))    //if ((!s[0].isEmpty()) && (!s[0].equals("")) && s[0].length() > 1)
                    {   String[] rw = new String[2];
                        rw[0] = selection;
                        //rw[1] = s[0];
                        if(f1 >1) {rw[1] = s[0]; mc.addRow(rw); Log.d("nonstar query", "selection:" + selection +  "value" +   s[0] ); return mc; }
                        else if(f2 >1 && f3<4) { rw[1] = s[1]; mc.addRow(rw); Log.d("nonstar query", "selection:" + selection +  "value" +   s[1] );return mc;}
                        else  { rw[1] = s[2]; mc.addRow(rw);Log.d("nonstar query", "selection:" + selection +  "value" +   s[2] ); return mc;}
                    }

                    else
                    {
                        Log.d("nonstar query_else", "selection:" + selection +  "value" +   s[0] );
                        String[] rw = new String[2];
                        rw[0] = selection;
                        /// was initially s[0]
                        if(!s[0].isEmpty() && !s[0].equals("")) rw[1] = s[0];
                        else if(!s[1].isEmpty() && !s[1].equals("")) rw[1] = s[1];
                        else rw[1] = s[2];
                        /// was initially s[0]
                        mc.addRow(rw);
                        return mc;
                    }

                } catch (Exception ex1) {
                    Log.d("ex1", "exception");
                }
            }

        } catch (Exception eeec) {
        }

        else {


            if (selection.equals("@")) {

                String test_s = "";
                String test_v = "";
                Log.d("inside@", "reglar now what");
                String valuenow = "";
                String[] s = new String[2];
                s[0] = "key";
                s[1] = "value";
                MatrixCursor mc = new MatrixCursor(s);
                Log.d("inside@", " regular now what1");
                try {
                    Context context = getContext();
                    Log.d("inside@", " regular now what2");
                    File fin = new File(context.getFilesDir().getPath());
                    Log.d("inside@", " regular now what3");
                    File[] listoffiles = fin.listFiles();
                    Log.d("inside@", " regular now what3");
                    for (int i = 0; i < listoffiles.length; i++) {
                        //listoffiles[i].getName()
                        String eachfile = listoffiles[i].getName();
                        if(eachfile.equals("misses0.txt") || eachfile.equals("misses1.txt") || eachfile.equals("misses2.txt") || eachfile.equals("misses3.txt") || eachfile.equals("misses4.txt")) continue;
                        Log.d("inside@", " regular now what4");
                        FileReader fr = new FileReader(fin + "/" + eachfile); /// removed + .txt here
                        Log.e("file", "inside file: path: " + fin + "/" + eachfile);
                        BufferedReader br = new BufferedReader(fr);
                        valuenow = br.readLine();
                        br.close();
                        Log.d("inside@", "now what5");
                        String[] rw = new String[2];
                        eachfile = eachfile.substring(0, eachfile.lastIndexOf('.'));
                        rw[0] = eachfile;
                        test_s = test_s + eachfile + " ";
                        Log.d("@_each", "regular test_s" + test_s);
                        rw[1] = valuenow;
                        test_v = test_v + valuenow + " ";
                        Log.d("@_each", "regular test_v" + test_v);
                        mc.addRow(rw);

                    }
                    //Log.d("@", "all selections:" + test_s + "all values:" + test_v);
                    return mc;
                } catch (Exception e) {
                    Log.e("readerror", "unable to read the file");
                }

            } else if (selection.equals("*")) {
                String ask_others = "starquery";

                String[] s = new String[5];
                try {
                    s[0] = new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ask_others, "11108").get();
                    Log.d("starquery", "here is s[0]" + s[0]);

                } catch (Exception ex1) {
                    Log.d("ex1", "exception");
                }
                try {
                    s[1] = new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ask_others, "11112").get();
                    Log.d("starquery", "here is s[1]" + s[1]);
                } catch (Exception ex1) {
                    Log.d("ex2", "exception");
                }
                try {
                    s[2] = new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ask_others, "11116").get();
                    Log.d("starquery", "here is s[2]" + s[2]);
                } catch (Exception ex1) {
                    Log.d("ex3", "exception");
                }
                try {
                    s[3] = new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ask_others, "11120").get();
                    Log.d("starquery", "here is s[3]" + s[3]);
                } catch (Exception ex1) {
                    Log.d("ex4", "exception");
                }
                try {
                    s[4] = new SimpleDynamoProvider.ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ask_others, "11124").get();
                    Log.d("starquery", "here is s[4]" + s[4]);
                } catch (Exception ex1) {
                    Log.d("ex5", "exception");
                }


                String[] sc = new String[2];
                sc[0] = "key";
                sc[1] = "value";
                MatrixCursor mc = new MatrixCursor(sc);
                for (int i = 0; i < 5; i++) {
                    if ((!s[i].isEmpty()) && (!s[i].equals("")) && s[i].length() > 2) {
                        String[] allmsgs = s[i].split(" ");
                        Log.d("starquery", "here is the msg from s[i]" + s[i]);
                        for (int j = 0; j < allmsgs.length; j += 2) {


                            String[] rw = new String[2];
                            rw[0] = allmsgs[j];//selctions
                            rw[1] = allmsgs[j + 1];
                            mc.addRow(rw);
                            //j = j+1;
                        }
                    }
                }
                return mc;

            }


        }


            return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    public class seqcomparator implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {

            try{
                String s11 = genHash(Integer.toString( Integer.parseInt(s1)/2));
                String s22 = genHash(Integer.toString( Integer.parseInt(s2)/2));
                return  s11.compareTo(s22);
            }
            catch(Exception exc) {Log.d("genhash", "gen hash here too");}
            return  0;
        }
    }





    private class ServerTask extends AsyncTask<ServerSocket, String, String> {

        @Override
        protected String doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            int sequencenum = 0;
            Log.d("test", "Inside the server task");
            Log.d(TAG, "Server testing");
            try {
                //publishProgress("test1");
                while (1 == 1) {
                    Socket s = serverSocket.accept();
                    String msg = "";
                    try{
                        DataInputStream dis = new DataInputStream(s.getInputStream());
                        msg = dis.readUTF();}
                    catch (Exception e) {
                        Log.d("dis",  "input data empty");

                    }
                    Log.d("server", "inpuit message:" + msg);
                    String [] msgr = msg.split(" ");










                    //didimissanything


                    if(msgr.length == 2 && msgr[0].equals("dmiss"))
                    {
                        Log.d("miss_asking", "some one asking:" + msgr[1]);

                        String r= ""; ;
                        if(msgr[1].equals("11108")) {
                            Log.d("server", "yes so 11108 is asking for missed data " + "what i have is :"  + misses[0]); r = misses[0]; misses[0] = "";
                            File file = new File(context.getFilesDir(), "misses0" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            //Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write("");
                            fw.close();
                        }
                        else if (msgr[1].equals("11112")) {r = misses[1]; misses[1] = "";
                            File file = new File(context.getFilesDir(), "misses1" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            //Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write("");
                            fw.close();}
                        else if (msgr[1].equals("11116")) {r = misses[2]; misses[2] = "";
                            File file = new File(context.getFilesDir(), "misses2" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            //Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write("");
                            fw.close();}
                        else if (msgr[1].equals("11120")) {r = misses[3]; misses[3] = "";
                            File file = new File(context.getFilesDir(), "misses3" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            //Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write("");
                            fw.close();}
                        else if (msgr[1].equals("11124")) {r = misses[4]; misses[4] = "";
                            File file = new File(context.getFilesDir(), "misses4" + ".txt");
                            FileWriter fw =  new FileWriter(file);
                            //Log.d("filewrite" , "key:" + key + "value:" + string );
                            fw.write("");
                            fw.close();}
                        else r = "";

                        Log.d("miss_asking", "what i told:" + msgr[1] + "matter is :" +r);
                        if(r == null) r=" ";
                        //else {r = r.trim();}
                        {r = r.trim();
                            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                            dos.writeUTF(r);
                            dos.flush();}


                    }

                    ///did i miss anything ends here






                    if(msgr.length == 4)
                    {
                        //String for_park_node =  "blindinsert" + " " + "key" + " "+ key + " "+ string;
                        if(msgr[0].equals("blindinsert") || msgr[0].equals("replica_forwarding"))
                        {
                            try {
                                // outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                                //OutputStream fo = new FileOutputStream(filename);
                                if (context == null)
                                    Log.e("context", "null");
                                Log.e("path", context.getFilesDir().getAbsolutePath());
                                File file = new File(context.getFilesDir(), msgr[2] + ".txt");
                                FileWriter fw =  new FileWriter(file);
                                Log.d("filewrite" , "key:" + msgr[2] + "value:" + msgr[3] );
                                fw.write(msgr[3]);
                                fw.close();

                                String returninserted = "inserted";  // added inserted
                                DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                                dos.writeUTF(returninserted);
                                dos.flush();

                                if(msgr[0].equals("blindinsert")) {
                                    String forward = "replica_forwarding" + " " + msgr[1] + " " + msgr[2] + " " + msgr[3];
                                    //generate_clienttask(forward);
                                }


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                    /////////////////// regular query
                    if(msgr.length == 2 && msgr[0].equals("query"))
                    {

                        try{
                            Context context =  getContext();
                            File fin = new File(context.getFilesDir().getPath());
                            FileReader fr = new FileReader(fin + "/" + msgr[1]+ ".txt");
                            Log.e("file", "inside file: path: " + fin + "/" + msgr[1]);
                            BufferedReader br = new BufferedReader(fr);
                            String returnvaluenow1 = br.readLine();
                            br.close();
                            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                            dos.writeUTF(returnvaluenow1);
                            dos.flush();
                            Log.d("input_server" , "message sent in response to query " + msgr[1] + "ans:retuern value" + returnvaluenow1 );
                            //s.close();/// closing socket here
                            //return returnvaluenow1;
                        }
                        catch (Exception e) {
                            Log.e("readerror", "unable to read the file");
                        }
                    }



                    ////////for delete
                    if(msgr.length == 2 && msgr[0].equals("delete"))
                    {
                          String del_file = msgr[1];
                        try{
                            Context context =  getContext();
                            //Context context = getContext();
                            Log.d("inside_delete", " deleting this file" + del_file);
                            File fin = new File(context.getFilesDir(), (del_file + ".txt"));
                            fin.delete();
                            //File fin = new File(context.getFilesDir().getPath());
                            ///FileReader fr = new FileReader(fin + "/" + msgr[1]+ ".txt");
                            //Log.e("file", "inside file: path: " + fin + "/" + msgr[1]);
                            //BufferedReader br = new BufferedReader(fr);
                            //String returnvaluenow1 = br.readLine();
                            //br.close();
                            //////////////
                            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                            dos.writeUTF("deleted");
                            dos.flush();
                            //Log.d("input_server" , "message sent in response to query " + msgr[1] + "ans:retuern value" + returnvaluenow1 );
                            //s.close();/// closing socket here
                            //return returnvaluenow1;
                        }
                        catch (Exception e) {
                            Log.e("deleteerror", "unable to delete the file");
                        }
                    }

                    ///delete ends here

                    /// tell if present or not
                    //String askifpresent = "present?" + eachfile;
                    if(msgr.length == 2 && msgr[0].equals("present?"))
                    {
                        String check_file = msgr[1];
                        try{
                            Context context =  getContext();
                            //Context context = getContext();
                            String truefalse ="";




                            //Log.d("inside_delete", " deleting this file" + del_file);
                            File fin = new File(context.getFilesDir(), (check_file));
                            if(fin.exists()) truefalse = "yes";
                            else truefalse = "no";
                            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                            dos.writeUTF(truefalse);
                            dos.flush();
                        }
                        catch (Exception e) {
                            Log.e("deleteerror", "unable to delete the file");
                        }
                    }

                    if(msgr.length == 1 && msgr[0].equals("starquery"))
                    {
                        String returnstar = "";

                        try{
                            Context context =  getContext();
                            Log.d("inside@", " regular now what2");
                            File fin = new File(context.getFilesDir().getPath());
                            Log.d("inside@", " regular now what3");
                            File [] listoffiles = fin.listFiles();
                            Log.d("inside@", " regular now what3");
                            for(int i=0; i< listoffiles.length; i++)
                            {
                                //listoffiles[i].getName()
                                String eachfile= listoffiles[i].getName();
                                if(eachfile.equals("misses0.txt") || eachfile.equals("misses1.txt") || eachfile.equals("misses2.txt") || eachfile.equals("misses3.txt") || eachfile.equals("misses4.txt")) continue;
                                Log.d("inside@", " regular now what4");
                                FileReader fr = new FileReader(fin + "/" + eachfile); /// removed + .txt here
                                Log.e("file", "inside file: path: " + fin + "/" + eachfile);
                                BufferedReader br = new BufferedReader(fr);
                                String  valuenow = br.readLine();
                                br.close();
                                Log.d("inside@", "now what5");
                                String []rw = new String[2];
                                eachfile = eachfile.substring(0, eachfile.lastIndexOf('.'));
                                returnstar +=  eachfile + " " + valuenow + " ";
                                //rw[0] = eachfile; test_s = test_s + eachfile +" "; Log.d("@_each",  "regular test_s"+  test_s);
                                //rw[1] = valuenow;  test_v = test_v + valuenow +" "; Log.d("@_each",  "regular test_v"+  test_v);
                                //mc.addRow(rw);
                                Log.d("resturnall", "returnallstring is here" + returnstar);

                            }
                            //Log.d("@",  "all selections:" + test_s + "all values:" +  test_v);
                            //return mc;
                            returnstar = returnstar.trim();
                            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                            dos.writeUTF(returnstar);
                            dos.flush();
                            //return returnstar;
                        }
                        catch (Exception e) {
                            Log.e("readerror", "unable to read the file");
                        }
                    }








                    s.close();
                }


            } catch (IOException e) {

                Log.d("wtf", "server got wrong");
                publishProgress("exception");
                e.printStackTrace();
            }


            return ""; // instead of null;
        }
    }
    ///// server task ends here:



    ///client task starts here:

    private class ClientTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... msgs) {
            String msgin = "";
            try {
                //String remotePort = REMOTE_PORT0;
                //if (msgs[1].equals(REMOTE_PORT0))


                ///removed return null
                //if (portnum.equals("11108")) return null;

                Log.d("test", "Inside the client task");

                String remotePort = msgs[1];

                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(remotePort));

                Log.d("test", "Error at new socket");

                String msgToSend = msgs[0];
                String [] msgrcv ;
                msgrcv = msgToSend.split(" ");

                /*
                 * TODO: Fill in your client code that sends out a message.
                 */
                Log.d("MyApp", "I am here:" + msgToSend + " portstr" + remotePort);
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF(msgToSend);
                dos.flush();
                Log.d("MyApp", "output complete till here");
                Log.d("MyApp", "now i need to get input from other nodes");

                //try{
                //Thread.sleep(200);}
                //catch(Exception e ) {}


                try {
                    //// proposal recieving from all other avds
                    DataInputStream datainputstream = new DataInputStream(socket.getInputStream());
                    msgin = datainputstream.readUTF();
                    //String [] rcvmsg_reply = msgin.split(" ");
                    Log.d("client_input", msgin);
                    return msgin;


                }
                catch (Exception e)
                {// something_crashed = true;
                    Log.d("deadnodes", "this node is not present" + remotePort);


                }
                //// / if the server of 11108 sends the info about its new pred and asks it to set succ on avd0
                //DataInputStream datainputstream = new DataInputStream(socket.getInputStream());
                //String msgin = datainputstream.readUTF();
                //Log.d("Ack", "Just Acknowledgement" + msgin);
                socket.close();

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }

            return "";
        }
    }


}
