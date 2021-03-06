//+------------------------------------------------------------------+
//|                                                      ZMQTest.mq4 |
//|                        Copyright 2014, MetaQuotes Software Corp. |
//|                                              http://www.mql5.com |
//+------------------------------------------------------------------+
#property copyright "Copyright 2014, MetaQuotes Software Corp."
#property link      "http://www.mql5.com"
#property version   "1.00"
#property strict

#include <Zmq/Zmq.mqh>
extern string PROJECT_NAME = "DWX_ZeroMQ_MT4_Server";
extern string ZEROMQ_PROTOCOL = "tcp";
extern string HOSTNAME = "*";
extern int PUSH_PORT = 5557;
extern int PULL_PORT = 32769;
extern int PUB_PORT = 32770;
extern int MILLISECOND_TIMER = 1000;

extern string t0 = "--- Trading Parameters ---";
extern int MagicNumber = 123456;
extern int MaximumOrders = 1;
extern double MaximumLotSize = 0.01;
extern int MaximumSlippage = 3;
extern bool DMA_MODE = true;

extern string t1 = "--- ZeroMQ Configuration ---";
extern bool Publish_MarketData = false;

// CREATE ZeroMQ Context
Context context(PROJECT_NAME);

// CREATE ZMQ_PUSH SOCKET
Socket pushSocket(context, ZMQ_PUSH);

// CREATE ZMQ_PULL SOCKET
Socket pullSocket(context, ZMQ_PULL);
//+------------------------------------------------------------------+
//| Expert initialization function                                   |
//+------------------------------------------------------------------+
int OnInit()
  {
//--- create timer




   /* Set Socket Options */

// Send responses to PULL_PORT that client is listening on.
   pushSocket.setSendHighWaterMark(1);
   pushSocket.setLinger(0);
   Print("[PUSH] Binding MT4 Server to Socket on Port " + IntegerToString(PULL_PORT) + "..");
   pushSocket.bind(StringFormat("%s://%s:%d", ZEROMQ_PROTOCOL, HOSTNAME, PULL_PORT));

// Receive commands from PUSH_PORT that client is sending to.
   pullSocket.setReceiveHighWaterMark(1);
   pullSocket.setLinger(0);
   Print("[PULL] Binding MT4 Server to Socket on Port " + IntegerToString(PUSH_PORT) + "..");
   pullSocket.bind(StringFormat("%s://%s:%d", ZEROMQ_PROTOCOL, HOSTNAME, PUSH_PORT));
   
   MessageBox("Start workers in another terminal and press Enter if they are ready.","Wait for workers...",MB_OK|MB_ICONINFORMATION);
      EventSetMillisecondTimer(MILLISECOND_TIMER);     // Set Millisecond Timer to get client socket input

   context.setBlocky(false);

//---
   return(INIT_SUCCEEDED);
  }
//+------------------------------------------------------------------+
//| Expert deinitialization function                                 |
//+------------------------------------------------------------------+
void OnDeinit(const int reason)
  {
//--- destroy timer
   Print("[PUSH] Unbinding MT4 Server from Socket on Port " + IntegerToString(PULL_PORT) + "..");
   pushSocket.unbind(StringFormat("%s://%s:%d", ZEROMQ_PROTOCOL, HOSTNAME, PULL_PORT));
   pushSocket.disconnect(StringFormat("%s://%s:%d", ZEROMQ_PROTOCOL, HOSTNAME, PULL_PORT));

   Print("[PULL] Unbinding MT4 Server from Socket on Port " + IntegerToString(PUSH_PORT) + "..");
   pullSocket.unbind(StringFormat("%s://%s:%d", ZEROMQ_PROTOCOL, HOSTNAME, PUSH_PORT));
   pullSocket.disconnect(StringFormat("%s://%s:%d", ZEROMQ_PROTOCOL, HOSTNAME, PUSH_PORT));

// Destroy ZeroMQ Context
   context.destroy(0);

   EventKillTimer();

  }
//+------------------------------------------------------------------+
//| Expert tick function                                             |
//+------------------------------------------------------------------+
void OnTick()
  {
//---

  }
//+------------------------------------------------------------------+
//| Timer function                                                   |
//+------------------------------------------------------------------+
void OnTimer()
  {
//---
   Print("TIMER");
   ZmqMsg message("0");
   message.rebuild(IntegerToString(100));
   pushSocket.send(message,true);
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

  }
//+------------------------------------------------------------------+


//+------------------------------------------------------------------+
//|                                                    TaskEvent.mq4 |
//|                  Copyright 2017, Bear Two Technologies Co., Ltd. |
//|                                                dingmaotu@126.com |
//+------------------------------------------------------------------+
//#property copyright "Copyright 2017, Bear Two Technologies Co., Ltd."
//#property link      "dingmaotu@126.com"
//#property version   "1.00"
//#property strict
//
//#include <Zmq/Zmq.mqh>
//
//#define within(num) (int) ((float) num * MathRand() / (32767 + 1.0))
////+------------------------------------------------------------------+
////| Task ventilator in MQL (adapted from the C++ version)            |
////| Binds PUSH socket to tcp://localhost:5557                        |
////| Sends batch of tasks to workers via that socket                  |
////|                                                                  |
////| Olivier Chamoux <olivier.chamoux@fr.thalesgroup.com>             |
////+------------------------------------------------------------------+
//void OnStart()
//  {
//   Context context;
//
////--- Socket to send messages on
//   Socket sender(context,ZMQ_PUSH);
//   sender.bind("tcp://*:5557");
//
////--- Block execution of Script: please start workers in another terminal
//   MessageBox("Start workers in another terminal and press Enter if they are ready.","Wait for workers...",MB_OK|MB_ICONINFORMATION);
//   Print("Sending tasks to workers…");
//
////--- The first message is "0" and signals start of batch
//   Socket sink(context,ZMQ_PUSH);
//   sink.connect("tcp://localhost:5558");
//   ZmqMsg message("0");
//   sink.send(message);
//
////--- Initialize random number generator
//   MathSrand(GetTickCount());
//
////--- Send 100 tasks
//   int totalMillis=0;     //--- Total expected cost in msecs
//   for(int i=0; i<100; i++)
//     {
//      //--- Random workload from 1 to 100msecs
//      int workload=within(100)+1;
//      totalMillis+=workload;
//      message.rebuild(IntegerToString(workload));
//      sender.send(message);
//     }
//   Print("Total expected cost: ",totalMillis," msec");
//   Sleep(1000);           //--- Give 0MQ time to deliver
//  }
////+------------------------------------------------------------------+
