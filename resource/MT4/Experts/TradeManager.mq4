//+------------------------------------------------------------------+
//|                                                   ZMQ_Server.mq4 |
//|                        Copyright 2014, MetaQuotes Software Corp. |
//|                                              http://www.mql5.com |
//+------------------------------------------------------------------+
#property copyright "Copyright 2014, MetaQuotes Software Corp."
#property link      "http://www.mql5.com"
#property version   "1.00"
#property strict

#include <Zmq/Zmq.mqh>
#include <hash.mqh>
#include <json.mqh>
Context context("tradeManager");
Socket socket(context,ZMQ_REP);
ZmqMsg request,reply;

JSONParser *parser;
JSONObject *jValue ;
JSONValue *jObject ;

extern int MagicNumber = 123456;
extern int MaximumOrders = 1;
extern double MaximumLotSize = 1.0;
extern int MaximumSlippage = 3;
extern bool DMA_MODE = false;
extern int MILLISECOND_TIMER = 1;
extern int PortNumber=6000;

uchar _data[];

//+------------------------------------------------------------------+
//| Expert initialization function                                   |
//+------------------------------------------------------------------+
int OnInit()
  {

   ObjectsDeleteAll();
   createLabel("status","TradeManager is running...",20,40);
   createLabel("request","REQUEST:",20,100);
   createLabel("reply","REPLY:",20,160);

   parser = new JSONParser();

   socket.bind("tcp://*:"+IntegerToString(PortNumber));
   socket.setReceiveTimeout(50);
   Print("Binding MT4 Server TradeManager on Port: "+IntegerToString(PortNumber));

   Sleep(500);

   socket.recv(request);
   while(request.size()>0)
     {
      socket.send("Purged");
      Print("Init purge: "+request.getData());
      socket.recv(request);
     }


   socket.setReceiveTimeout(1000);

   EventSetMillisecondTimer(1);



//---
   return(INIT_SUCCEEDED);
  }
