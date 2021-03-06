//+------------------------------------------------------------------+
//|                                       Waddah_Attar_Explosion.mq4 |
//|                              Copyright © 2006, Eng. Waddah Attar |
//|                                          waddahattar@hotmail.com |
//+------------------------------------------------------------------+
//----
#property strict
#property  indicator_separate_window
#property  indicator_buffers 10
#import "user32.dll"
int PostMessageA(int hWnd, int Msg, int wParam, int lParam);
int RegisterWindowMessageA(string lpString);
#import

#include <WinUser32.mqh>
#include <stdlib.mqh>


extern string fileName="log4J_Indicator_.csv";

//----

double   b1[],b2[],b3[],b4[],b5[],b6[],b7[],b0[],b8[],b9[],b10[];


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
   for(int i=0; i<10; i++)
     {
      SetIndexLabel(i, NULL);
     }

//----
   IndicatorShortName("SUBCHART");
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

                  int bufferIndex=StrToInteger(item[0]);

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
                     default:
                        break;
                    }
                  IndicatorShortName(item[1]);
                  SetIndexLabel(bufferIndex,item[1]);
                  SetIndexStyle(bufferIndex,DRAW_LINE,EMPTY,1,str_color);


                 }
               else
                 {
                  //int ind=iBarShift(NULL,0,StrToTime(item[1]),true)+1;
                  //Print(item[1]+" - "+ind+" - "+item[2]);
                  int bufferIndex=StrToInteger(item[0]);

                  switch(bufferIndex)
                    {
                     case 0:
                        b0[StrToInteger(iBarShift(NULL,0,StrToTime(item[1]),true))+1]=StrToDouble(item[2]);
                        //if(fileName=="log4J_Indicator_4.csv")
                        //   Print((StrToInteger(iBarShift(NULL,0,StrToTime(item[1]),true))+1)+". "+item[1]+"  -  "+item[2]);

                        break;
                     case 1:
                        b1[StrToInteger(iBarShift(NULL,0,StrToTime(item[1]),true))+1]=StrToDouble(item[2]);
                        break;
                     case 2:
                        b2[StrToInteger(iBarShift(NULL,0,StrToTime(item[1]),true))+1]=StrToDouble(item[2]);
                        break;
                     case 3:
                        b3[StrToInteger(iBarShift(NULL,0,StrToTime(item[1]),true))+1]=StrToDouble(item[2]);
                        break;
                     case 4:
                        b4[StrToInteger(iBarShift(NULL,0,StrToTime(item[1]),true))+1]=StrToDouble(item[2]);
                        break;
                     case 5:
                        b5[StrToInteger(iBarShift(NULL,0,StrToTime(item[1]),true))+1]=StrToDouble(item[2]);
                        break;
                     case 6:
                        b6[StrToInteger(iBarShift(NULL,0,StrToTime(item[1]),true))+1]=StrToDouble(item[2]);
                        break;
                     case 7:
                        b7[StrToInteger(iBarShift(NULL,0,StrToTime(item[1]),true))+1]=StrToDouble(item[2]);
                        break;
                     case 8:
                        b8[StrToInteger(iBarShift(NULL,0,StrToTime(item[1]),true))+1]=StrToDouble(item[2]);
                        break;
                     case 9:
                        b9[StrToInteger(iBarShift(NULL,0,StrToTime(item[1]),true))+1]=StrToDouble(item[2]);
                        break;
                     case 10:
                        b10[StrToInteger(iBarShift(NULL,0,StrToTime(item[1]),true))+1]=StrToDouble(item[2]);
                        break;
                     default:
                        break;
                    }



                 }

              }

           }
         //--- close the file
         FileClose(file_handle);
         RefreshRates();
         ChartRedraw();
         //Print("Executed: "+(GetTickCount()-startT));
        }
      else
        {
         PrintFormat("File "+fileName+" not found!");
         IndicatorSetInteger(INDICATOR_HEIGHT,0);
         ChartRedraw(0);
         WindowRedraw();
         RefreshRates();
        }


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

//   if(id==CHARTEVENT_CLICK && fileName=="log4J_Indicator_4.csv")
//     {
//      RefreshRates();
//      long chart_id=ChartID();
//      //Print("********************* "+chart_id+"  "+Symbol()+Period());
//      ChartSetInteger(chart_id,CHART_AUTOSCROLL,true);
//      ChartSetInteger(chart_id,CHART_SHIFT,true);
//      ChartNavigate(chart_id,CHART_END);
//      ChartRedraw(chart_id);
//      ChartSetSymbolPeriod(chart_id,Symbol(),Period());
//
//      WindowRedraw();
//      OnInit();
//      //Print(fileName);
//      //for(int i=0; i<Bars; i++)
//      //  {
//      //   Print(i+". "+TimeToString(iTime(NULL,0,i))+"  -  "+b0[i]);
//      //  }
//
//      //              int hwnd = WindowHandle(Symbol(), Period());
//      //
//      //      if(hwnd != 0)
//      //         Print("Chart window detected "+hwnd);
//      //
//      //      int MT4InternalMsg = RegisterWindowMessageA("MetaTrader4_Internal_Message");
//      //
//      //
//      //      if(hwnd != 0)
//      //         if(PostMessageA(hwnd, WM_COMMAND, 0x822c, 0) == 0)
//      //            hwnd = 0;
//      //      if(hwnd != 0 && MT4InternalMsg != 0)
//      //         PostMessageA(hwnd, MT4InternalMsg, 2, 1);
//      //                  ChartRedraw(0);
//      //         WindowRedraw();
//      //         RefreshRates();
//      //Print("HERERE");
//      //IndicatorSetInteger(INDICATOR_HEIGHT,4);
//      //ChartRedraw();
//      //WindowRedraw();
//      //int clickX=(int)lparam;
//      //int clickY=(int)dparam;
//      //int clickWindow=0;
//      //datetime clickTime;
//      //double clickPrice;
//      ////+------------------------------------------------------------------+
//      ////|                                                                  |
//      ////+------------------------------------------------------------------+
//      ////+------------------------------------------------------------------+
//      ////|                                                                  |
//      ////+------------------------------------------------------------------+
//      //if(ChartXYToTimePrice(0,clickX,clickY,clickWindow,clickTime,clickPrice))
//      //   Print("--------------------------------CLICK1:",TimeToStr(clickTime));
//     }
  }
//+------------------------------------------------------------------+
