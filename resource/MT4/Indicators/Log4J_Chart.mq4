//+------------------------------------------------------------------+
//|                                       Waddah_Attar_Explosion.mq4 |
//|                              Copyright © 2006, Eng. Waddah Attar |
//|                                          waddahattar@hotmail.com |
//+------------------------------------------------------------------+
//----
#property strict
#property  indicator_chart_window
#property  indicator_buffers 20

extern string fileName="log4J_Indicator_0.csv";

//----

double   b1[],b2[],b3[],b4[],b5[],b6[],b7[],b0[],b8[],b9[],b10[],b11[],b12[],b13[],b14[],b15[],b16[],b17[],b18[],b19[],b20[];


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
   for(int i=0; i<20; i++)
     {
      SetIndexLabel(i, NULL);
     }

//----
//IndicatorShortName("SignalScanner");
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

   int bufferIndex,barIndex;

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

               if(ArraySize(item)==5)
                 {
                  //Print(item[0]+","+item[1]+","+item[2]+","+item[3]+","+item[4]);
                  color str_color=StringToColor(item[2]+","+item[3]+","+item[4]);

                  bufferIndex=StrToInteger(item[0]);

                  switch(bufferIndex)
                    {
                     case 0:
                        SetIndexBuffer(bufferIndex,b0);
                        break;
                     case 1:
                        SetIndexBuffer(bufferIndex,b1);
                        break;
                     case 2:
                        SetIndexBuffer(bufferIndex,b2);
                        break;
                     case 3:
                        SetIndexBuffer(bufferIndex,b3);
                        break;
                     case 4:
                        SetIndexBuffer(bufferIndex,b4);
                        break;
                     case 5:
                        SetIndexBuffer(bufferIndex,b5);
                        break;
                     case 6:
                        SetIndexBuffer(bufferIndex,b6);
                        break;
                     case 7:
                        SetIndexBuffer(bufferIndex,b7);
                        break;
                     case 8:
                        SetIndexBuffer(bufferIndex,b8);
                        break;
                     case 9:
                        SetIndexBuffer(bufferIndex,b9);
                        break;
                     case 10:
                        SetIndexBuffer(bufferIndex,b10);
                        break;
                     case 11:
                        SetIndexBuffer(bufferIndex,b11);
                        break;
                     case 12:
                        SetIndexBuffer(bufferIndex,b12);
                        break;
                     case 13:
                        SetIndexBuffer(bufferIndex,b13);
                        break;
                     case 14:
                        SetIndexBuffer(bufferIndex,b14);
                        break;
                     case 15:
                        SetIndexBuffer(bufferIndex,b15);
                        break;
                     case 16:
                        SetIndexBuffer(bufferIndex,b16);
                        break;
                     case 17:
                        SetIndexBuffer(bufferIndex,b17);
                        break;
                     case 18:
                        SetIndexBuffer(bufferIndex,b18);
                        break;
                     case 19:
                        SetIndexBuffer(bufferIndex,b19);
                        break;
                     case 20:
                        SetIndexBuffer(bufferIndex,b20);
                     default:
                        break;
                    }
                  SetIndexLabel(bufferIndex,item[1]);
                  SetIndexStyle(bufferIndex,DRAW_LINE,EMPTY,1,str_color);


                 }
               else
                 {
                  //int ind=iBarShift(NULL,0,StrToTime(item[1]),true)+1;
                  //Print(item[1]+" - "+ind+" - "+item[2]);
                  bufferIndex=StrToInteger(item[0]);
                  barIndex=iBarShift(NULL,0,StrToTime(item[1]),true);
                  if(barIndex<0 || barIndex>Bars-2)
                     continue;
                  barIndex++;

                  switch(bufferIndex)
                    {
                     case 0:
                        b0[barIndex]=StrToDouble(item[2]);
                        break;
                     case 1:
                        b1[barIndex]=StrToDouble(item[2]);
                        break;
                     case 2:
                        b2[barIndex]=StrToDouble(item[2]);
                        break;
                     case 3:
                        b3[barIndex]=StrToDouble(item[2]);
                        break;
                     case 4:
                        b4[barIndex]=StrToDouble(item[2]);
                        break;
                     case 5:
                        b5[barIndex]=StrToDouble(item[2]);
                        break;
                     case 6:
                        b6[barIndex]=StrToDouble(item[2]);
                        break;
                     case 7:
                        b7[barIndex]=StrToDouble(item[2]);
                        break;
                     case 8:
                        b8[barIndex]=StrToDouble(item[2]);
                        break;
                     case 9:
                        b9[barIndex]=StrToDouble(item[2]);
                        break;
                     case 10:
                        b10[barIndex]=StrToDouble(item[2]);
                        break;
                     case 11:
                        b11[barIndex]=StrToDouble(item[2]);
                        break;
                     case 12:
                        b12[barIndex]=StrToDouble(item[2]);
                        break;
                     case 13:
                        b13[barIndex]=StrToDouble(item[2]);
                        break;
                     case 14:
                        b14[barIndex]=StrToDouble(item[2]);
                        break;
                     case 15:
                        b15[barIndex]=StrToDouble(item[2]);
                        break;
                     case 16:
                        b16[barIndex]=StrToDouble(item[2]);
                        break;
                     case 17:
                        b17[barIndex]=StrToDouble(item[2]);
                        break;
                     case 18:
                        b18[barIndex]=StrToDouble(item[2]);
                        break;
                     case 19:
                        b19[barIndex]=StrToDouble(item[2]);
                        break;
                     case 20:
                        b20[barIndex]=StrToDouble(item[2]);
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
   return;
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
         Print("--------------------------------CLICK1:",TimeToStr(clickTime));
     }
  }
//+------------------------------------------------------------------+
