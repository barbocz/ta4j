//+------------------------------------------------------------------+
//|                                                     MyMurray.mq4 |
//|                        Copyright 2014, MetaQuotes Software Corp. |
//|                                              http://www.mql5.com |
//+------------------------------------------------------------------+

2019.03.06 20:21:35.353	Murray EURUSD,M5: mml[0]: 1.12762451
2019.03.06 20:21:35.353	Murray EURUSD,M5: dmml: 0.00076294
2019.03.06 20:21:35.353	Murray EURUSD,M5: finalL: 1.12915039
2019.03.06 20:21:35.353	Murray EURUSD,M5: y1: 0, y2: 0, y3: 0, y4: 0, y5: 0, y6: 1.12915039
2019.03.06 20:21:35.353	Murray EURUSD,M5: x1: 0, x2: 0, x3: 0, x4: 0, x5: 0, x6: 1.13525391
2019.03.06 20:21:35.353	Murray EURUSD,M5: 3*(mx-mn)/16+mn: 1.1302948   9*(mx-mn)/16+mn: 1.13258362   x2:0
2019.03.06 20:21:35.353	Murray EURUSD,M5: mn: 1.12915039   mx: 1.13525391
2019.03.06 20:21:35.353	Murray EURUSD,M5: sum: 8   octave: 0.00610352
2019.03.06 20:21:35.353	Murray EURUSD,M5: fractal: 1.5625   range: 0.00364
2019.03.06 20:21:35.353	Murray EURUSD,M5: v1: 1.13091   v2: 1.13455



#property copyright "Copyright 2014, MetaQuotes Software Corp."
#property link      "http://www.mql5.com"
#property version   "1.00"

#property copyright "Barb�cz Attila"
#property link      "http://barbocz.com"
#property indicator_chart_window


#property indicator_buffers 13

#property indicator_color1  Green
#property indicator_color2  Red
#property indicator_color3  Yellow
#property indicator_color4  Aqua
#property indicator_color5  White
#property indicator_color6  White

#property indicator_color7  White
#property indicator_color8  White
#property indicator_color9  Aqua
#property indicator_color10  Yellow
#property indicator_color11  Red
#property indicator_color12  Green
#property indicator_color13  Blue


extern int MurrayPeriod=256;
extern int MaxBar=2000;

double m3[];
double m2[];
double m1[];
double m0[];
double mMinus1[];
double mMinus2[];

double m4[];
double m5[];
double m6[];
double m7[];
double m8[];
double mPlus1[];
double mPlus2[];

datetime lastChecked;
bool firstRun=true;
int minute;
int firstFrameIndex,strength;

int
OctLinesCnt=13,
nDigits=0,
i=0;

double  dmml=0,
dvtl = 0,
sum  = 0,
v1 = 0,
v2 = 0,
mn = 0,
mx = 0,
x1 = 0,
x2 = 0,
x3 = 0,
x4 = 0,
x5 = 0,
x6 = 0,
y1 = 0,
y2 = 0,
y3 = 0,
y4 = 0,
y5 = 0,
y6 = 0,
octave=0,
fractal = 0,
range   = 0,
finalH  = 0,
finalL  = 0;

string ln_txt[13],TAG="MURRAY_MATH";
color mml_clr[13];
int windowId=-1;
bool drawOnce=true;


