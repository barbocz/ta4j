create table STRATEGY_EVALUATION
(
    STRATEGY_ID INT,
    HASHCODE INT,
    BAR_INDEX INT,
    ORDER_ID INT,
    VALUE REAL,
    BAR_TIME VARCHAR(255)
);

create index STRATEGY_EVALUATION_STRATEGY_ID_HASHCODE_INDEX
    on STRATEGY_EVALUATION (STRATEGY_ID, HASHCODE);

create table STRATEGY_PARAMETER
(
    STRATEGY_ID INT,
    TYPE VARCHAR(255),
    ACTION VARCHAR(255),
    DESCRIPTION VARCHAR(255),
    HASHCODE INT,
    SUBWINDOW_INDEX INT,
    BUFFER_INDEX INT,
    COLOR VARCHAR(255)
);

create index STRATEGY_PARAMETER_STRATEGY_ID_INDEX
    on STRATEGY_PARAMETER (STRATEGY_ID);

create table STRATEGY_TRADE_HISTORY
(
    STRATEGY_ID INTEGER,
    ORDER_ID INTEGER,
    TYPE VARCHAR(255),
    OPEN_TIME VARCHAR(255),
    OPEN_PRICE REAL,
    OPEN_AMOUNT REAL,
    CLOSE_TIME VARCHAR(255),
    CLOSE_PRICE REAL,
    CLOSE_AMOUNT REAL,
    CLOSE_BY VARCHAR(255),
    DURATION INTEGER,
    PROFIT REAL,
    COMMENT TEXT,
    MAX_PROFIT REAL,
    MAX_LOSS REAL
);

create index STRATEGY_TRADE_HISTORY_STRATEGY_ID_ORDER_ID_INDEX
    on STRATEGY_TRADE_HISTORY (STRATEGY_ID, ORDER_ID);

create table STRATEGY
(
    STRATEGY_ID INT auto_increment,
    SOURCE VARCHAR(255),
    BAR_NUMBER INT,
    PERIOD INT,
    SYMBOL VARCHAR(255),
    TRADE_NUMBER INT,
    PROFITABLE_TRADES_RATIO REAL,
    EQUITY_MINIMUM REAL,
    BALANCE_DRAWDOWN REAL,
    OPEN_MINIMUM REAL,
    OPEN_MAXIMUM REAL,
    TOTAL_PROFIT REAL,
    TOTAL_PROFIT_PER_MONTH REAL,
    DAY_NUMBER REAL,
    BAR_PROCESSTIME REAL,
    ENTRY_STATEGY CLOB,
    EXIT_STRATEGY CLOB,
    ENTRY_STRATEGY_NAME VARCHAR(512),
    EXIT_STRATEGY_NAME VARCHAR(512),
    START_TIME VARCHAR(255),
    END_TIME VARCHAR(255),
    FIRST_BAR_TIME VARCHAR(255),
    LAST_BAR_TIME VARCHAR(255),
    LAST_CLOSE_PRICE REAL
);

