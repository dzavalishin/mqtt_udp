


.. _st-lang-api:

CodeSys ST Language API Reference
---------------------------------

.. NOTE::

   This implementation ise currently send only.

Sorry, due to PLC limitations, there is no clear API in this code example, just integrated protocol
and client code example. 

PLC is specific: it runs all its programs in loop and it is assumed that each program is running
without blocking and does not spend too much time each loop cycle. There's usually a watch dog
that checks for it. Hence, ST implementation is cycling, sending just one topic per loop cycle.

.. index:: single: send

Actual API is simple:

.. code-block:: pascal

    FUNCTION MQTT_SEND : BOOL
    
    VAR_INPUT
        socket          : DINT;

        topic           : STRING;
        data            : STRING;

        sock_adr_out    : SOCKADDRESS;
    END_VAR
    

    FUNCTION MQ_SEND_REAL : BOOL
    VAR_INPUT
        socket          : DINT;
        m_SAddress      : SOCKADDRESS;

        topic           : STRING;
        data            : REAL;
    END_VAR



Here is how it is used in main program:

.. code-block:: pascal

    PROGRAM MQTT_PRG
    VAR
        STEP         : INT  := 0;
        socket       : DINT := SOCKET_INVALID;
        wOutPort     : INT  := 1883;
        addr         : SOCKADDRESS;
    
    END_VAR
    
    CASE STEP OF
        0:
            socket := SysSockCreate( SOCKET_AF_INET, SOCKET_DGRAM, SOCKET_IPPROTO_UDP );

            addr.sin_family := SOCKET_AF_INET;
            addr.sin_port   := SysSockHtons( wOutPort );
            addr.sin_addr   := 16#FFFFFFFF; (* broadcast *)

        1:  MQ_SEND_REAL( socket, addr,  'PLK0_WarmWater', WarmWaterConsumption );
        2:  MQ_SEND_REAL( socket, addr,  'PLK0_ColdWater', ColdWaterConsumption );

        3:  MQ_SEND_REAL( socket, addr,  'PLK0_activePa', activePa_avg );
        4:  MQ_SEND_REAL( socket, addr,  'PLK0_Va', Va );
    
    ELSE
        IF socket <> SOCKET_INVALID THEN
            SysSockClose( socket );
        END_IF
        socket := SOCKET_INVALID;
    END_CASE

    STEP := STEP + 1;
    
    IF socket = SOCKET_INVALID THEN
        STEP := 0;
    END_IF
    
    END_PROGRAM


