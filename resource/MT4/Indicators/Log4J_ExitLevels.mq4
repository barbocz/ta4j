//+------------------------------------------------------------------+
//|                                       Waddah_Attar_Explosion.mq4 |
//|                              Copyright © 2006, Eng. Waddah Attar |
//|                                          waddahattar@hotmail.com |
//+------------------------------------------------------------------+
//----
#property strict
#property  indicator_chart_window
#property  indicator_buffers 2
#property  indicator_color1  Lime
#property  indicator_color2  Red


extern string fileName="log4J_exits.csv";
//----

double stopLoss[],takeProfit[];
int lastStartIndex,lastEndIndex;
//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+


bool firstRunOk=false;

//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+
int OnInit()
  {


   firstRunOk=false;

//----
//IndicatorShortName("SignalScanner");

   SetIndexBuffer(0,takeProfit);SetIndexLabel(0,"takeProfit");
   SetIndexStyle(0,DRAW_LINE,STYLE_DASHDOT,2);

   SetIndexBuffer(1,stopLoss);SetIndexLabel(1,"stopLoss");
   SetIndexStyle(1,DRAW_LINE,STYLE_DASHDOT,2);
   
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






   return(rates_total);
  }
//+------------------------------------------------------------------+
void OnChartEvent(const int id,
                  const long &lparam,
                  const double &dparam,
                  const string &sparam)
  {
//---

   if(id==CHARTEVENT_OBJECT_CLICK)
     {
      //Print("The mouse has been clicked on the object with name '"+sparam+"'");
      ushort sep=StringGetCharacter("_",0);

      string item[];
      StringSplit(sparam,sep,item);
      //Print("index: "+item[1]);
      getExitLevels(item[1]);
      return;
     }
  }
//+------------------------------------------------------------------+
void getExitLevels(string index)
  {
  bool first=true;
   ushort sep=StringGetCharacter("|",0);

   int file_handle=FileOpen("log4J_exits.csv",FILE_READ);

   if(file_handle!=INVALID_HANDLE)
     {
      //--- additional variables
      int    str_size;

      string str;
      string item[];

      //--- read data from the file

      int tradeIndex=0;
      while(!FileIsEnding(file_handle))
        {
         //--- find out how many symbols are used for writing the time

         str_size=FileReadInteger(file_handle,INT_VALUE);
         str=FileReadString(file_handle,str_size);


         if(StringLen(str)>1)
           {
            StringSplit(str,sep,item);
            if(item[0]==index) {
               Print(str);
               if (first) {
               //Print("lastStartIndex "+lastStartIndex+"  lastEndIndex "+lastEndIndex );
                  first=false;
                  while (lastStartIndex>lastEndIndex) {
                     //Print("REset "+lastStartIndex);
                     stopLoss[lastStartIndex]=EMPTY_VALUE;
                     takeProfit[lastStartIndex]=EMPTY_VALUE;
                     lastStartIndex--;
                  }
                  lastStartIndex=iBarShift(NULL,0,StrToTime(item[2]),true)+1;
               }
               lastEndIndex=iBarShift(NULL,0,StrToTime(item[2]),true)+1;
               if (item[4]!="0.0") stopLoss[lastEndIndex]=DoubleToStr(item[4]);
               if (item[3]!="0.0") takeProfit[lastEndIndex]=DoubleToStr(item[3]);
               //Print(iBarShift(NULL,0,StrToTime(item[2]))+" - "+DoubleToStr(item[3]));
               
            //exitLeves.set(StrToInteger(item[0],str);
            }

           }

        }
      //--- close the file
      FileClose(file_handle);

     }
   else
      PrintFormat("File log4J_exits.csv not found!");
  }
//+------------------------------------------------------------------+
