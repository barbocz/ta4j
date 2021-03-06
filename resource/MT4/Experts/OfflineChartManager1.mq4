//+------------------------------------------------------------------+
//|                                          OfflineChartManager.mq4 |
//|                        Copyright 2014, MetaQuotes Software Corp. |
//|                                              http://www.mql5.com |
//+------------------------------------------------------------------+
#property copyright "Copyright 2014, MetaQuotes Software Corp."
#property link      "http://www.mql5.com"
#property version   "1.00"
#property strict

#import "user32.dll"
int PostMessageA(int hWnd, int Msg, int wParam, int lParam);
int GetAncestor(int hWnd, int gaFlags);
int GetLastActivePopup(int hWnd);
int GetDlgItem(int hDlg, int nIDDlgItem);
int RegisterWindowMessageA(string lpString);
#import

#import "kernel32.dll"
int  FindFirstFileW(string Path, int& Answer[]);
bool FindNextFileA(int handle, int& Answer[]);
bool FindClose(int handle);
#import

#define WM_COMMAND 0x0111
#define WM_KEYDOWN 0x0100

#define VK_DOWN 0x28

#define BM_CLICK 0x00F5

#define GA_ROOT 2

#define PAUSE 100

#include <WinUser32.mqh>
#include <stdlib.mqh>

#include <Arrays\ArrayString.mqh>
#include <Arrays\ArrayObj.mqh>

CArrayString *historyFiles;
long lastModified;
ushort sep;
int timeFrame=3;
string symbol="EURUSD";
string STRATEGY_ID,TRADE_NUMBER,PROFITABLE_TRADES_RATIO,EQUITY_MINIMUM,BALANCE_DRAWDOWN,TOTAL_PROFIT_PER_MONTH,OPEN_PROFIT;

//+------------------------------------------------------------------+
//| Expert initialization function                                   |
//+------------------------------------------------------------------+
int OnInit()
  {
//--- create timer
   ObjectsDeleteAll();
   createLabel("status","Offline ChartManager is running...",20,40);
   lastModified=FileGetInteger("endFile",FILE_MODIFY_DATE);
   sep=StringGetCharacter("|",0);

   EventSetTimer(1);


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
   ObjectsDeleteAll();
   delete(historyFiles);

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
   if(FileGetInteger("endFile",FILE_MODIFY_DATE)!=lastModified)
     {

      lastModified=FileGetInteger("endFile",FILE_MODIFY_DATE);



      getSymbolAndPeriod();  // endFile-ból kiolvassuk a symbol-t és a timeframe-et
      

      createHistoryFile();   // az aktuális log4J_balanceEquity.csv-ből legyártjuk a history file-t


      //ChartRedraw();
    

      //            long offlineChartId=-1;
      //      long currChart=ChartFirst();
      //      int ci=0;
      //
      //      while(ci<CHARTS_MAX) // We have certainly no more than CHARTS_MAX open charts
      //        {
      //
      //         currChart=ChartNext(currChart); // We have received a new chart from the previous
      //         //Print(ChartSymbol(currChart)+"+ "+ChartPeriod(currChart));
      //
      //         if(currChart==-1) break;
      //
      //         if(ChartGetInteger(currChart,CHART_IS_OFFLINE))
      //           {
      //            Print(ChartSymbol(currChart)+"*** "+ChartPeriod(currChart)+"   "+ChartGetInteger(currChart,CHART_IS_OFFLINE));
      //            offlineChartId=currChart;
      //
      //           }
      //         ci++;// Do not forget to increase the counter
      //        }
      //      if (offlineChartId>-1) ChartApplyTemplate(offlineChartId,"\\Templates\\offline.tpl");



      //setTemplate();
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
   if(id==CHARTEVENT_CLICK)
     {
      offlineChartClose();
      //GetHistoryFiles();
      //getSymbolAndPeriod();
      //Print("Searching for "+symbol+" on "+IntegerToString(timeFrame));
      //int pos=historyFiles.Search(getKey(symbol,IntegerToString(timeFrame)));
      //Print("GetHistoryFiles() : "+pos);
      //OpenOfflineChartbyNum(pos);
      //setTemplate();
     }

  }
//+------------------------------------------------------------------+
//+------------------------------------------------------------------+
string BuffToString(int& Buffer[])
  {
   string Str = "";
   int Pos = 11;

   while(Pos < 75)
     {
      while(Buffer[Pos] != 0)
        {
         Str = Str + CharToStr(Buffer[Pos] & 0xFF);

         Buffer[Pos] /= 0x100;
        }

      Pos++;
     }

   return(Str);
  }
//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+
void GetHistoryFiles()
  {

   historyFiles=new CArrayString;

   string fileName,periodPart,symbolPart;
   int Buffer[79];


   int handle = FindFirstFileW(TerminalInfoString(TERMINAL_DATA_PATH)+"\\history\\"  + AccountServer() + "\\*.hst", Buffer);
//fileName="AUDCAD1.hst";
//symbolPart=StringSubstr(fileName,0,6);
//periodPart=StringSubstr(fileName,6);
//periodPart=StringSubstr(periodPart,0,StringFind(periodPart,"."));
//historyFiles.Add(getKey(symbolPart,periodPart));


   while(FindNextFileA(handle, Buffer))
     {
      fileName=BuffToString(Buffer);
      //Print(fileName);
      symbolPart=StringSubstr(fileName,0,6);
      periodPart=StringSubstr(fileName,6);
      periodPart=StringSubstr(periodPart,0,StringFind(periodPart,"."));

      historyFiles.Add(getKey(symbolPart,periodPart));
     }


   historyFiles.Sort();
//int pos=historyFiles.Search(getKey(symbol,IntegerToString(period)));

//for(int i=0;i<160;i++)
//  {
//   Print(i+". "+historyFiles.At(i));
//  }


//if(handle > 0) FindClose(handle);

   Print("Total:::::::::::::: "+historyFiles.Total());
  }
//+------------------------------------------------------------------+
string getKey(string symbol,string period)
  {
   string preZeros;
   StringInit(preZeros,5-StringLen(period),'0');
   return symbol+preZeros+period;
  }
//+------------------------------------------------------------------+
void getSymbolAndPeriod()
  {

   int file_handle=FileOpen("endFile",FILE_READ);


   timeFrame=0;
   symbol="";

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
            if(lineNumber==0)
              {
               Print("Data on "+item[0]+" + "+item[1]+" was created...");
               timeFrame=StrToInteger(item[1]);
               symbol=item[0];
              }
            else
               if(lineNumber==1)
                 {
                  STRATEGY_ID=item[0];
                  TRADE_NUMBER=item[1];
                  PROFITABLE_TRADES_RATIO=item[2];
                  EQUITY_MINIMUM=item[3];
                  BALANCE_DRAWDOWN=item[4];
                  TOTAL_PROFIT_PER_MONTH=item[5];
                  OPEN_PROFIT=item[6];
                 }

           }
         lineNumber++;

        }
      FileClose(file_handle);
      Print("STRATEGY_ID "+STRATEGY_ID+" , TRADE_NUMBER "+TRADE_NUMBER+"  TOTAL_PROFIT_PER_MONTH "+TOTAL_PROFIT_PER_MONTH,"  OPEN_PROFIT "+OPEN_PROFIT);
     }
   else
      PrintFormat("Failed to open %s file, Error code = %d","InpFileName",GetLastError());

   if(timeFrame==0 || StringLen(symbol)==0)
     {
      Print("TimeFrame or Symbol is not set!");
      return;
     }
  }
