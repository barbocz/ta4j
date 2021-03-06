//+------------------------------------------------------------------+
//|                                       Waddah_Attar_Explosion.mq4 |
//|                              Copyright © 2006, Eng. Waddah Attar |
//|                                          waddahattar@hotmail.com |
//+------------------------------------------------------------------+
//----
#property strict
#property  indicator_separate_window
#property  indicator_buffers 16
#property  indicator_color1  Lime
#property  indicator_color2  Lime
#property  indicator_color3  Lime
#property  indicator_color4  Lime
#property  indicator_color5  Lime
#property  indicator_color6  Lime
#property  indicator_color7  Lime
#property  indicator_color8  Lime

#property  indicator_color9  Red
#property  indicator_color10  Red
#property  indicator_color11  Red
#property  indicator_color12  Red
#property  indicator_color13  Red
#property  indicator_color14  Red
#property  indicator_color15  Red
#property  indicator_color16  Red
//----


double   entry0[],entry1[],entry2[],entry3[],entry4[],entry5[],entry6[],entry7[];
double   exit0[],exit1[],exit2[],exit3[],exit4[],exit5[],exit6[],exit7[];

string ruleItems[16];

#property  indicator_minimum -1.0
#property  indicator_maximum 18.0
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
string fileName="log4J_BuyRules.csv";
//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+
int OnInit()
  {

   for(int i=0; i<16; i++)
     {
      ruleItems[i]="";
     }
   firstRunOk=false;
   for(int i=0; i<16; i++)
     {
      SetIndexLabel(i, NULL);
     }

   IndicatorShortName("Buy Entry-Exit");
   IndicatorDigits(0);

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
         int    str_size;

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

               if(ArraySize(item)==3)
                 {
                  //Print(item[0]+","+item[1]+","+item[2]);

                  int bufferIndex=StrToInteger(item[0]);

                  switch(bufferIndex)
                    {
                     case 0:
                        SetIndexBuffer(bufferIndex,entry0);
                        break;
                     case 1:
                        SetIndexBuffer(bufferIndex,entry1);
                        break;
                     case 2:
                        SetIndexBuffer(bufferIndex,entry2);
                        break;
                     case 3:
                        SetIndexBuffer(bufferIndex,entry3);
                        break;
                     case 4:
                        SetIndexBuffer(bufferIndex,entry4);
                        break;
                     case 5:
                        SetIndexBuffer(bufferIndex,entry5);
                        break;
                     case 6:
                        SetIndexBuffer(bufferIndex,entry6);
                        break;
                     case 7:
                        SetIndexBuffer(bufferIndex,entry7);
                        break;

                     case 8:
                        SetIndexBuffer(bufferIndex,exit0);
                        break;
                     case 9:
                        SetIndexBuffer(bufferIndex,exit1);
                        break;
                     case 10:
                        SetIndexBuffer(bufferIndex,exit2);
                        break;
                     case 11:
                        SetIndexBuffer(bufferIndex,exit3);
                        break;
                     case 12:
                        SetIndexBuffer(bufferIndex,exit4);
                        break;
                     case 13:
                        SetIndexBuffer(bufferIndex,exit5);
                        break;
                     case 14:
                        SetIndexBuffer(bufferIndex,exit6);
                        break;
                     case 15:
                        SetIndexBuffer(bufferIndex,exit7);
                        break;
                     default:
                        break;
                    }
                  SetIndexLabel(bufferIndex,IntegerToString(bufferIndex));
                  ruleItems[bufferIndex]=item[2];

                  SetIndexStyle(bufferIndex,DRAW_ARROW,STYLE_SOLID);
                  SetIndexArrow(bufferIndex,110);
                  IndicatorSetDouble(INDICATOR_MAXIMUM,(double)bufferIndex+1.0);


                 }
               else
                 {
                  //int ind=iBarShift(NULL,0,StrToTime(item[1]),true)+1;
                  //Print(item[1]+" - "+ind+" - "+item[2]);
                  int bufferIndex=StrToInteger(item[0]);

                  switch(bufferIndex)
                    {
                     case 0:
                        entry0[iBarShift(NULL,0,StrToTime(item[1]),true)+1]=0.0;
                        break;
                     case 1:
                        entry1[iBarShift(NULL,0,StrToTime(item[1]),true)+1]=1.0;
                        break;
                     case 2:
                        entry2[iBarShift(NULL,0,StrToTime(item[1]),true)+1]=2.0;
                        break;
                     case 3:
                        entry3[iBarShift(NULL,0,StrToTime(item[1]),true)+1]=3.0;
                        break;
                     case 4:
                        entry4[iBarShift(NULL,0,StrToTime(item[1]),true)+1]=4.0;
                        break;
                     case 5:
                        entry5[iBarShift(NULL,0,StrToTime(item[1]),true)+1]=5.0;
                        break;
                     case 6:
                        entry6[iBarShift(NULL,0,StrToTime(item[1]),true)+1]=6.0;
                        break;
                     case 7:
                        entry7[iBarShift(NULL,0,StrToTime(item[1]),true)+1]=7.0;
                        break;

                     case 8:
                        exit0[iBarShift(NULL,0,StrToTime(item[1]),true)+1]=0.0;
                        break;
                     case 9:
                        exit1[iBarShift(NULL,0,StrToTime(item[1]),true)+1]=1.0;
                        break;
                     case 10:
                        exit2[iBarShift(NULL,0,StrToTime(item[1]),true)+1]=2.0;
                        break;
                     case 11:
                        exit3[iBarShift(NULL,0,StrToTime(item[1]),true)+1]=3.0;
                        break;
                     case 12:
                        exit4[iBarShift(NULL,0,StrToTime(item[1]),true)+1]=4.0;
                        break;
                     case 13:
                        exit5[iBarShift(NULL,0,StrToTime(item[1]),true)+1]=5.0;
                        break;
                     case 14:
                        exit6[iBarShift(NULL,0,StrToTime(item[1]),true)+1]=6.0;
                        break;
                     case 15:
                        exit7[iBarShift(NULL,0,StrToTime(item[1]),true)+1]=7.0;
                        break;

                     default:
                        break;
                    }



                 }

              }

           }
         //--- close the file
         FileClose(file_handle);
         //Print("Executed: "+(GetTickCount()-startT));
        }
      else
         PrintFormat("Failed to open %s file, Error code = %d","InpFileName",GetLastError());

     }



   return(rates_total);
  }
//+------------------------------------------------------------------+
void OnChartEvent(const int id,
                  const long &lparam,
                  const double &dparam,
                  const string &sparam)
  {

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
      ChartXYToTimePrice(0,clickX,clickY,clickWindow,clickTime,clickPrice);
      //Print("--------------------------------CLICK BUY:",clickWindow);
      if(clickWindow==1)
        {
         int index=(int)MathRound(clickPrice);
         if (index>ArraySize(ruleItems)-1 || index<0) return;
         if(StringLen(ruleItems[index])>0)
            Print(index+". "+ruleItems[index]);
         else
           {
            for(int i=0; i<16; i++)
              {
               if(StringLen(ruleItems[i])>0)
                  Print(i+".. "+ruleItems[i]);
              }
                Print("-------------------------------------------------------------");
           }
        }

     }
  }
//+------------------------------------------------------------------+
