//+------------------------------------------------------------------+
//|                                       Waddah_Attar_Explosion.mq4 |
//|                              Copyright © 2006, Eng. Waddah Attar |
//|                                          waddahattar@hotmail.com |
//+------------------------------------------------------------------+
//----
#property strict
#property  indicator_chart_window
#property  indicator_buffers 0

#include <Mql4Lib\Collection\HashMap.mqh>

HashMap<int,int>orders;  // index,barshift
int tradeIndex=0;
extern string fileName="log4J_trades.csv";
int firstVisbileBar=0;
//----

long chartId;
//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+


bool firstRunOk=false;

//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+
int OnInit()
  {

   chartId=ChartID();

   firstRunOk=false;
   ObjectsDeleteAll();
//----
//IndicatorShortName("SignalScanner");
   IndicatorDigits(5);



   return(0);
  }

//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+
void OnDeinit(const int reason)
  {
   ObjectsDeleteAll();
  }
//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+
int OnCalculate(const int rates_total,
                const int prev_calculated,
                const datetime &time[],
                const double &open[],
                const double &high[],
                const double &low[],
                const double &close[],
                const long &tick_volume[],
                const long &volume[],
                const int &spread[])
  {



   if(!firstRunOk)
     {
      firstVisbileBar=ChartGetInteger(chartId,CHART_FIRST_VISIBLE_BAR,0);

      firstRunOk=true;
      ushort sep=StringGetCharacter("|",0);

      if(FileIsExist("log4J_tradeSignals.csv"))
         fileName="log4J_tradeSignals.csv";

      int file_handle=FileOpen(fileName,FILE_READ);

      if(file_handle!=INVALID_HANDLE)
        {
         //--- additional variables
         int    str_size;

         string str;
         string item[];

         //--- read data from the file
         uint startT=GetTickCount();
         tradeIndex=0;
         while(!FileIsEnding(file_handle))
           {
            //--- find out how many symbols are used for writing the time

            str_size=FileReadInteger(file_handle,INT_VALUE);
            str=FileReadString(file_handle,str_size);


            if(StringLen(str)>1)
              {

               StringSplit(str,sep,item);

               //               ObjectCreate(0,"LINE",OBJ_TREND,0,StrToTime("2019.11.01 21:30"),1.116,StrToTime("2019.11.01 23:03"),1.1167);
               //ObjectSetInteger(0,"LINE",OBJPROP_RAY_RIGHT,false);
               //ObjectSetInteger(0,"LINE",OBJPROP_COLOR,clrAqua);

               string objectName="TRADELINE_"+item[0];
               ObjectCreate(0,objectName,OBJ_TREND,0,StrToTime(item[2]),StrToDouble(item[3]),StrToTime(item[4]),StrToDouble(item[5]));
               ObjectSetInteger(0,objectName,OBJPROP_RAY_RIGHT,false);
               if(item[1]=="BUY")
                  ObjectSetInteger(0,objectName,OBJPROP_COLOR,clrLime);
               else
                  ObjectSetInteger(0,objectName,OBJPROP_COLOR,clrFireBrick);
               ObjectSetInteger(0,objectName,OBJPROP_WIDTH,4);

               orders.set(tradeIndex,iBarShift(NULL,0,StrToTime(item[2])));

               tradeIndex++;



              }

           }
         //--- close the file
         FileClose(file_handle);
         Print("Executed: "+(GetTickCount()-startT));
        }
      else
         PrintFormat("File "+fileName+" not found!");
      tradeIndex--;


     }
//----


   return(rates_total);
  }
//+------------------------------------------------------------------+
void OnChartEvent(const int id,
                  const long &lparam,
                  const double &dparam,
                  const string &sparam)
  {
//---

   if(id==CHARTEVENT_CLICK)
     {

      int clickX=(int)lparam;
      int clickY=(int)dparam;
      int clickWindow=0;
      datetime clickTime;
      double clickPrice;
      //+------------------------------------------------------------------+
      //|                                                                  |
      //+------------------------------------------------------------------+
      //+------------------------------------------------------------------+
      //|                                                                  |
      //+------------------------------------------------------------------+
      if(ChartXYToTimePrice(0,clickX,clickY,clickWindow,clickTime,clickPrice))
        {
         //Print("clickWindow "+clickY+"  h? "+ChartGetInteger(0,CHART_HEIGHT_IN_PIXELS));
         if(clickWindow==0 && ChartGetInteger(0,CHART_HEIGHT_IN_PIXELS)-clickY<50)
           {
            firstVisbileBar=ChartGetInteger(chartId,CHART_FIRST_VISIBLE_BAR,0);

            while(tradeIndex>-1 && orders.get(tradeIndex,0)< firstVisbileBar)
              {
               tradeIndex--;
              }


            ChartNavigate(chartId,CHART_BEGIN,Bars-orders.get(tradeIndex,0)-30);


            //else ChartNavigate(chartId,CHART_END,1000);


           }
        }

     }
  }
//+------------------------------------------------------------------+
