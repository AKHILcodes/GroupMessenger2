package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.EventLog;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    static final int SERVER_PORT = 10000;
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    int counter = 0;
    int P1counter = 0;
    int clientMessageCounter = 0;
    int maxSuggester = 0;
    boolean[] failedNode = new boolean[5];
    boolean aFailure = false;
    int FailedPort = -1;
    boolean broadcastedFailue = false;
    String PORT_Number;
    int[] PortLookup = new int[]{11108,11112,11116,11120,11124};
    PriorityBlockingQueue<MyMessage> pQ = new PriorityBlockingQueue<MyMessage>();
    private final Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        //added by akhil from simple messenger
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        PORT_Number =myPort;
        Log.e(TAG,"Port Numb is "+PORT_Number);
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }
        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        final TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        //added by akhil
        final EditText editText = (EditText) findViewById(R.id.editText1);
        final Button send = (Button) findViewById(R.id.button4);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = editText.getText().toString() + "\n";
                editText.setText("");
                tv.append("\t" + msg);
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
            }
        });
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            try {
                String str = "";
                while (true) {
                    Socket clSocket = serverSocket.accept();
                    DataOutputStream out = new DataOutputStream(clSocket.getOutputStream());
                    //out.writeUTF("All Well");

                    try {
                        DataInputStream br = new DataInputStream(clSocket.getInputStream());
                        str = br.readUTF();
                        out.writeUTF(Integer.toString(P1counter++));
                        String[] tokens = str.split(";");
                        if(tokens[0].equals("Akhil msg is")){

                            Log.e(TAG, "Server Side" + tokens[1]+"."+tokens[2] + "message is "+tokens[3]);
                            //Log.e(TAG, "unique id for msg is "+ tokens[1]+"."+tokens[2]);
                            //Log.e(TAG, "process id is "+str.substring(15,16));
                            MyMessage newmsg = new MyMessage(tokens[3],false,Integer.parseInt(tokens[1]),Integer.parseInt(tokens[2]),Integer.parseInt(tokens[2]));
                            pQ.add(newmsg);
                        }
                        else if(tokens[0].equals("Akhil FS is")){
                            int P2 = Integer.parseInt(tokens[4]);
                            P1counter=P2+1;

                            int identifier = Integer.parseInt(tokens[1]);
                            int id2 = Integer.parseInt(tokens[2]);
                            Log.e(TAG,"Server Side P2 for message " + tokens[5] +" with id("+identifier+"."+id2+") is"+P2);
                            Iterator it = pQ.iterator();
                            int sizePQ = pQ.size();
                            Log.e(TAG,"Serve Side Size of PQ "+Integer.toString(sizePQ));
                            while(it.hasNext()){
                                MyMessage temp = (MyMessage)it.next();
                                Log.e(TAG,"Server Side Entered First While Loop");
                                if(temp.getStr().equals(tokens[5])){
                                    String updatedmsg = temp.getStr();
                                    int sender = temp.getSenderPort();
                                    Log.e(TAG,"token error wala is"+tokens[3]);
                                    int updatedPortNum = Integer.parseInt(tokens[3]);
                                    Log.e(TAG,"updated msg is "+updatedmsg);
                                    Log.e(TAG,"P2 for msg is "+P2);
                                    Log.e(TAG,"Server Side " + tokens[1]+"."+tokens[2]+" message is "+updatedmsg+ "highest proposal port num is"+ updatedPortNum + "sender port is " + sender);
                                    it.remove();
                                    //pQ.remove(temp);
                                    MyMessage updatedMyMessage = new MyMessage(updatedmsg,true,P2,updatedPortNum,sender);
                                    pQ.add(updatedMyMessage);

                                    break;
                                }
                            }
                            while (!pQ.isEmpty()){
                                MyMessage temp1 = (MyMessage) pQ.peek();
                                if(temp1.getFlag()){
                                    String message = temp1.getStr();
                                    pQ.poll();
                                    saveMessageMethod(counter+"",message);
                                    counter++;
                                    publishProgress(message);
                                }
                                /*Log.e(TAG,"Server Side Entered Second while loop");
                                Log.e(TAG,"Server Side 2nd while loop size of PQ is "+newsize);
                                MyMessage temp = (MyMessage)iterator.next();
                                Log.e(TAG,"Curr_Msg:"+temp+" set P2 for:"+tokens[5]);
                                if(temp.getFlag()){
                                    Log.e(TAG,"Curr_Msg:"+temp+" inserting into DB");

                                    String message = temp.getStr();
                                    Log.e(TAG,"message to be pulled from PQ is "+message +"with seq no" + temp.getSeqno() +" with port num "+ temp.getPortNum());
                                    Log.e(TAG,"seq no of message to be pulled from PQ is "+temp.getSeqno());
                                    Log.e(TAG,"Port no of message to be pulled from PQ is "+temp.getPortNum());
                                    pQ.poll();
                                    Log.e(TAG,"CP message is "+ message + "with counter " + counter);
                                    Log.e(TAG,"CP count is" +counter);
                                    saveMessageMethod(counter+"",message);
                                    counter++;
                                    publishProgress(message);
                                }*/ else break;
                            }
                            
                        } else if(tokens[0].equals("Akhil FP is")){
                            int DevilPort = PortLookup[Integer.parseInt(tokens[1])];
                            Iterator it = pQ.iterator();
                            int sizePQ = pQ.size();
                            for(int i = 0; i < sizePQ;i++) {
                                MyMessage temp = (MyMessage) it.next();
                                if(temp.getSenderPort()==DevilPort){
                                    it.remove();
                                }
                            }
                        }

                        //br.close();
                    }catch (NullPointerException e){
                        Log.e(TAG,"null pointer data input stream");
                    }

                    clSocket.close();
                }
            }catch (IOException e){
                Log.e(TAG,"IOException in server");
            }
            return null;
        }

        private void saveMessageMethod(String count,String str) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("key", count);
            contentValues.put("value", str);
            getContentResolver().insert(mUri,contentValues);
        }


        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            TextView localTextView = (TextView) findViewById(R.id.textView1);
            localTextView.append(strReceived + "\t\n");

            return;
        }
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            String[] array = new String[5];
            array[0] = REMOTE_PORT0;
            array[1] = REMOTE_PORT1;
            array[2] = REMOTE_PORT2;
            array[3] = REMOTE_PORT3;
            array[4] = REMOTE_PORT4;
            float[] Proposed = new float[5];
            FailedPort = -1;
            float maxProposed = 0;
            for (int i = 0; i < 5; i++) {
                if(!failedNode[i]) {
                    try {
                        //Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(array[i]));
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt(array[i])),8000);
                        StringBuilder sb = new StringBuilder("Akhil msg is");

                        sb.append(";" + clientMessageCounter);
                        sb.append(";" + PORT_Number + ";");
                        String msgToSend = msgs[0];
                        if (msgToSend != null)
                            msgToSend = msgToSend.trim();
                        DataOutputStream pw = new DataOutputStream(socket.getOutputStream());
                        Log.e(TAG, "Sending in first for loop:" + sb.toString() + msgToSend);
                        pw.writeUTF(sb.append(msgToSend).toString());
                        pw.flush();
                        socket.setSoTimeout(5000);
                        try {
                            InputStream inFromServer = socket.getInputStream();
                            DataInputStream in = new DataInputStream(inFromServer);
                            String p1 = in.readUTF();
//                        Proposed[i] = Integer.parseInt(p1);
                            Proposed[i] = Float.parseFloat(p1 + "." + i);
                            if (Proposed[i] > maxProposed) {
                                maxProposed = Proposed[i];
                                maxSuggester = i;
                            }
                            maxProposed = (int) maxProposed;
                            //maxProposed = Proposed[i] > maxProposed ? Proposed[i] : maxProposed;
                        } catch (SocketTimeoutException ste) {
                            failedNode[i] = true;
                            aFailure = true;
                            FailedPort = i;
                            Log.e(TAG, "Time Out Exception in Client");
                        }
                    /*while (true){
                        InputStream inFromServer = socket.getInputStream();
                        DataInputStream in = new DataInputStream(inFromServer);
                        String comp = in.readUTF();
                        if(comp.equals("All Well"))
                            break;
                    }*/
                        socket.close();
                    } catch (UnknownHostException e) {
                        Log.e(TAG, "ClientTask UnknownHostException");
                    } catch (IOException e) {
                        Log.e(TAG, "ClientTask socket IOException");
                    }
                }

            }

            for(int i = 0; i < 5; i++) {
                if (!failedNode[i]) {
                    try {
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt(array[i])),8000);
                        String finalSeq = Integer.toString((int) maxProposed);
                        StringBuilder sb = new StringBuilder("Akhil FS is");
                        String msgToSend = msgs[0];
                        if (msgToSend != null)
                            msgToSend = msgToSend.trim();
                        sb.append(";" + clientMessageCounter + ";");
                        sb.append(PORT_Number + ";");
                        sb.append(maxSuggester + ";");
                        sb.append(finalSeq + ";");
                        Log.e(TAG, "Sending in second for loop:" + sb.toString() + msgToSend);
                        DataOutputStream pw = new DataOutputStream(socket.getOutputStream());
                        pw.writeUTF(sb.append(msgToSend).toString());
                        pw.flush();
                    } catch (UnknownHostException e) {
                        Log.e(TAG, "ClientTask UnknownHostException");
                    } catch (IOException e) {
                        Log.e(TAG, "ClientTask socket IOException");
                    }
                }
            }
            /*if(aFailure && (!broadcastedFailue)){
                for(int i = 0; i < 5; i++){
                    if (!failedNode[i]) {*/
            if(FailedPort > -1) {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(msgs[1])),8000);
                    String sb = "Akhil FP is" + ";" + FailedPort;
                    DataOutputStream pw = new DataOutputStream(socket.getOutputStream());
                    pw.writeUTF(sb);
                    pw.flush();
                    broadcastedFailue = true;
                } catch (UnknownHostException e) {
                    Log.e(TAG, "ClientTask UnknownHostException");
                } catch (IOException e) {
                    Log.e(TAG, "ClientTask socket IOException");
                }
            }
                   // }
               // }
            //}
            clientMessageCounter++;
            return null;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }
}
