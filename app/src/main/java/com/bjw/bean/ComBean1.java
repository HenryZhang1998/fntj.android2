package com.bjw.bean;

import java.text.SimpleDateFormat;

public class ComBean1 {
        public byte[] bRec=null;
        public String sRecTime="";
        public String sComPort="";
        public ComBean1(String sPort,byte[] buffer,int size){
            sComPort=sPort;
            bRec=new byte[size];
            for (int i = 0; i < size; i++)
            {
                bRec[i]=buffer[i];
            }
            SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm:ss");
            sRecTime = sDateFormat.format(new java.util.Date()); 
        }
}