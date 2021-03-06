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
bool statusOk=false,lock=true;
int portNumber=0;
datetime lastBarTime;
ZmqMsg reply;
ZmqMsg request;
double lastBid=0.0,lastAsk=0.0;
//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+
Context dataContext("dataContext");
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
   createButton("IS_ALIVE",10,230);
   createButton("SEND_HISTORY",10,200);
   createButton("SEND_TICK",10,170);
   createLabel("TERMINAL_STATUS","MT4: OFFLINE",10,10);
   createLabel("SERVER_STATUS","SERVER: OFFLINE",10,30);

//---
   portNumber=getPort();
   dataHandle=dataSocket.connect("tcp://localhost:"+portNumber);
   dataSocket.setReceiveTimeout(1000);

   checkStatus();


//Print("INIT");

   return(INIT_SUCCEEDED);
  }
//+------------------------------------------------------------------+
//| Expert deinitialization function                                 |
//+------------------------------------------------------------------+
void OnDeinit(const int reason)
  {
//--- destroy timer
   ObjectsDeleteAll();
   Print("DEINIT");
   dataSocket.disconnect("tcp://localhost:"+portNumber);
   dataContext.destroy(1);
   EventKillTimer();



  }
//+------------------------------------------------------------------+
//| Expert tick function                                             |
//+------------------------------------------------------------------+
void OnTick()
  {
//---

   if(statusOk && !lock)
     {
      if(Bid==lastBid && Ask==lastAsk)
         return;
      lastAsk=Ask;
      lastBid=Bid;
      ZmqMsg request(StringFormat("T;%s;%f;%f",TimeToStr(Time[0]),Bid,Ask));
      //Print(StringFormat("T;%s;%f;%f",TimeToStr(Time[0]),Bid,Ask));
      dataSocket.send(request);
      dataSocket.recv(reply);
      if(reply.size()==0)
        {
         checkStatus();
         return;
        }
      if(Time[0]!=lastBarTime)
        {
         lastBarTime=Time[0];
         message=StringFormat("M;%s;%f;%f;%f;%f;%.0f",TimeToStr(iTime(NULL,0,0)),iOpen(NULL,0,1),iHigh(NULL,0,1),iLow(NULL,0,1),iClose(NULL,0,1),iVolume(NULL,0,1));

         ZmqMsg request(message);
         //PrintFormat("Barchange Sending  ... "+message);
         dataSocket.send(request);
         dataSocket.recv(reply);
         if(reply.size()==0)
           {
            checkStatus();
            return;
           }
        }

     }




  }
//+------------------------------------------------------------------+
//| Timer function                                                   |
//+------------------------------------------------------------------+
void OnTimer()
  {
   if(!statusOk || !IsConnected())
      checkStatus();
//---
//if(!mt4TerminalOk)
//  {
//  Print("+++++++++++++++++Timer");
//   portNumber=getPort();
//   if(portNumber>0)
//     {
//      dataHandle=dataSocket.connect("tcp://localhost:"+portNumber);
//      dataSocket.setReceiveTimeout(1000);
//      sendInitialBars(100);
//     }
//  }

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
        {
          OnInit();

         //dataSocket.disconnect("tcp://localhost:"+portNumber);
         //dataContext.destroy(1);
         //OnInit();
         //dataContext.destroy(dataHandle);
         //dataContext.shutdown();
        }
      if(sparam=="SEND_TICK")
         sendTick();
      if(sparam=="IS_ALIVE")
         checkStatus();

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
//ZmqMsg  request(StringFormat("S;%s;%f;%f",TimeToStr(Time[0]),Bid,Ask));
//dataSocket.send(request);
//dataSocket.recv(reply);
//if(reply.size()==0)
//  {
//   checkStatus();
//   return;
//  }
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

   ZmqMsg reply;


//ZmqMsg request("init");
//dataSocket.send(request);
//dataSocket.recv(reply);
//if(reply.size()==0)
//  {
//   checkStatus();
//   return;
//  }

   for(int i=barNumber; i>0; i--)
     {
      ZmqMsg requestTick(StringFormat("T;%s;%f;%f",TimeToStr(iTime(NULL,0,i)),iClose(NULL,0,i),iClose(NULL,0,i)));
      dataSocket.send(requestTick);
      dataSocket.recv(reply);
      if(reply.size()==0)
        {
         checkStatus();
         return;
        }

      message=StringFormat("M;%s;%f;%f;%f;%f;%.0f",TimeToStr(iTime(NULL,0,i-1)),iOpen(NULL,0,i),iHigh(NULL,0,i),iLow(NULL,0,i),iClose(NULL,0,i),iVolume(NULL,0,i));
      ZmqMsg requestMinute(message);
      //PrintFormat(i+". : "+message);
      dataSocket.send(requestMinute);
      dataSocket.recv(reply);

      if(!reply.size()>0)
        {
         checkStatus();
         return;
        }
      //Sleep(100);
     }
   lastBarTime=Time[0];


//ZmqMsg request(StringFormat("T;%f;%f",Bid,Ask));
//dataSocket.send(request);
//dataSocket.recv(reply);


  }
//+------------------------------------------------------------------+
int getPort()
  {
   string symbol=Symbol();
   int id=0;
   for(int j=0; j<StringLen(symbol); j++)
     {

      if(MathMod(j,2)==0)
         id+=(StringGetChar(symbol,j)) *MathPow(2,j);
      if(MathMod(j,2)==1)
         id+=(StringGetChar(symbol,j)) *MathPow(3,j);
     }
   Print("Port: "+id);
   return id;

  }
//+------------------------------------------------------------------+
void checkStatus()
  {
//PrintFormat("Checking status ....");


   statusOk=false;


   if(!IsConnected())
     {
      ObjectSetString(0,"TERMINAL_STATUS",OBJPROP_TEXT,"MT4: OFFLINE");
      ObjectSetString(0,"SERVER_STATUS",OBJPROP_TEXT,"SERVER: UNKNOWN");
      return;
     }

   //Print(Symbol()+" Ping: "+"P;"+TimeToStr(Time[0]));

   ZmqMsg  request("P;"+TimeToStr(Time[0]));   // ping
   dataSocket.send(request);
   dataSocket.recv(reply);

   if(reply.size()>0)
     {
      //Print(Symbol()+ " REQUESTED BAR NUMBER: "+reply.getData());

      ObjectSetString(0,"TERMINAL_STATUS",OBJPROP_TEXT,"MT4: OK");
      ObjectSetString(0,"SERVER_STATUS",OBJPROP_TEXT,"SERVER: OK");

      if(StrToInteger(reply.getData())==0)
        {
         ZmqMsg  request("P;"+TimeToStr(Time[0]));   // ping
         dataSocket.send(request);
         dataSocket.recv(reply);
         //Print(Symbol()+ " RE REQUESTED BAR NUMBER: "+reply.getData());
        }

      sendInitialBars(StrToInteger(reply.getData()));
      statusOk=true;
      lock=false;
      return;

     }
   else
     {
      lock=true;
      ObjectSetString(0,"TERMINAL_STATUS",OBJPROP_TEXT,"MT4: OK");
      ObjectSetString(0,"SERVER_STATUS",OBJPROP_TEXT,"SERVER: OFFLINE");
      return;


     }

   return ;

  }
//+------------------------------------------------------------------+