//+------------------------------------------------------------------+
//| Custom indicator initialization function                         |
//+------------------------------------------------------------------+
int OnInit()
  {
   IndicatorDigits(5);
   IndicatorBuffers(13);
   SetIndexBuffer(0,m3);SetIndexLabel(0,"[3/8]P");
   SetIndexBuffer(1,m2);SetIndexLabel(1,"[2/8]P");
   SetIndexBuffer(2,m1);SetIndexLabel(2,"[1/8]P");
   SetIndexBuffer(3,m0);SetIndexLabel(3,"[0/8]P");
   SetIndexBuffer(4,mMinus1);SetIndexLabel(4,"[-1/8]P");
   SetIndexBuffer(5,mMinus2);SetIndexLabel(5,"[-2/8]P");

   SetIndexBuffer(6,mPlus2);SetIndexLabel(6,"[+2/8]P");
   SetIndexBuffer(7,mPlus1);SetIndexLabel(7,"[+1/8]P");
   SetIndexBuffer(8,m8);SetIndexLabel(8,"[8/8]P");
   SetIndexBuffer(9,m7);SetIndexLabel(9,"[7/8]P");
   SetIndexBuffer(10,m6);SetIndexLabel(10,"[6/8]P");
   SetIndexBuffer(11,m5);SetIndexLabel(11,"[5/8]P");
   SetIndexBuffer(12,m4);SetIndexLabel(12,"[4/8]P");

   for(i=0; i<6; i++) {if(MathMod(i,2)!=0) SetIndexStyle(i,DRAW_LINE,STYLE_SOLID); else SetIndexStyle(i,DRAW_LINE,STYLE_DOT); }
   for(i=6; i<12; i++) {if(MathMod(i,2)==0) SetIndexStyle(i,DRAW_LINE,STYLE_SOLID); else SetIndexStyle(i,DRAW_LINE,STYLE_DOT); }

   ln_txt[0]  = "[-2/8]P";// "extremely overshoot [-2/8]";// [-2/8]
   ln_txt[1]  = "[-1/8]P";// "overshoot [-1/8]";// [-1/8]
   ln_txt[2]  = "[0/8]P";// "Ultimate Support - extremely oversold [0/8]";// [0/8]
   ln_txt[3]  = "[1/8]P";// "Weak, Place to Stop and Reverse - [1/8]";// [1/8]
   ln_txt[4]  = "[2/8]P";// "Pivot, Reverse - major [2/8]";// [2/8]
   ln_txt[5]  = "[3/8]P";// "Bottom of Trading Range - [3/8], if 10-12 bars then 40% Time. BUY Premium Zone";//[3/8]
   ln_txt[6]  = "[4/8]P";// "Major Support/Resistance Pivotal Point [4/8]- Best New BUY or SELL level";// [4/8]
   ln_txt[7]  = "[5/8]P";// "Top of Trading Range - [5/8], if 10-12 bars then 40% Time. SELL Premium Zone";//[5/8]
   ln_txt[8]  = "[6/8]P";// "Pivot, Reverse - major [6/8]";// [6/8]
   ln_txt[9]  = "[7/8]P";// "Weak, Place to Stop and Reverse - [7/8]";// [7/8]
   ln_txt[10] = "[8/8]P";// "Ultimate Resistance - extremely overbought [8/8]";// [8/8]
   ln_txt[11] = "[+1/8]P";// "overshoot [+1/8]";// [+1/8]
   ln_txt[12] = "[+2/8]P";// "extremely overshoot [+2/8]";// [+2/8]


   mml_clr[0]  = White;
   mml_clr[1]  = White;    // [-1]/8
   mml_clr[2]  = Aqua;     //  [0]/8
   mml_clr[3]  = Yellow;   //  [1]/8
   mml_clr[4]  = Red;      //  [2]/8
   mml_clr[5]  = Green;    //  [3]/8
   mml_clr[6]  = Blue;     //  [4]/8
   mml_clr[7]  = Green;    //  [5]/8
   mml_clr[8]  = Red;      //  [6]/8
   mml_clr[9]  = Yellow;   //  [7]/8
   mml_clr[10] = Aqua;     //  [8]/8
   mml_clr[11] = White;    // [+1]/8
   mml_clr[12] = White;    // [+2]/8


   IndicatorShortName(TAG);

   windowId=ChartWindowFind();

   return(0);
   return(INIT_SUCCEEDED);
  }
//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+
int onDeinit()
  {

   if(windowId>-1) objectDel(TAG);

   return(0);
  }
//+------------------------------------------------------------------+
//| Custom indicator iteration function                              |
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
//---
   int counted_bars=IndicatorCounted();
   int shift,limit;