//+------------------------------------------------------------------+
//| Expert deinitialization function                                 |
//+------------------------------------------------------------------+
void OnDeinit(const int reason)
  {
//--- destroy timer
   Print("Unbinding MT4 Server from Socket on Port: "+IntegerToString(PortNumber));
   socket.unbind("tcp://*:5555");
   socket.disconnect("tcp://*:5555");

// Destroy ZeroMQ Context
   context.destroy(0);
   EventKillTimer();
   ObjectsDeleteAll();
   delete jObject;
   delete jValue;
   delete parser;

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
   socket.recv(request);
   if(request.size()==0)
      return;
   else
     {
      string message=request.getData();

      Print(message);
      InterpretZmqMessage(message);
      socket.send("GOT IT");
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

  }
//+------------------------------------------------------------------+
// OPEN NEW ORDER
int OpenOrder(string symbol, int _type, double lot, double _price, double stopLoss, double takeProfit, string _comment, int magicNumber, string &zmq_ret)
  {

   int ticket, error;


   if(lot > MaximumLotSize)
     {
      zmq_ret = zmq_ret + ", " + "\"error\": \"LOT_SIZE_ERROR\", \"errorValue\": \"MAX_LOT_SIZE_EXCEEDED\"";
      return(-1);
     }

   double sl = stopLoss;
   double tp = takeProfit;

// Else
   if(DMA_MODE)
     {
      sl = 0.0;
      tp = 0.0;
     }

   if(symbol == "NULL")
     {
      ticket = OrderSend(Symbol(), _type, lot, _price, MaximumSlippage, sl, tp, _comment, magicNumber);
     }
   else
     {
      ticket = OrderSend(symbol, _type, lot, _price, MaximumSlippage, sl, tp, _comment, magicNumber);
     }
   if(ticket < 0)
     {
      // Failure
      error = GetLastError();
      zmq_ret = zmq_ret + ", " + "\"error\": \"" + IntegerToString(error) + "\", \"errorValue\": \"" + ErrorDescription(error) + "\"";
      return(-1*error);
     }

   int tmpRet = OrderSelect(ticket, SELECT_BY_TICKET, MODE_TRADES);

   zmq_ret = zmq_ret + ", \"orders\": [{" + "\"magicNumber\": " + IntegerToString(magicNumber) + ", \"ticketNumber\": " + IntegerToString(OrderTicket()) + ", \"openTime\": \"" + TimeToStr(OrderOpenTime(),TIME_DATE|TIME_SECONDS) + "\", \"openPrice\": " + DoubleToString(OrderOpenPrice())+ "}]";

   if(DMA_MODE)
     {

      int retries = 3;
      while(true)
        {
         retries--;
         if(retries < 0)
            return(0);

         if((stopLoss == 0 && takeProfit == 0) || (OrderStopLoss() == stopLoss && OrderTakeProfit() == takeProfit))
           {
            return(ticket);
           }

         if(IsTradingAllowed(30, zmq_ret) == 1)
           {
            if(SetSLTP(ticket, stopLoss, takeProfit, zmq_ret))
              {
               return(ticket);
              }
            if(retries == 0)
              {
               zmq_ret = zmq_ret + ", \"error\": \"ERROR_SETTING stopLosstakeProfit\"";
               return(-11111);
              }
           }

         Sleep(MILLISECOND_TIMER);
        }

      zmq_ret = zmq_ret + ", \"error\": \"ERROR_SETTING stopLosstakeProfit\"";
      zmq_ret = zmq_ret + "]}";
      return(-1);
     }

// Send zmq_ret to Python Client


   return(ticket);
  }

// SET SL/TP
bool SetSLTP(int ticket, double stopLoss, double takeProfit, string &zmq_ret)
  {

   zmq_ret = zmq_ret + ", \"ticketNumber\""+IntegerToString(ticket);;
   if(OrderSelect(ticket, SELECT_BY_TICKET) == true)
     {
      //      int dir_flag = -1;
      //
      //      if(OrderType() == 0 || OrderType() == 2 || OrderType() == 4)
      //         dir_flag = 1;

      //double vpoint  = MarketInfo(OrderSymbol(), MODE_POINT);
      int    vdigits = (int)MarketInfo(OrderSymbol(), MODE_DIGITS);
      double mSL = NormalizeDouble(stopLoss,vdigits);
      double mTP = NormalizeDouble(takeProfit,vdigits);

      // if(OrderModify(ticket, OrderOpenPrice(), NormalizeDouble(OrderOpenPrice()-stopLoss*dir_flag*vpoint,vdigits), NormalizeDouble(OrderOpenPrice()+takeProfit*dir_flag*vpoint,vdigits), 0, 0)) {
      if(OrderModify(ticket, OrderOpenPrice(), mSL, mTP, 0, 0))
        {
         // zmq_ret = zmq_ret + ", \"stopLoss\": " + DoubleToString(stopLoss) + ", \"takeProfit\": " + DoubleToString(takeProfit);
         zmq_ret = zmq_ret + ", \"stopLoss\": " + DoubleToString(mSL) + ", \"takeProfit\": " + DoubleToString(mTP);
         return(true);
        }
      else
        {
         int error = GetLastError();
         zmq_ret = zmq_ret + ", \"error\": \"" + IntegerToString(error) + "\", \"errorValue\": \"" + ErrorDescription(error) + "\", \"stopLoss\": " + DoubleToString(mTP) + ", \"takeProfit\": " + DoubleToString(mSL);

         return(false);
        }
     }
   else
     {
      zmq_ret = zmq_ret + ", \"error\": \"TICKET NOT_FOUND\"";
     }


   return(false);
  }

// CLOSE AT MARKET
bool CloseAtMarket(double size, string &zmq_ret, int ticket = 0)
  {

   int error;

   int retries = 3;
   while(true)
     {
      retries--;
      if(retries < 0)
         return(false);

      if(IsTradingAllowed(30, zmq_ret) == 1)
        {
         if(ClosePartial(size, zmq_ret,ticket))
           {
            // trade successfuly closed

            return(true);
           }
         else return(false);
         //  {
         //   error = GetLastError();
         //   zmq_ret = zmq_ret + " \"error\": \"" + IntegerToString(error) + "\", \"errorValue\": \"" + ErrorDescription(error) + "\"";
         //   return(false);
         //  }
        }

     }

   return(false);
  }

// CLOSE PARTIAL SIZE
bool ClosePartial(double size, string &zmq_ret, int ticket = 0)
  {

   int error;
   bool close_ret = False;



// If the function is called directly, setup init() JSON here and get OrderSelect.
   if(ticket != 0)
     {

      if(!OrderSelect(ticket, SELECT_BY_TICKET))
        {
         zmq_ret = zmq_ret + "{ \"error\": \"TICKET NOT FOUND\"}";
         return false;
        }
      else
         zmq_ret = zmq_ret + "{\"ticketNumber\":"+IntegerToString(ticket);
     }

//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+
   RefreshRates();
   double priceCP;

   if(OrderType() == OP_BUY)
     {
      priceCP = GetBid(OrderSymbol());
     }
   else
      if(OrderType() == OP_SELL)
        {
         priceCP = GetAsk(OrderSymbol());
        }
      else
        {
         return(true);
        }

   ticket = OrderTicket();

   if(size < 0.0 || size > OrderLots())
     {
      zmq_ret = zmq_ret + ", \"error\": \"BAD LOT SIZE\", \"lot\":" +DoubleToString(size)+"}";
      return false;
     }
   if(size == 0.0)
      size = OrderLots();

   size=NormalizeDouble(size,2);

   bool isFullClose=(size==OrderLots());

   close_ret = OrderClose(ticket, size, priceCP, MaximumSlippage);

   if(close_ret == true)
     {
      if(!OrderSelect(ticket, SELECT_BY_TICKET))
        {
         zmq_ret = zmq_ret + " , \"error\": \"TICKET NOT FOUND AFTER CLOSE\"}";
         return false;
        }
      else
        {
        

         zmq_ret = zmq_ret + ", \"closePrice\": " + DoubleToString(OrderClosePrice(),5) + ", \"lot\": " + DoubleToString(OrderLots(),2)+", \"closeTime\": \"" + TimeToStr(OrderCloseTime(),TIME_DATE|TIME_SECONDS)+"\", \"profit\": " + DoubleToString((OrderProfit()+OrderCommission()),2);
         if(!isFullClose)
           {
            string comment=OrderComment();
            int newTicketNumberPos=StringFind(comment,"to #");
            if(newTicketNumberPos<0)
              {
               zmq_ret = zmq_ret + " , \"error\": \"NEW TICKET REF NOT FOUND AFTER PARTIAL CLOSE\"}";
               return false;
              }
            else
              {
               int newTicketNumber=StringToInteger(StringSubstr(comment,newTicketNumberPos+4));
               if(!OrderSelect(newTicketNumber, SELECT_BY_TICKET))
                 {
                  zmq_ret = zmq_ret + " , \"error\": \"TICKET NOT FOUND AFTER PARTIAL CLOSE\"}";
                  return false;
                 }
               else
                  zmq_ret = zmq_ret + ",\"newTicketNumber\": " + IntegerToString(OrderTicket()) + ", \"newOpenPrice\": " + DoubleToString(OrderOpenPrice(),5) + ", \"newOpenTime\": \"" + TimeToStr(OrderOpenTime(),TIME_DATE|TIME_SECONDS) +"\"";

              }
           }

            zmq_ret = zmq_ret + "}";
        }
     }
   else
     {
      error = GetLastError();
      zmq_ret = zmq_ret + ", \"error\": \"" + IntegerToString(error) + "\", \"errorValue\": \"" + ErrorDescription(error) + "\"}";
     }

   return(close_ret);

  }

// CLOSE ORDER (by Magic Number)
void CloseOrdermagicNumber(int magicNumber, string &zmq_ret)
  {

   if(OrdersTotal()>0)
      zmq_ret = zmq_ret + ", \"orders\": [";
   else
      return;


   for(int i=OrdersTotal()-1; i >= 0; i--)
     {
      if(OrderSelect(i,SELECT_BY_POS)==true && OrderMagicNumber() == magicNumber)
        {


         if(OrderType() == OP_BUY || OrderType() == OP_SELL)
           {
            CloseAtMarket(0.0, zmq_ret,OrderTicket());
            //zmq_ret = zmq_ret + ", \"response\": \"CLOSE_MARKET\"";

            if(i == 0)
               zmq_ret = zmq_ret + "]";

           }
         else
           {
            //zmq_ret = zmq_ret + ", \"response\": \"CLOSE_PENDING\"";

            if(i == 0)
               zmq_ret = zmq_ret + "]";

            int tmpRet = OrderDelete(OrderTicket());
           }
        }
     }






  }

// CLOSE ORDER (by Ticket)
void CloseOrderticketNumber(int ticketNumber, string &zmq_ret)
  {


   if(OrderSelect(ticketNumber, SELECT_BY_TICKET))
     {
      zmq_ret = zmq_ret + ", \"orders\": [";
      if(OrderType() == OP_BUY || OrderType() == OP_SELL)
        {
         CloseAtMarket(0.0, zmq_ret,ticketNumber);
         //zmq_ret = zmq_ret + ", \"_response\": \"CLOSE_MARKET\"";
        }
      else
        {
         //zmq_ret = zmq_ret + ", \"_response\": \"CLOSE_PENDING\"";
         int tmpRet = OrderDelete(OrderTicket());
        }
      zmq_ret = zmq_ret + "]";
     }

   else
      zmq_ret = zmq_ret + ", \"error\": \"NOT_FOUND\"";




  }

// CLOSE ALL ORDERS
void CloseAllOrders(string &zmq_ret)
  {


   if(OrdersTotal()>0)
      zmq_ret = zmq_ret + ", \"orders\": [";
   else
      return;

   for(int i=OrdersTotal()-1; i >= 0; i--)
     {
      if(OrderSelect(i,SELECT_BY_POS)==true)
        {

         if(OrderType() == OP_BUY || OrderType() == OP_SELL)
           {
            CloseAtMarket(0.0, zmq_ret,OrderTicket());
            //zmq_ret = zmq_ret + ", \"_response\": \"CLOSE_MARKET\"";

            if(i == 0)
               zmq_ret = zmq_ret + "]";

           }
         else
           {
            //zmq_ret = zmq_ret + ", \"_response\": \"CLOSE_PENDING\"";

            if(i == 0)
               zmq_ret = zmq_ret + "]";

            int tmpRet = OrderDelete(OrderTicket());
           }
        }
     }





  }

// GET OPEN ORDERS
void GetOpenOrders(string &zmq_ret)
  {

   if(OrdersTotal()>0)
      zmq_ret = zmq_ret + ", \"orders\": [";
   else
      return;

   for(int i=OrdersTotal()-1; i>=0; i--)
     {


      if(OrderSelect(i,SELECT_BY_POS)==true)
        {


         zmq_ret = zmq_ret + "{ \"ticketNumber\": " + IntegerToString(OrderTicket()) + ", \"symbol\": \"" + OrderSymbol() + "\", \"lot\": " + DoubleToString(OrderLots(),2) + ", \"type\": " + IntegerToString(OrderType()) + ", \"openPrice\": " + DoubleToString(OrderOpenPrice(),5) + ", \"openTime\": \"" + TimeToStr(OrderOpenTime(),TIME_DATE|TIME_SECONDS) + "\", \"stopLoss\": " + DoubleToString(OrderStopLoss()) + ", \"takeProfit\": " + DoubleToString(OrderTakeProfit()) + ", \"profit\": " + DoubleToString(OrderProfit()) + ", \"comment\": \"" + OrderComment() + "\"}";

         if(i != 0)
            zmq_ret = zmq_ret + ", ";

        }
     }
   zmq_ret = zmq_ret + "]";
  }

// CHECK IF TRADE IS ALLOWED
int IsTradingAllowed(int MaxWaiting_sec, string &zmq_ret)
  {

   if(!IsTradeAllowed())
     {

      int StartWaitingTime = (int)GetTickCount();
      zmq_ret = zmq_ret + ", " + "\"error\": \"TRADE_CONTEXT_BUSY\"";

      while(true)
        {

         if(IsStopped())
           {
            zmq_ret = zmq_ret + ", " + "\"errorValue\": \"EA_STOPPED_BY_USER\"";
            return(-1);
           }

         int diff = (int)(GetTickCount() - StartWaitingTime);
         if(diff > MaxWaiting_sec * 1000)
           {
            zmq_ret = zmq_ret + ", \"error\": \"WAIT_LIMIT_EXCEEDED\", \"errorValue\": " + IntegerToString(MaxWaiting_sec);
            return(-2);
           }
         // if the trade context has become free,
         if(IsTradeAllowed())
           {
            //zmq_ret = zmq_ret + ", \"_response\": \"TRADE_CONTEXT_NOW_FREE\"";
            RefreshRates();
            return(1);
           }

        }
     }
   else
     {
      return(1);
     }

   return(1);
  }
//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+
string ErrorDescription(int error_code)
  {
   string error_string;
//----
   switch(error_code)
     {
      //---- codes returned from trade server
      case 0:
      case 1:
         error_string="no error";
         break;
      case 2:
         error_string="common error";
         break;
      case 3:
         error_string="invalid trade parameters";
         break;
      case 4:
         error_string="trade server is busy";
         break;
      case 5:
         error_string="old version of the client terminal";
         break;
      case 6:
         error_string="no connection with trade server";
         break;
      case 7:
         error_string="not enough rights";
         break;
      case 8:
         error_string="too frequent requests";
         break;
      case 9:
         error_string="malfunctional trade operation (never returned error)";
         break;
      case 64:
         error_string="account disabled";
         break;
      case 65:
         error_string="invalid account";
         break;
      case 128:
         error_string="trade timeout";
         break;
      case 129:
         error_string="invalid price";
         break;
      case 130:
         error_string="invalid stops";
         break;
      case 131:
         error_string="invalid trade volume";
         break;
      case 132:
         error_string="market is closed";
         break;
      case 133:
         error_string="trade is disabled";
         break;
      case 134:
         error_string="not enough money";
         break;
      case 135:
         error_string="price changed";
         break;
      case 136:
         error_string="off quotes";
         break;
      case 137:
         error_string="broker is busy (never returned error)";
         break;
      case 138:
         error_string="requote";
         break;
      case 139:
         error_string="order is locked";
         break;
      case 140:
         error_string="long positions only allowed";
         break;
      case 141:
         error_string="too many requests";
         break;
      case 145:
         error_string="modification denied because order too close to market";
         break;
      case 146:
         error_string="trade context is busy";
         break;
      case 147:
         error_string="expirations are denied by broker";
         break;
      case 148:
         error_string="amount of open and pending orders has reached the limit";
         break;
      case 149:
         error_string="hedging is prohibited";
         break;
      case 150:
         error_string="prohibited by FIFO rules";
         break;
      //---- mql4 errors
      case 4000:
         error_string="no error (never generated code)";
         break;
      case 4001:
         error_string="wrong function pointer";
         break;
      case 4002:
         error_string="array index is out of range";
         break;
      case 4003:
         error_string="no memory for function call stack";
         break;
      case 4004:
         error_string="recursive stack overflow";
         break;
      case 4005:
         error_string="not enough stack for parameter";
         break;
      case 4006:
         error_string="no memory for parameter string";
         break;
      case 4007:
         error_string="no memory for temp string";
         break;
      case 4008:
         error_string="not initialized string";
         break;
      case 4009:
         error_string="not initialized string in array";
         break;
      case 4010:
         error_string="no memory for array string";
         break;
      case 4011:
         error_string="too long string";
         break;
      case 4012:
         error_string="remainder from zero divide";
         break;
      case 4013:
         error_string="zero divide";
         break;
      case 4014:
         error_string="unknown command";
         break;
      case 4015:
         error_string="wrong jump (never generated error)";
         break;
      case 4016:
         error_string="not initialized array";
         break;
      case 4017:
         error_string="dll calls are not allowed";
         break;
      case 4018:
         error_string="cannot load library";
         break;
      case 4019:
         error_string="cannot call function";
         break;
      case 4020:
         error_string="expert function calls are not allowed";
         break;
      case 4021:
         error_string="not enough memory for temp string returned from function";
         break;
      case 4022:
         error_string="system is busy (never generated error)";
         break;
      case 4050:
         error_string="invalid function parameters count";
         break;
      case 4051:
         error_string="invalid function parameter value";
         break;
      case 4052:
         error_string="string function internal error";
         break;
      case 4053:
         error_string="some array error";
         break;
      case 4054:
         error_string="incorrect series array using";
         break;
      case 4055:
         error_string="custom indicator error";
         break;
      case 4056:
         error_string="arrays are incompatible";
         break;
      case 4057:
         error_string="global variables processing error";
         break;
      case 4058:
         error_string="global variable not found";
         break;
      case 4059:
         error_string="function is not allowed in testing mode";
         break;
      case 4060:
         error_string="function is not confirmed";
         break;
      case 4061:
         error_string="send mail error";
         break;
      case 4062:
         error_string="string parameter expected";
         break;
      case 4063:
         error_string="integer parameter expected";
         break;
      case 4064:
         error_string="double parameter expected";
         break;
      case 4065:
         error_string="array as parameter expected";
         break;
      case 4066:
         error_string="requested history data in update state";
         break;
      case 4099:
         error_string="end of file";
         break;
      case 4100:
         error_string="some file error";
         break;
      case 4101:
         error_string="wrong file name";
         break;
      case 4102:
         error_string="too many opened files";
         break;
      case 4103:
         error_string="cannot open file";
         break;
      case 4104:
         error_string="incompatible access to a file";
         break;
      case 4105:
         error_string="no order selected";
         break;
      case 4106:
         error_string="unknown symbol";
         break;
      case 4107:
         error_string="invalid price parameter for trade function";
         break;
      case 4108:
         error_string="invalid ticket";
         break;
      case 4109:
         error_string="trade is not allowed in the expert properties";
         break;
      case 4110:
         error_string="longs are not allowed in the expert properties";
         break;
      case 4111:
         error_string="shorts are not allowed in the expert properties";
         break;
      case 4200:
         error_string="object is already exist";
         break;
      case 4201:
         error_string="unknown object property";
         break;
      case 4202:
         error_string="object is not exist";
         break;
      case 4203:
         error_string="unknown object type";
         break;
      case 4204:
         error_string="no object name";
         break;
      case 4205:
         error_string="object coordinates error";
         break;
      case 4206:
         error_string="no specified subwindow";
         break;
      default:
         error_string="unknown error";
     }
//----
   return(error_string);
  }
//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+
double GetAsk(string symbol)
  {
   if(symbol == "NULL")
     {
      return(Ask);
     }
   else
     {
      return(MarketInfo(symbol,MODE_ASK));
     }
  }

//+------------------------------------------------------------------+

//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+
double GetBid(string symbol)
  {
   if(symbol == "NULL")
     {
      return(Bid);
     }
   else
     {
      return(MarketInfo(symbol,MODE_BID));
     }
  }
//+------------------------------------------------------------------+
void InterpretZmqMessage(string message)
  {
   bool answer=false;
   string action="",zmq_ret="";
   double orderPrice=0.0,stopLoss=0.0,takeProfit=0.0,lot=-1.0;
   int orderType=-1,magicNumber=0,ticketNumber=0;
   string orderSymbol="",comment="",errorMessage="",currentDateTimeString;
   datetime currentDateTime=TimeCurrent();

   currentDateTimeString=TimeToStr(TimeCurrent(),TIME_DATE|TIME_SECONDS);

   zmq_ret="[{";

   jObject= parser.parse(message);
   if(jObject == NULL)
     {
      Print("Message parse error: "+(string)parser.getErrorCode()+parser.getErrorMessage());
      return;
     }
   else
     {
      if(jObject.isObject())
         jValue = jObject;
     }

   string defaultValue="invalid parameter";

   if(!jValue.getString("action",action))
     {
      Print("Message interpretation error: action  is not found!");
      return;
     }
   zmq_ret = zmq_ret + "\"action\": \""+action+"\"";



   jValue.getString("currentDateTimeString",currentDateTimeString);
   currentDateTime=StrToTime(currentDateTimeString);

   if(StringFind(action,"TRADE_")==0)
     {
      if(!jValue.getInt("type",orderType) &&  action=="TRADE_OPEN")
         errorMessage+="orderType ";
      if(!jValue.getString("symbol",orderSymbol) &&  action=="TRADE_OPEN")
         errorMessage+="orderSymbol ";
      if(StringLen(errorMessage)==0)
        {
         if(orderType==OP_BUY)
            orderPrice=GetBid(orderSymbol);
         else
            if(orderType==OP_SELL)
               orderPrice=GetAsk(orderSymbol);
        }
      else
        {
         Print("Trade Message interpretation error: "+errorMessage+" missing!");
         zmq_ret = zmq_ret + ", \"error\": \"interpretation error, missing "+errorMessage+"\"";
         SendReply(zmq_ret + "]}");
         return;
        }

      jValue.getDouble("lot",lot);
      jValue.getDouble("stopLoss",stopLoss);
      jValue.getDouble("takeProfit",takeProfit);
      jValue.getInt("magicNumber",magicNumber);
      jValue.getInt("ticketNumber",ticketNumber);
      jValue.getString("comment",comment);



     }


   if(action=="TRADE_OPEN")
     {
      if(CheckOpsStatus())
        {

         int ticket = OpenOrder(orderSymbol, orderType, lot, orderPrice, stopLoss, takeProfit, comment, magicNumber, zmq_ret);

         // Send TICKET back as JSON
         //SendReply(pSocket, zmq_ret + "]}");
        }

     }
   else
      if(action=="TRADE_SET_SLTP")
        {
         if(ticketNumber==0)
            zmq_ret = zmq_ret + "\"error\": \"ticketNumber=0\"";
         else

            answer = SetSLTP(ticketNumber, stopLoss, takeProfit, zmq_ret);
        }
      else
         if(action=="TRADE_CLOSE_PARTIAL")
           {
            if(ticketNumber==0)
               zmq_ret = zmq_ret + "\"error\": \"ticketNumber=0\"";
            else {
               zmq_ret = zmq_ret + ", \"orders\": [";
               answer = ClosePartial(lot, zmq_ret, ticketNumber);
               zmq_ret = zmq_ret + "]";
               }
           }
         else
            if(action=="TRADE_CLOSE")
              {
               if(ticketNumber==0)
                  zmq_ret = zmq_ret + "\"error\": \"ticketNumber=0\"";
               else

                  CloseOrderticketNumber(ticketNumber, zmq_ret);
              }

            else
               if(action=="TRADE_CLOSE_BY_MAGICNUMBER")
                 {

                  CloseOrdermagicNumber(magicNumber, zmq_ret);
                 }
               else
                  if(action=="TRADE_CLOSE_ALL")
                    {

                     CloseAllOrders(zmq_ret);
                    }
                  else
                     if(action=="GET_OPEN_ORDERS")
                       {

                        GetOpenOrders(zmq_ret);
                       }


//   int switchaction = 0;
//
//   /* 02-08-2019 10:41 CEST - HEARTBEAT */
//   if(compArray[0] == "HEARTBEAT")
//      SendReply( "[{\"action\": \"heartbeat\", \"_response\": \"loud and clear!\"]}");
//
//   /* Process Messages */
//   if(compArray[0] == "TRADE" && compArray[1] == "OPEN")
//      switchaction = 1;
//   if(compArray[0] == "TRADE" && compArray[1] == "MODIFY")
//      switchaction = 2;
//   if(compArray[0] == "TRADE" && compArray[1] == "CLOSE")
//      switchaction = 3;
//   if(compArray[0] == "TRADE" && compArray[1] == "CLOSE_PARTIAL")
//      switchaction = 4;
//   if(compArray[0] == "TRADE" && compArray[1] == "CLOSEmagicNumber")
//      switchaction = 5;
//   if(compArray[0] == "TRADE" && compArray[1] == "CLOSE_ALL")
//      switchaction = 6;
//   if(compArray[0] == "TRADE" && compArray[1] == "GET_OPEN_TRADES")
//      switchaction = 7;
//   if(compArray[0] == "DATA")
//      switchaction = 8;
//
//   /* Setup processing variables */
//   string zmq_ret = "";
//   string ret = "";
//   int ticket = -1;
//   bool ans = false;
//
//   /****************************
//    * PERFORM SOME CHECKS HERE *
//    ****************************/
//   if(CheckOpsStatus(switchaction) == true)
//     {
//      switch(switchaction)
//        {
//         case 1: // OPEN TRADE
//
//            zmq_ret = "[{";
//
//            //TRADE|OPEN|1|EURUSD|0.1|0|0|R-to-MetaTrader4|12345678
//            //OpenOrder(string symbol, int _type, double lot, double _price, double stopLoss, double takeProfit, string _comment, int magicNumber, string &zmq_ret)
//            // Function definition:
//            ticket = OpenOrder(compArray[3], StrToInteger(compArray[2]), StrToDouble(compArray[4]),
//                               Ask, StrToInteger(compArray[5]), StrToInteger(compArray[6]),
//                               compArray[7], StrToInteger(compArray[8]), zmq_ret);
//
//            // Send TICKET back as JSON
//            SendReply(pSocket, zmq_ret + "]}");
//
//            break;
//
//         case 2: // MODIFY SL/TP
//
//            zmq_ret = "[{\"action\": \"MODIFY\"";
//
//            // Function definition:
//            ans = SetSLTP(StrToInteger(compArray[10]), StrToDouble(compArray[5]), StrToDouble(compArray[6]),
//                          StrToInteger(compArray[9]), StrToInteger(compArray[2]), StrToDouble(compArray[4]),
//                          compArray[3], 3, zmq_ret);
//
//            SendReply(pSocket, zmq_ret + "]}");
//
//            break;
//
//         case 3: // CLOSE TRADE
//
//            zmq_ret = "[{";
//
//            // IMPLEMENT CLOSE TRADE LOGIC HERE
//            CloseOrderticketNumber(StrToInteger(compArray[10]), zmq_ret);
//
//            SendReply(pSocket, zmq_ret + "]}");
//
//            break;
//
//         case 4: // CLOSE PARTIAL
//
//            zmq_ret = "[{";
//
//            ans = ClosePartial(StrToDouble(compArray[8]), zmq_ret, StrToInteger(compArray[10]));
//
//            SendReply(pSocket, zmq_ret + "]}");
//
//            break;
//
//         case 5: // CLOSE MAGIC
//
//            zmq_ret = "[{";
//
//            CloseOrdermagicNumber(StrToInteger(compArray[9]), zmq_ret);
//
//            SendReply(pSocket, zmq_ret + "]}");
//
//            break;
//
//         case 6: // CLOSE ALL ORDERS
//
//            zmq_ret = "[{";
//
//            CloseAllOrders(zmq_ret);
//
//            SendReply(pSocket, zmq_ret + "]}");
//
//            break;
//
//         case 7: // GET OPEN ORDERS
//
//            zmq_ret = "[{";
//
//            GetOpenOrders(zmq_ret);
//
//            SendReply(pSocket, zmq_ret + "]}");
//
//            break;
//
//         case 8: // DATA REQUEST
//
//            //            zmq_ret = "[{";
//            //
//            //            GetData(compArray, zmq_ret);
//            //
//            //            SendReply(pSocket, zmq_ret + "]]}");
//
//            break;
//
//         default:
//            break;
//        }
//     }

   ObjectSetString(0,"request",OBJPROP_TEXT,"REQUEST: "+currentDateTimeString+" - "+action);

   SendReply(zmq_ret + "}]");

   return;
  }
//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+
// Inform Client
void SendReply(string message)
  {

   ZmqMsg pushReply(StringFormat("%s", message));
   Print(message);
   socket.send(pushReply,true); // NON-BLOCKING

   if(StringFind(message,"NOT FOUND")>-1 || StringFind(message,"error")>-1)
      ObjectSetString(0,"reply",OBJPROP_TEXT,"REPLY: ERROR");
   else
      ObjectSetString(0,"reply",OBJPROP_TEXT,"REPLY: OK");



  }

//+------------------------------------------------------------------+
//|                                                                  |
//+------------------------------------------------------------------+
bool CheckOpsStatus()
  {



   if(!IsTradeAllowed())
     {
      SendReply("[{\"error\": \"TRADING_IS_NOT_ALLOWED__ABORTED_COMMAND\"}]");
      return(false);
     }
   else
      if(!IsExpertEnabled())
        {
         SendReply("[{\"error\": \"EA_IS_DISABLED__ABORTED_COMMAND\"}]");
         return(false);
        }
      else
         if(IsTradeContextBusy())
           {
            SendReply("[{\"error\": \"TRADE_CONTEXT_BUSY__ABORTED_COMMAND\"}]");
            return(false);
           }
         else
            if(!IsDllsAllowed())
              {
               SendReply("[{\"error\": \"DLLS_DISABLED__ABORTED_COMMAND\"}]");
               return(false);
              }
            else
               if(!IsLibrariesAllowed())
                 {
                  SendReply("[{\"error\": \"LIBS_DISABLED__ABORTED_COMMAND\"}]");
                  return(false);
                 }
               else
                  if(!IsConnected())
                    {
                     SendReply("[{\"error\": \"NO_BROKER_CONNECTION__ABORTED_COMMAND\"}]");
                     return(false);
                    }


   return(true);
  }
//+------------------------------------------------------------------+
void ParseZmqMessage(string& message, string& retArray[])
  {

//Print("Parsing: " + message);

   string sep = "|";
   ushort u_sep = StringGetCharacter(sep,0);

   int splits = StringSplit(message, u_sep, retArray);

   /*
   for(int i = 0; i < splits; i++) {
      Print(IntegerToString(i) + ") " + retArray[i]);
   }
   */
  }
//+------------------------------------------------------------------+

//+------------------------------------------------------------------+
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
   ObjectSetInteger(0,objectName,OBJPROP_FONTSIZE,10);
   ObjectSetInteger(0,objectName,OBJPROP_WIDTH,1000);


  }
//+------------------------------------------------------------------+

//+------------------------------------------------------------------+

//+------------------------------------------------------------------+

//+------------------------------------------------------------------+

//+------------------------------------------------------------------+
