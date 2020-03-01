//+------------------------------------------------------------------+
//|                                                     BackTest.mq4 |
//|                        Copyright 2014, MetaQuotes Software Corp. |
//|                                              http://www.mql5.com |
//+------------------------------------------------------------------+
#property copyright "Copyright 2014, MetaQuotes Software Corp."
#property link      "http://www.mql5.com"
#property version   "1.00"
#property strict

#include <Mql4Lib\Collection\HashMap.mqh>
HashMap<int,string>trades;
HashMap<int,datetime>closeTimes;
datetime lastBarChange,orderOpenTime,orderCloseTime;
int orderTpye;
double orderLot=0.1,orderPrice,orderClosePrice,stopLoss,takeProfit;
int tradeIndex=0,closeTicket,ticketNumber;

//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+
ushort sep=StringGetCharacter("|",0);
string str;
string item[];
bool found=false;

int minCounter=0;
//+------------------------------------------------------------------+
//| Expert initialization function                                   |
//+------------------------------------------------------------------+
int OnInit()
  {
//---


   int file_handle=FileOpen("log4J_trades.csv",FILE_READ);

   if(file_handle!=INVALID_HANDLE)
     {
      //--- additional variables
      int    str_size;



      //--- read data from the file

      while(!FileIsEnding(file_handle))
        {
         //--- find out how many symbols are used for writing the time

         str_size=FileReadInteger(file_handle,INT_VALUE);
         str=FileReadString(file_handle,str_size);


         if(StringLen(str)>1)
           {

            StringSplit(str,sep,item);
            trades.set(StrToInteger(item[0]),str);


            //               ObjectCreate(0,"LINE",OBJ_TREND,0,StrToTime("2019.11.01 21:30"),1.116,StrToTime("2019.11.01 23:03"),1.1167);
            //ObjectSetInteger(0,"LINE",OBJPROP_RAY_RIGHT,false);
            //ObjectSetInteger(0,"LINE",OBJPROP_COLOR,clrAqua);

            //string objectName="TRADELINE_"+item[0];
            //ObjectCreate(0,objectName,OBJ_TREND,0,StrToTime(item[2]),StrToDouble(item[3]),StrToTime(item[4]),StrToDouble(item[5]));
            //ObjectSetInteger(0,objectName,OBJPROP_RAY_RIGHT,false);
            //if(item[1]=="BUY")
            //   ObjectSetInteger(0,objectName,OBJPROP_COLOR,clrAqua);
            //else
            //   ObjectSetInteger(0,objectName,OBJPROP_COLOR,clrOrange);
            //ObjectSetInteger(0,objectName,OBJPROP_WIDTH,3);


           }

        }
      //--- close the file
      FileClose(file_handle);

     }
   else
      PrintFormat("log4J_trades.csv "+" not found!");

   getNextOrder();
//---
   return(INIT_SUCCEEDED);
  }
//+------------------------------------------------------------------+
//| Expert deinitialization function                                 |
//+------------------------------------------------------------------+
void OnDeinit(const int reason)
  {
//---

  }
//+------------------------------------------------------------------+
//| Expert tick function                                             |
//+------------------------------------------------------------------+
void OnTick()
  {
//---
   if(Time[0]!=lastBarChange)
     {
      lastBarChange=Time[0];
      //if (minCounter<50) {
      //Print(Time[0]);
      //minCounter++;
      //}

      if(lastBarChange>=orderOpenTime)
        {
         stopLoss=0.0;
         takeProfit=0.0;
         if(orderTpye==OP_BUY)
           {
            orderPrice=Ask;
            if(orderClosePrice>=Ask)
               takeProfit=orderClosePrice;
            else
               stopLoss=orderClosePrice;

           }
         else
           {
            orderPrice=Bid;
            if(orderClosePrice<=Bid)
               takeProfit=orderClosePrice;
            else
               stopLoss=orderClosePrice;
           }


         ticketNumber=OrderSend(NULL,orderTpye,orderLot,orderPrice,3,stopLoss,takeProfit,"",0,0);
         if(ticketNumber<0)
            Print("OrderSend ERROR --------------------------------------------");
         else
           {
            closeTimes.set(ticketNumber,orderCloseTime);

            getNextOrder();
            if(lastBarChange==orderOpenTime)
              {
               stopLoss=0.0;
               takeProfit=0.0;
               if(orderTpye==OP_BUY)
                 {

                  if(orderClosePrice>=Ask)
                     takeProfit=orderClosePrice;
                  else
                     stopLoss=orderClosePrice;

                 }
               else
                 {

                  if(orderClosePrice<=Bid)
                     takeProfit=orderClosePrice;
                  else
                     stopLoss=orderClosePrice;
                 }

               ticketNumber=OrderSend(NULL,orderTpye,orderLot,orderPrice,3,stopLoss,takeProfit,"",0,0);
               if(ticketNumber<0)
                  Print("OrderSend2 ERROR --------------------------------------------");
               closeTimes.set(ticketNumber,orderCloseTime);
               getNextOrder();
              }



           }
        }

      //for(int orderIndex=OrdersTotal(); orderIndex>0-1; orderIndex--)
      //  {
      //   if(OrderSelect(orderIndex,SELECT_BY_POS,MODE_TRADES)==false)
      //      continue;
      //   datetime toClose=closeTimes.get(OrderTicket(),NULL);
      //   if(Time[0]==toClose)
      //      closeTicket=OrderClose(OrderTicket(),OrderLots(),OrderType()==0?Bid:Ask,3);
      //   if(closeTicket<0)
      //      Print("ERRRRRRRRRRRRRRRRRRRRRRRROOOOOORRRR closeTicket:",OrderTicket());
      //  }

      //Print("CHANGE");
     }
  }
//+------------------------------------------------------------------+
void getNextOrder()
  {
   string trade=trades.get(tradeIndex,NULL);
   Print(tradeIndex+". "+trade);
   if(StringLen(trade)==0)
      return;
   StringSplit(trade,sep,item);

   orderOpenTime=StrToTime(item[2]);
   orderCloseTime=StrToTime(item[4]);
   orderTpye=item[1]=="BUY"?0:1;
   orderLot=item[6]==100000.0?1.0:0.5;
   orderClosePrice=NormalizeDouble(StrToDouble(item[5]),5);
   tradeIndex++;
//Print(tradeId+". Next orderOpenTime: "+TimeToString(orderOpenTime));
  }
//+------------------------------------------------------------------+

//+------------------------------------------------------------------+