//----
   if(counted_bars==0)
     {

      limit=Bars-MurrayPeriod-1;
      if(limit>MaxBar+MurrayPeriod) limit=MaxBar;

     }
   else limit=Bars-counted_bars-1;

   if(limit==0) limit=-1;
   for(shift=limit; shift>=0; shift--)
     {

      process(shift);
     }



//--- return value of prev_calculated for next call
   return(rates_total);
  }
//+------------------------------------------------------------------+
//+------------------------------------------------------------------+
void process(int shift)
  {
   double mml[13];

debugStartTime=2019.03.05 09:50
if (Time[shift]>=debugStartTime && Time[shift]<=debugEndTime) debug=true; else debug=false;

   v1 = Low[iLowest(NULL,0,MODE_LOW,MurrayPeriod,shift)];
   v2 = High[iHighest(NULL,0,MODE_HIGH,MurrayPeriod,shift)];
   
   if (debug) Print("v1: "+v1+"   v2: "+v2);

//determine fractal.....
   if(v2<=250000 && v2>25000)
      fractal=100000;
   else
   if(v2<=25000 && v2>2500)
          fractal=10000;
   else
   if(v2<=2500 && v2>250)
          fractal=1000;
   else
   if(v2<=250 && v2>25)
          fractal=100;
   else
   if(v2<=25 && v2>12.5)
          fractal=12.5;
   else
   if(v2<=12.5 && v2>6.25)
          fractal=12.5;
   else
   if(v2<=6.25 && v2>3.125)
          fractal=6.25;
   else
   if(v2<=3.125 && v2>1.5625)
          fractal=3.125;
   else
   if(v2<=1.5625 && v2>0.390625)
          fractal=1.5625;
   else
   if(v2<=0.390625 && v2>0)
          fractal=0.1953125;

   range=(v2-v1);
   if(range==0) return;
   
   if (debug) Print("fractal: "+fractal+"   range: "+range);
 
   sum=MathFloor(MathLog(fractal/range)/MathLog(2));
   octave=fractal*(MathPow(0.5,sum));
   
   if (debug) Print("sum: "+sum+"   octave: "+octave);

   if(octave==0) return;

   mn=MathFloor(v1/octave)*octave;
   if((mn+octave)>v2) mx=mn+octave;   else   mx=mn+(2*octave);
   
   if (debug) Print("mn: "+mn+"   mx: "+mx);

// calculating xx

   if((v1>=(3*(mx-mn)/16+mn)) && (v2<=(9*(mx-mn)/16+mn))) x2=mn+(mx-mn)/2;     else x2=0;
   
   if (debug) Print("3*(mx-mn)/16+mn: "+3*(mx-mn)/16+mn)+"   9*(mx-mn)/16+mn: "+(9*(mx-mn)/16+mn)+"   x2:"+x2);

   if((v1>=(mn-(mx-mn)/8)) && (v2<=(5*(mx-mn)/8+mn)) && (x2==0)) x1=mn+(mx-mn)/2; else x1=0;

   if((v1>=(mn+7*(mx-mn)/16)) && (v2<=(13*(mx-mn)/16+mn))) x4=mn+3*(mx-mn)/4;     else x4=0;

   if((v1>=(mn+3*(mx-mn)/8)) && (v2<=(9*(mx-mn)/8+mn)) && (x4==0)) x5=mx;   else  x5=0;

   if((v1>=(mn+(mx-mn)/8)) && (v2<=(7*(mx-mn)/8+mn)) && (x1==0) && (x2==0) && (x4==0) && (x5==0)) x3=mn+3*(mx-mn)/4; else x3=0;

   if((x1+x2+x3+x4+x5)==0) x6=mx;  else x6=0;

   finalH=x1+x2+x3+x4+x5+x6;
   
   if (debug) Print("x1: "+x1+", x2: "+ x2 +", x3: "+ x3+", x4: "+ x4+", x5: "+ x5+", x6: "+ x6);