//+------------------------------------------------------------------+
int OpenOfflineList()
  {
   int hwnd = WindowHandle(Symbol(), Period());

   hwnd = GetAncestor(hwnd, GA_ROOT);

   PostMessageA(hwnd, WM_COMMAND, 33053, 0);
   Sleep(PAUSE);

   hwnd = GetLastActivePopup(hwnd);

   return(hwnd);
  }
//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+


//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+
void OpenOfflineChartbyNum(int ChartPos)
  {
   int hwnd1 = OpenOfflineList();
   int hwnd2 = GetDlgItem(hwnd1, 1);

   hwnd1 = GetDlgItem(hwnd1, 0x487);

   while(ChartPos >= 0)
     {
      PostMessageA(hwnd1, WM_KEYDOWN, VK_DOWN, 0);

      ChartPos--;
     }

   Sleep(PAUSE);

   PostMessageA(hwnd2, BM_CLICK, 0, 0);

   return;
  }
//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+

//+------------------------------------------------------------------+
//|                                                                  |
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
//|                                                                  |
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

   int      i_digits=5;
   int      i_unused[13];
   MqlRates rate;


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

   int file_handle=FileOpen("log4J_balanceEquity.csv",FILE_READ);


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
void setTemplate()
  {
   long offlineChartId=-1;
   long currChart=ChartFirst();
   int ci=0;
   string templateSuffix="";
   Print(ChartSymbol(currChart)+"+ "+ChartPeriod(currChart));
   while(ci<CHARTS_MAX) // We have certainly no more than CHARTS_MAX open charts
     {

      currChart=ChartNext(currChart); // We have received a new chart from the previous
      Print(ChartSymbol(currChart)+"+ "+ChartPeriod(currChart));

      if(currChart==-1)
         break;        // Reached the end of the charts list
      if(ChartSymbol(currChart)==symbol && ChartPeriod(currChart)==timeFrame && ChartGetInteger(currChart,CHART_IS_OFFLINE))
        {
         Print(ChartSymbol(currChart)+"*** "+ChartPeriod(currChart)+"   "+ChartGetInteger(currChart,CHART_IS_OFFLINE));
         offlineChartId=currChart;
        }
      ci++;// Do not forget to increase the counter
     }

   if(offlineChartId>-1)
     {
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

      ChartApplyTemplate(offlineChartId,"\\Templates\\off"+templateSuffix+".tpl");
     }

//      string templateSuffix="";
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
//
//      int hwnd = WindowHandle(symbol, timeFrame);
//
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
//
//
//      ChartApplyTemplate(0,"\\Templates\\off"+templateSuffix+".tpl");

  }
//+------------------------------------------------------------------+
void offlineChartClose()
  {


   long offlineChartId=-1;
   long currChart=ChartFirst();
   int ci=0;

   while(ci<CHARTS_MAX) // We have certainly no more than CHARTS_MAX open charts
     {

      currChart=ChartNext(currChart); // We have received a new chart from the previous
      Print(ChartSymbol(currChart)+"+ "+ChartPeriod(currChart));

      if(currChart==-1)
         break;        // Reached the end of the charts list
      if(ChartGetInteger(currChart,CHART_IS_OFFLINE) && ChartPeriod(currChart)==timeFrame && ChartSymbol(currChart)==symbol)
        {
         //Print(ChartSymbol(currChart)+"*** "+ChartPeriod(currChart)+"   "+ChartGetInteger(currChart,CHART_IS_OFFLINE));
         offlineChartId=currChart;
         break;
        }
      ci++;// Do not forget to increase the counter
     }
   if(offlineChartId>-1)
      ChartClose(offlineChartId);
   RefreshRates();
   WindowRedraw();
  }
//+------------------------------------------------------------------+
