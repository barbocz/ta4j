//+------------------------------------------------------------------+
//|                                               BackTestReport.mq4 |
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

#include <Mql4Lib\Collection\HashMap.mqh>
HashMap<int,double>takeProfits;
HashMap<int,double>stopLosses;
HashMap<int,string>exitLeves;

long lastModified;
string templateSuffix;
bool exitLevelsReady=false;

int      timeFrame=0;
string   symbol="*";

#include <WinUser32.mqh>
#include <stdlib.mqh>
//+------------------------------------------------------------------+
//| Expert initialization function                                   |
//+------------------------------------------------------------------+
int OnInit()
  {
//--- create timer
//EventSetTimer(1);
//lastModified=FileGetInteger("endFile",FILE_MODIFY_DATE);
   ushort sep=StringGetCharacter("|",0);
   string templateSuffix="";
   if(FileIsExist("log4J_Indicator_4.csv"))
      templateSuffix="_4";
   if(FileIsExist("log4J_Indicator_5.csv"))
      templateSuffix="_5";
   if(FileIsExist("log4J_Indicator_6.csv"))
      templateSuffix="_6";
   if(FileIsExist("log4J_Indicator_7.csv"))
      templateSuffix="_7";
   if(FileIsExist("log4J_Indicator_8.csv"))
      templateSuffix="_8";
      
         int hwnd = WindowHandle(Symbol(), Period());

   if(hwnd != 0)
      Print("Chart window detected "+hwnd);

   int MT4InternalMsg = RegisterWindowMessageA("MetaTrader4_Internal_Message");


   if(hwnd != 0)
      if(PostMessageA(hwnd, WM_COMMAND, 0x822c, 0) == 0)
         hwnd = 0;
   if(hwnd != 0 && MT4InternalMsg != 0)
      PostMessageA(hwnd, MT4InternalMsg, 2, 1);

   Print("templateSuffix: "+templateSuffix);
   ChartApplyTemplate(0,"\\Templates\\off"+templateSuffix+".tpl");
   ChartGetInteger(0,CHART_FIRST_VISIBLE_BAR,0);




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



//   if(FileGetInteger("endFile",FILE_MODIFY_DATE)!=lastModified)
//     {
//
//      lastModified=FileGetInteger("endFile",FILE_MODIFY_DATE);
//
//      createHistoryFile();
//      templateSuffix="";
//
//      if(FileIsExist("log4J_Indicator_4.csv"))
//         templateSuffix="_4";
//      if(FileIsExist("log4J_Indicator_5.csv"))
//         templateSuffix="_5";
//      if(FileIsExist("log4J_Indicator_6.csv"))
//         templateSuffix="_6";
//      if(FileIsExist("log4J_Indicator_7.csv"))
//         templateSuffix="_7";
//      if(FileIsExist("log4J_Indicator_8.csv"))
//         templateSuffix="_8";
//      Print("templateSuffix "+templateSuffix);
//      RefreshRates();
//
//      int hwnd = WindowHandle(Symbol(), Period());
//
//      if(hwnd != 0)
//         Print("Chart window detected "+hwnd);
//
//      int MT4InternalMsg = RegisterWindowMessageA("MetaTrader4_Internal_Message");
//
//
//      if(hwnd != 0)
//         if(PostMessageA(hwnd, WM_COMMAND, 0x822c, 0) == 0)
//            hwnd = 0;
//      if(hwnd != 0 && MT4InternalMsg != 0)
//         PostMessageA(hwnd, MT4InternalMsg, 2, 1);
//
//
//      ChartApplyTemplate(0,"\\Templates\\off"+templateSuffix+".tpl");
//
//
//
//     }


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
//   if(id==CHARTEVENT_OBJECT_CLICK)
//     {
//      Print("The mouse has been clicked on the object with name '"+sparam+"'");
//      ushort sep=StringGetCharacter("_",0);
//
//      string item[];
//      StringSplit(sparam,sep,item);
//      Print("index: "+item[1]);
//      getExitLevels(item[1]);
//      return;
//     }
   if(id==CHARTEVENT_CLICK)
     {
      bool b=ChartSetSymbolPeriod(0,"GBPUSD",3);
      Print("CLICK: "+b);

      int hwnd = WindowHandle(Symbol(), Period());

      if(hwnd != 0)
         Print("Chart window detected "+hwnd);

      int MT4InternalMsg = RegisterWindowMessageA("MetaTrader4_Internal_Message");


      if(hwnd != 0)
         if(PostMessageA(hwnd, WM_COMMAND, 0x822c, 0) == 0)
            hwnd = 0;
      if(hwnd != 0 && MT4InternalMsg != 0)
         PostMessageA(hwnd, MT4InternalMsg, 2, 1);


      //      int clickX=(int)lparam;
      //      int clickY=(int)dparam;
      //      int clickWindow=0;
      //      datetime clickTime;
      //      double clickPrice;
      //      //+------------------------------------------------------------------+
      //      //|                                                                  |
      //      //+------------------------------------------------------------------+
      //      //+------------------------------------------------------------------+
      //      //|                                                                  |
      //      //+------------------------------------------------------------------+
      //      if(ChartXYToTimePrice(0,clickX,clickY,clickWindow,clickTime,clickPrice))
      //         Print("--------------------------------CLICK1:",TimeToStr(clickTime));
      //      RefreshRates();
      //      long id=ChartFirst();
      //      while(id>=0)
      //        {
      //         //--- find appropriate offline chart
      //         if(ChartSymbol(id)==Symbol() && ChartPeriod(id)==Period() && ChartGetInteger(id,CHART_IS_OFFLINE))
      //           {
      //
      //            ChartSetInteger(id,CHART_AUTOSCROLL,true);
      //            ChartSetInteger(id,CHART_SHIFT,true);
      //            ChartNavigate(id,CHART_END);
      //            ChartRedraw(id);
      //            PrintFormat("Chart window [%s,%d] found",Symbol(),Period());
      //            break;
      //           }
      //         //--- enumerate opened charts
      //         Print("charid: "+id);
      //         id=ChartNext(id);
      //        }
      //
      //      ChartApplyTemplate(0,"\\Templates\\off_5.tpl");
      //      WindowRedraw();


     }


  }
