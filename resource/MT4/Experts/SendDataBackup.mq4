//+------------------------------------------------------------------+
//|                                                     SendData.mq4 |
//|                        Copyright 2014, MetaQuotes Software Corp. |
//|                                              http://www.mql5.com |
//+------------------------------------------------------------------+
#property copyright "Copyright 2014, MetaQuotes Software Corp."
#property link      "http://www.mql5.com"
#property version   "1.00"
#property strict
#include <Zmq/Zmq.mqh>

int buttonIndex=0;
string message;
bool serverAlive=false;
int portNumber=0;
datetime lastBarTime;
ZmqMsg reply;
ZmqMsg request;

//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+
Context dataContext("dataContext");
Context initContext("initContext");
Socket initSocket(initContext,ZMQ_REQ);
Socket dataSocket(dataContext,ZMQ_REQ);
int initHandle,dataHandle;
//+------------------------------------------------------------------+
//| Expert initialization function                                   |
//+------------------------------------------------------------------+
int OnInit()
  {
//--- create timer

   ObjectsDeleteAll();
   EventSetTimer(10);
   createButton("TEST",10,300);
   createButton("INIT",10,230);
   createButton("SEND_HISTORY",10,200);
   createButton("SEND_TICK",10,170);
   createLabel("SERVER_STATUS","OFFLINE",10,10);

//---
   initSocket.setReceiveTimeout(1000);
   dataSocket.setReceiveTimeout(1000);

   initHandle=initSocket.connect("tcp://localhost:5000");
   portNumber=getPort();
   if(portNumber>0)
     {
      dataHandle=dataSocket.connect("tcp://localhost:"+portNumber);
      sendInitialBars(300);
     }

   return(INIT_SUCCEEDED);
  }
//+------------------------------------------------------------------+
//| Expert deinitialization function                                 |
//+------------------------------------------------------------------+
void OnDeinit(const int reason)
  {
//--- destroy timer

   EventKillTimer();
   ObjectsDeleteAll();


  }
//+------------------------------------------------------------------+
//| Expert tick function                                             |
//+------------------------------------------------------------------+
void OnTick()
  {
//---

   if(serverAlive)
     {
      ZmqMsg request(StringFormat("T;%f;%f",Bid,Ask));
      dataSocket.send(request);
      dataSocket.recv(reply);
      if(reply.size()==0)
        {
         serverAlive=false;
         ObjectSetString(0,"SERVER_STATUS",OBJPROP_TEXT,"OFFLINE");
         return;
        }
      if(Time[1]!=lastBarTime)
        {
         lastBarTime=Time[1];
         message=StringFormat("M;%s;%f;%f;%f;%f;%.0f",TimeToStr(iTime(NULL,0,1)),iOpen(NULL,0,1),iHigh(NULL,0,1),iLow(NULL,0,1),iClose(NULL,0,1),iVolume(NULL,0,1));

         ZmqMsg request(message);
         //PrintFormat("Barchange Sending  ... "+message);
         dataSocket.send(request);
         dataSocket.recv(reply);
        }
     }




  }
//+------------------------------------------------------------------+
//| Timer function                                                   |
//+------------------------------------------------------------------+
void OnTimer()
  {
//---
   if(!serverAlive)
     {
     Print("+++++++++++++++++Timer");
      portNumber=getPort();
      if(portNumber>0)
        {
         dataHandle=dataSocket.connect("tcp://localhost:"+portNumber);
         sendInitialBars(100);
        }
     }

  }
//+------------------------------------------------------------------+
//| ChartEvent function                                              |
//+------------------------------------------------------------------+
void OnChartEvent(const int id,
                  const long &lparam,
                  const double &dparam,
                  const string &sparam)
  {
//---
   if(id==CHARTEVENT_OBJECT_CLICK)
     {
      if(sparam=="SEND_HISTORY")
         sendHistory();
      if(sparam=="SEND_TICK")
         sendTick();
      if(sparam=="INIT")
         sendInitialBars(100);

      if(sparam=="TEST")
        {
         Print("TESTELEK1");
        }
     }

  }
//+------------------------------------------------------------------+
void sendHistory()
  {
   Print("HIST");
  }
//+------------------------------------------------------------------+
void sendTick()
  {
   Print("TICK");
  }

//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+
void createButton(string label,int x,int y)
  {
   string objectName=label;
   buttonIndex++;
   ObjectDelete(0,objectName);
   ObjectCreate(0,objectName,OBJ_BUTTON,0,0,0);
//--- set button coordinates
   ObjectSetInteger(0,objectName,OBJPROP_XDISTANCE,x);
   ObjectSetInteger(0,objectName,OBJPROP_YDISTANCE,y);
//--- set button size
   ObjectSetInteger(0,objectName,OBJPROP_XSIZE,250);
   ObjectSetInteger(0,objectName,OBJPROP_YSIZE,25);
//--- set the chart's corner, relative to which point coordinates are defined
   ObjectSetString(0,objectName,OBJPROP_TEXT,label);
   ObjectSetInteger(0,objectName,OBJPROP_CORNER,CORNER_LEFT_LOWER);


  }
//+------------------------------------------------------------------+
void createLabel(string objectName,string label,int x,int y)
  {

   ObjectDelete(0,objectName);
   ObjectCreate(0,objectName,OBJ_LABEL,0,0,0);
   ObjectSetInteger(0,objectName,OBJPROP_XDISTANCE,x);
   ObjectSetInteger(0,objectName,OBJPROP_YDISTANCE,y);
   ObjectSetInteger(0,objectName,OBJPROP_CORNER,CORNER_LEFT_UPPER);
   ObjectSetString(0,objectName,OBJPROP_TEXT,label);
   ObjectSetInteger(0,objectName,OBJPROP_FONTSIZE,12);

  }
//+------------------------------------------------------------------+
void sendInitialBars(int barNumber)
  {

   if(portNumber==0 || !serverAlive)
     {
     Print("+++++++++++++++++sendInitialBars");
      portNumber=getPort();
      if(portNumber>0)
         dataHandle=dataSocket.connect("tcp://localhost:"+portNumber);
     }

   ZmqMsg reply;

   if(serverAlive)
     {

      for(int i=barNumber; i>0; i--)
        {
         message=StringFormat("M;%s;%f;%f;%f;%f;%.0f",TimeToStr(iTime(NULL,0,i)),iOpen(NULL,0,i),iHigh(NULL,0,i),iLow(NULL,0,i),iClose(NULL,0,i),iVolume(NULL,0,i));
         ZmqMsg request(message);
         //PrintFormat("Sending  ... "+message);
         dataSocket.send(request);
         dataSocket.recv(reply);
        }
      lastBarTime=Time[1];
      
      ZmqMsg request(StringFormat("T;%f;%f",Bid,Ask));
      dataSocket.send(request);
      dataSocket.recv(reply);

     }

  }
//+------------------------------------------------------------------+
int getPort()
  {
   PrintFormat("Sending port request for "+Symbol());

   ZmqMsg request(Symbol());

  
   initSocket.send(request);
   initSocket.recv(reply);


   if(reply.size()>0)
     {
      portNumber=StrToInteger(reply.getData());
      Print("Port number: "+portNumber);
      ObjectSetString(0,"SERVER_STATUS",OBJPROP_TEXT,"LIVE:"+portNumber);
      serverAlive=true;
      return(portNumber);

     }
   else
     {
      serverAlive=false;
      ObjectSetString(0,"SERVER_STATUS",OBJPROP_TEXT,"OFFLINE");
      return 0;

     }

  }
//+------------------------------------------------------------------+
