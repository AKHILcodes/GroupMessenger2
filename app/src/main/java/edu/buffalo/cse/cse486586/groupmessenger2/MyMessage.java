package edu.buffalo.cse.cse486586.groupmessenger2;

/**
 * Created by akhil on 3/25/17.
 */

public class MyMessage implements Comparable<MyMessage> {

    private String str;
    private boolean deliverableFlag;
    private int seqno;
    private int portNum;
    private int senderPort;

    public MyMessage(String s, boolean f, int j, int k, int l){
        str = s;
        deliverableFlag = f;
        seqno = j;
        portNum = k;
        senderPort = l;
    }

    public String getStr(){return str;}
    public int getSeqno(){return seqno;}
    public boolean getFlag(){return deliverableFlag;}
    public int getPortNum(){return portNum;}
    public int getSenderPort(){return senderPort;}

    @Override
    public int compareTo(MyMessage another) {
        float tobeComp = Float.parseFloat(Integer.toString(this.getSeqno()) + "." + (this.getSenderPort()));
        float CompWith = Float.parseFloat(Integer.toString(another.getSeqno()) + "." + (another.getSenderPort()));
        if(tobeComp > CompWith)
            return 1;
        else
            return -1;
    }
    public String toString(){
        return "msg:"+str+"|deliverable:"+deliverableFlag+"|seqNo:"+seqno+"|portNum:"+portNum;
    }
}