//+------------------------------------------------------------------+
void getExitLevels(string index)
  {
   ushort sep=StringGetCharacter("|",0);

   int file_handle=FileOpen("exitPrices.csv",FILE_READ);

   if(file_handle!=INVALID_HANDLE)
     {
      //--- additional variables
      int    str_size;

      string str;
      string item[];

      //--- read data from the file
      uint startT=GetTickCount();
      int tradeIndex=0;
      while(!FileIsEnding(file_handle))
        {
         //--- find out how many symbols are used for writing the time

         str_size=FileReadInteger(file_handle,INT_VALUE);
         str=FileReadString(file_handle,str_size);


         if(StringLen(str)>1)
           {
            StringSplit(str,sep,item);
            if(item[0]==index)
               Print(str);
            //exitLeves.set(StrToInteger(item[0],str);

           }

        }
      //--- close the file
      FileClose(file_handle);
      //Print("Executed: "+(GetTickCount()-startT));
     }
   else
      PrintFormat("File exitPrices.csv not found!");
  }
//+------------------------------------------------------------------+
void createHistoryFile()
  {

   int       ExtHandle=-1;
   ulong    last_fpos=0;
   long     last_volume=0;
   int      periodseconds;
   int      cnt=0;
//---- History header
   int      file_version=401;
   string   c_copyright;

   int      i_digits=Digits;
   int      i_unused[13];
   MqlRates rate;

   int file_handle=FileOpen("endFile",FILE_READ);
   ushort sep=StringGetCharacter("|",0);

   timeFrame=0;
   symbol="";

   if(file_handle!=INVALID_HANDLE)
     {
      //--- additional variables
      int    str_size;

      string str;
      string item[];


      while(!FileIsEnding(file_handle))
        {
         //--- find out how many symbols are used for writing the time

         str_size=FileReadInteger(file_handle,INT_VALUE);
         str=FileReadString(file_handle,str_size);
         //Print(str);

         if(StringLen(str)>1)
           {

            StringSplit(str,sep,item);
            Print("History file on "+item[0]+" + "+item[1]+" has created...");
            timeFrame=StrToInteger(item[1]);
            symbol=item[0];

           }

        }
      FileClose(file_handle);
     }
   else
      PrintFormat("Failed to open %s file, Error code = %d","InpFileName",GetLastError());

   if(timeFrame==0 || StringLen(symbol)==0)
     {
      Print("TimeFrame or Symbol is not set!");
      return;
     }


//---
   ExtHandle=FileOpenHistory(symbol+(string)timeFrame+".hst",FILE_BIN|FILE_WRITE|FILE_SHARE_WRITE|FILE_SHARE_READ|FILE_ANSI);
   if(ExtHandle<0)
      return;
   c_copyright="(C)opyright 2003, MetaQuotes Software Corp.";
   ArrayInitialize(i_unused,0);
//--- write history file header
   FileWriteInteger(ExtHandle,file_version,LONG_VALUE);
   FileWriteString(ExtHandle,c_copyright,64);
   FileWriteString(ExtHandle,symbol,12);
   FileWriteInteger(ExtHandle,timeFrame,LONG_VALUE);
   FileWriteInteger(ExtHandle,i_digits,LONG_VALUE);
   FileWriteInteger(ExtHandle,0,LONG_VALUE);
   FileWriteInteger(ExtHandle,0,LONG_VALUE);
   FileWriteArray(ExtHandle,i_unused,0,13);
//--- write history file
   periodseconds=timeFrame*60;
   rate.spread=0;
   rate.real_volume=0;
//--- normalize open time

   file_handle=FileOpen("log4J_balanceEquity.csv",FILE_READ);


   if(file_handle!=INVALID_HANDLE)
     {
      //--- additional variables
      int    str_size;

      string str;
      string item[];


      while(!FileIsEnding(file_handle))
        {
         //--- find out how many symbols are used for writing the time

         str_size=FileReadInteger(file_handle,INT_VALUE);
         str=FileReadString(file_handle,str_size);
         //Print(str);

         if(StringLen(str)>1)
           {

            StringSplit(str,sep,item);
            //Print(item[0]+" + "+item[3]);
            rate.time=StrToTime(item[0])/periodseconds;
            rate.time*=periodseconds;
            rate.open=StrToDouble(item[1]);
            rate.low=StrToDouble(item[3]);
            rate.high=StrToDouble(item[2]);
            rate.close=StrToDouble(item[4]);
            rate.tick_volume=(long)StrToInteger(item[5]);

            last_fpos=FileTell(ExtHandle);

            FileWriteStruct(ExtHandle,rate);
            cnt++;

           }

        }
      //--- close the file
      FileClose(file_handle);

      FileFlush(ExtHandle);
      FileClose(ExtHandle);
      PrintFormat("%d record(s) written",cnt);
      //Print("Executed: "+(GetTickCount()-startT));
     }
   else
      PrintFormat("Failed to open %s file, Error code = %d","InpFileName",GetLastError());


  }
//+------------------------------------------------------------------+
