//+------------------------------------------------------------------+
//|                                       Waddah_Attar_Explosion.mq4 |
//|                              Copyright © 2006, Eng. Waddah Attar |
//|                                          waddahattar@hotmail.com |
//+------------------------------------------------------------------+
//----
#property strict
#property  indicator_separate_window
#property  indicator_buffers 3
#property  indicator_level1 100.0;
#property  indicator_color1  clrGold
#property  indicator_color2  clrCyan


extern string fileName="log4J_balanceEquity.csv";




//----


double   balance[],equity[],timeSeriesIndex[];



//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+
struct SignalRecord
  {
   datetime          time;
   short             signals[8];
  };
SignalRecord        signalRecord;

bool firstRunOk=false;

//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+
int OnInit()
  {


   SetIndexBuffer(0,balance);
   SetIndexLabel(0,"balance");
   SetIndexStyle(0,DRAW_LINE,STYLE_SOLID,2);

   SetIndexBuffer(1,equity);
   SetIndexLabel(1,"equity");
   SetIndexStyle(1,DRAW_LINE,STYLE_SOLID,2);

   SetIndexBuffer(2,timeSeriesIndex);
   SetIndexLabel(2,"timeSeriesIndex");
   SetIndexStyle(2,DRAW_NONE);



   IndicatorDigits(5);

   return(0);
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


      firstRunOk=true;
      ushort sep=StringGetCharacter("|",0);

      int file_handle=FileOpen(fileName,FILE_READ);

      if(file_handle!=INVALID_HANDLE)
        {
         //--- additional variables
         int    str_size,index;

         string str;
         string item[];

         //--- read data from the file
         uint startT=GetTickCount();
         while(!FileIsEnding(file_handle))
           {
            //--- find out how many symbols are used for writing the time

            str_size=FileReadInteger(file_handle,INT_VALUE);
            str=FileReadString(file_handle,str_size);


            if(StringLen(str)>1)
              {

               StringSplit(str,sep,item);


               //int ind=iBarShift(NULL,0,StrToTime(item[1]),true)+1;
               //Print(item[1]+" - "+ind+" - "+item[2]);

               index=iBarShift(NULL,0,StrToTime(item[0]),true);
               //Print(item[0]+", "+item[6]+", "+item[7]+"  "+index);
               if(index>-1)
                 {

                  balance[index]=StrToDouble(item[6]);

                  equity[index]=StrToDouble(item[7]);

                  timeSeriesIndex[index]=StrToDouble(item[8]);
                 }



              }

           }
         //--- close the file
         FileClose(file_handle);
         //Print("Executed: "+(GetTickCount()-startT));
         //for(int i=Bars-2; i>-1; i--)
         //  {
         //   if(balance[i]==EMPTY_VALUE)
         //      balance[i]=balance[i+1];
         //   if(equity[i]==EMPTY_VALUE)
         //      equity[i]=equity[i+1];
         //  }
        }
      else
         PrintFormat("File "+fileName+" not found!");


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
         if(clickWindow==0)
           {
            
            Print("barindex: "+timeSeriesIndex[iBarShift(NULL,0,clickTime)]);
           
            //Print("--------------------------------CLICK3:",TimeToStr(clickTime),"  ",clickX,"   ",clickWindow);
           }
        }
     }
  }
//+------------------------------------------------------------------+
