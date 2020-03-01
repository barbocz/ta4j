//+------------------------------------------------------------------+
//|                                                 RefreshChart.mq4 |
//|                        Copyright 2014, MetaQuotes Software Corp. |
//|                                              http://www.mql5.com |
//+------------------------------------------------------------------+
#property copyright "Copyright 2014, MetaQuotes Software Corp."
#property link      "http://www.mql5.com"
#property version   "1.00"
#property strict

#import "user32.dll"
int PostMessageA(int hWnd, int Msg, int wParam, int lParam);
int RegisterWindowMessageA(string lpString);
#import

#include <WinUser32.mqh>
#include <stdlib.mqh>
string STRATEGY_ID,TRADE_NUMBER,PROFITABLE_TRADES_RATIO,EQUITY_MINIMUM,BALANCE_DRAWDOWN,TOTAL_PROFIT_PER_MONTH,OPEN_PROFIT;
bool templateApplied=false;
long chartId;
//+------------------------------------------------------------------+
//| Expert initialization function                                   |
//+------------------------------------------------------------------+
int OnInit()
  {
//--- create timer
//EventSetTimer(60);

   chartId=ChartID();

   ChartSetInteger(chartId,CHART_AUTOSCROLL,false);

   int file_handle=FileOpen("endFile",FILE_READ);

   ushort sep=StringGetCharacter("|",0);


   if(file_handle!=INVALID_HANDLE)
     {
      //--- additional variables
      int    str_size;

      string str;
      string item[];

      int lineNumber=0;
      while(!FileIsEnding(file_handle))
        {
         //--- find out how many symbols are used for writing the time

         str_size=FileReadInteger(file_handle,INT_VALUE);
         str=FileReadString(file_handle,str_size);
         Print(str);

         if(StringLen(str)>1)
           {

            StringSplit(str,sep,item);

            if(lineNumber==1)
              {
               STRATEGY_ID=item[0];
               createLabel("STRATEGY_ID","ID:"+item[0],100,10);
               TRADE_NUMBER=item[1];
               createLabel("TRADE_NUMBER","TRADES NUMBER:"+item[1],300,10);
               PROFITABLE_TRADES_RATIO=item[2];
               createLabel("RATIO","RATIO:"+item[2],550,10);
               EQUITY_MINIMUM=item[3];
               createLabel("MINIMUM","MINIMUM:"+item[3],700,10);
               BALANCE_DRAWDOWN=item[4];
               TOTAL_PROFIT_PER_MONTH=item[5];
               OPEN_PROFIT=item[6];
               createLabel("PROFIT","PROFIT:"+item[5],900,10);
               createLabel("OPEN_PROFIT","OPEN PROFIT:"+item[6],1100,10);
              }

           }
         lineNumber++;

        }
      FileClose(file_handle);
      Print("STRATEGY_ID "+STRATEGY_ID+" , TRADE_NUMBER "+TRADE_NUMBER+"  TOTAL_PROFIT_PER_MONTH "+TOTAL_PROFIT_PER_MONTH,"  OPEN_PROFIT "+OPEN_PROFIT);
     }
   else
      PrintFormat("Failed to open %s file, Error code = %d","InpFileName",GetLastError());
   createButton("LOAD",300,10);


//ChartApplyTemplate(0,"\\Templates\\offline.tpl");

//---
   return(INIT_SUCCEEDED);
  }
//+------------------------------------------------------------------+
//| Expert deinitialization function                                 |
//+------------------------------------------------------------------+
void OnDeinit(const int reason)
  {
//--- destroy timer
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
//if(!templateApplied)
//  {
//   ChartApplyTemplate(ChartID(),"\\Templates\\offline.tpl");
//   ChartNavigate(ChartID(),CHART_END,0);
//   templateApplied=true;
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
//if(id==CHARTEVENT_OBJECT_CLICK)
//  {
//   if(sparam=="LOAD")
//     {
//      ChartApplyTemplate(ChartID(),"\\Templates\\offline.tpl");
//      ChartNavigate(ChartID(),CHART_END,0);
//     }
//  }

   if(id==CHARTEVENT_CLICK)
     {

      int clickX=(int)lparam;
      int clickY=(int)dparam;
      int clickWindow=0;
      datetime clickTime;
      double clickPrice;
      ChartXYToTimePrice(0,clickX,clickY,clickWindow,clickTime,clickPrice);
      if(clickX<100)
        {
         ChartApplyTemplate(ChartID(),"\\Templates\\offline.tpl");
         ChartNavigate(ChartID(),CHART_END,0);

        }

      //+------------------------------------------------------------------+
      //|                                                                  |
      //+------------------------------------------------------------------+
      //+------------------------------------------------------------------+
      //|                                                                  |
      //+------------------------------------------------------------------+
      //if(ChartXYToTimePrice(0,clickX,clickY,clickWindow,clickTime,clickPrice))
      //   Print("--------------------------------CLICK1:",TimeToStr(clickTime),"  ",clickX,"   ",clickWindow);
     }

//   if(id==CHARTEVENT_CLICK)
//     {
//
//      ChartApplyTemplate(ChartID(),"\\Templates\\offline.tpl");
//      ChartNavigate(ChartID(),CHART_END,0);
//            Print(WindowFind("SUBCHART"));
//            Print(  ChartIndicatorName( 0,WindowFind("SUBCHART"),0));
//
//
//            bool res=ChartIndicatorDelete(0,WindowFind("SUBCHART"),"SUBCHART");
//            //--- Analyse the result of call of ChartIndicatorDelete()
//            if(!res)
//              {
//               PrintFormat("Failed to delete indicator %s from window #%d. Error code %d",
//                           "subchart",4,GetLastError());
//              }
//
//            int hwnd = WindowHandle(Symbol(), Period());
//
//            if(hwnd != 0)
//               Print("Chart window detected "+hwnd);
//
//            int MT4InternalMsg = RegisterWindowMessageA("MetaTrader4_Internal_Message");
//
//
//            if(hwnd != 0)
//               if(PostMessageA(hwnd, WM_COMMAND, 0x822c, 0) == 0)
//                  hwnd = 0;
//            if(hwnd != 0 && MT4InternalMsg != 0)
//               PostMessageA(hwnd, MT4InternalMsg, 2, 1);
//     }

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
   ObjectSetInteger(0,objectName,OBJPROP_FONTSIZE,10);

  }
//+------------------------------------------------------------------+
void createButton(string label,int x,int y)
  {
   string objectName=label;

   ObjectDelete(0,objectName);
   ObjectCreate(0,objectName,OBJ_BUTTON,0,0,0);
//--- set button coordinates
   ObjectSetInteger(0,objectName,OBJPROP_XDISTANCE,x);
   ObjectSetInteger(0,objectName,OBJPROP_YDISTANCE,y);
//--- set button size
   ObjectSetInteger(0,objectName,OBJPROP_XSIZE,150);
   ObjectSetInteger(0,objectName,OBJPROP_YSIZE,25);
//--- set the chart's corner, relative to which point coordinates are defined
   ObjectSetString(0,objectName,OBJPROP_TEXT,label);
   ObjectSetInteger(0,objectName,OBJPROP_CORNER,CORNER_RIGHT_UPPER);


  }
//+------------------------------------------------------------------+