// calculating yy
   if(x1>0) y1=mn;  else y1=0;

   if(x2>0) y2=mn+(mx-mn)/4; else y2=0;

   if(x3>0) y3=mn+(mx-mn)/4; else y3=0;

   if(x4>0) y4=mn+(mx-mn)/2; else y4=0;

   if(x5>0) y5=mn+(mx-mn)/2; else y5=0;

   if((finalH>0) && ((y1+y2+y3+y4+y5)==0)) y6=mn;    else y6=0;
   
   if (debug) Print("y1: "+y1+", y2: "+ y2 +", y3: "+ y3+", y4: "+ y4+", y5: "+ y5+", y6: "+ y6);

   finalL=y1+y2+y3+y4+y5+y6;
   if (debug) Print("finalL: "+finalL);

   for(i=0; i<OctLinesCnt; i++) { mml[i]=0; }

   dmml=(finalH-finalL)/8;
   if (debug) Print("dmml: "+dmml);

   mml[0]=(finalL-dmml*2); //-2/8
   if (debug) Print("mml[0]: "+mml[0]);
   for(i=1; i<OctLinesCnt; i++) { mml[i]=mml[i-1]+dmml; }

   m3[shift]=mml[5];
   m2[shift]=mml[4];
   m1[shift]=mml[3];
   m0[shift]=mml[2];
   mMinus1[shift]=mml[1];
   mMinus2[shift]=mml[0];

   m4[shift]=mml[6];
   m5[shift]=mml[7];
   m6[shift]=mml[8];
   m7[shift]=mml[9];
   m8[shift]=mml[10];
   mPlus1[shift]=mml[11];
   mPlus2[shift]=mml[12];

      SetIndexBuffer(0,m3);SetIndexLabel(0,"[3/8]P");
      SetIndexBuffer(1,m2);SetIndexLabel(1,"[2/8]P");
      SetIndexBuffer(2,m1);SetIndexLabel(2,"[1/8]P");
      SetIndexBuffer(3,m0);SetIndexLabel(3,"[0/8]P");
      SetIndexBuffer(4,mMinus1);SetIndexLabel(4,"[-1/8]P");
      SetIndexBuffer(5,mMinus2);SetIndexLabel(5,"[-2/8]P");

      SetIndexBuffer(6,mPlus2);SetIndexLabel(6,"[+2/8]P");
      SetIndexBuffer(7,mPlus1);SetIndexLabel(7,"[+1/8]P");
      SetIndexBuffer(8,m8);SetIndexLabel(8,"[8/8]P");
      SetIndexBuffer(9,m7);SetIndexLabel(9,"[7/8]P");
      SetIndexBuffer(10,m6);SetIndexLabel(10,"[6/8]P");
      SetIndexBuffer(11,m5);SetIndexLabel(11,"[5/8]P");
      SetIndexBuffer(12,m4);SetIndexLabel(12,"[4/8]P");


      //if(windowId>-1)
      //  {
      //   for(i=0; i<13; i++)
      //     {
      //      if(ObjectFind(ln_txt[i])==-1)
      //        {
      //         ObjectCreate(TAG+ln_txt[i],OBJ_TEXT,0,0,0);
      //         ObjectSetText(TAG+ln_txt[i],ln_txt[i],8,"Arial",mml_clr[i]);
      //         ObjectMove(TAG+ln_txt[i],0,Time[0]+Period()*60*2,mml[i]);
      //        }
      //      else ObjectMove(TAG+ln_txt[i],0,Time[0]+Period()*60*2,mml[i]);
      //     }
      //  }


  }
//+------------------------------------------------------------------+
int objectDel(string objectPrefix)
  {
   int counter,obj_total=ObjectsTotal();
   for(i=obj_total;i>-1;i--)
     {
      string objectName=ObjectName(i);
      if(StringFind(objectName,objectPrefix)>-1)
        {
         ObjectDelete(objectName);
         counter++;
        }
     }
   return (counter);
  }
//+------------------------------------------------------------------+
