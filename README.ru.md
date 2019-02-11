# MQTT/UDP
Упрощённая версия MQTT поверх UDP: Брокер - это сеть!

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/c498fc36dbea4e41a05f4ba5a8c0ff96)](https://www.codacy.com/app/dzavalishin/mqtt_udp?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dzavalishin/mqtt_udp&amp;utm_campaign=Badge_Grade) 
[![Build Status](https://travis-ci.org/dzavalishin/mqtt_udp.svg?branch=master)](https://travis-ci.org/dzavalishin/mqtt_udp)
[![Documentation Status](https://readthedocs.org/projects/mqtt-udp/badge/?version=latest)](https://mqtt-udp.readthedocs.io/en/latest/?badge=latest)
[![PyPI version](https://badge.fury.io/py/mqttudp.svg)](https://badge.fury.io/py/mqttudp)

[See English version / Английская версия здесь](./README.md)


MQTT - симпатичный и простой протокол, который отлично подходит для IoT и прочих умных домов.

Но его можно сделать ещё проще. Предлагамый MQTT/UDP это просто
пакеты MQTT Publish, передаваемые бродкастом через UDP.

## Плюсы MQTT/UDP

*   Невероятно просто.
*   Очень быстро, при этом - минимальная возможная латентность
*   Не нужен брокер (который является потенциальной точкой отказа)
*   Низкий трафик в сети - один пакет доставляет данные всем получателям сразу
*   Разумная надёжность (особенно если мы применяем его для сенсоров, которые, как правило, регулярно высылают новые измерения)
*   Можно использовать на самом слабом железе (не требуется реализация TCP, достаточно только UDP и только на отправку)
*   Минимальная потребность в конфигурировнии.

## Для дальнейшего чтения

*   [MQTT/UDP Wiki](../../wiki)
*   [Документация](https://mqtt-udp.readthedocs.io/en/latest/) - пока только на английском, извиняюсь.

## Что в этом репозитории

*   Простая реализация протокола на нескольких популярных языках
*   Минимальная версия гейта между обычным MQTT/UDP и MQTT, OpenHAB на Питоне
*   Транслятор из MQTT/UDP в OpenHub
*   [Отладочное приложение](https://github.com/dzavalishin/mqtt_udp/wiki/MQTT-UDP-Viewer-Help) чтобы смотреть MQTT/UDP трафик в сети ([tools/viewer](tools/viewer)).
*   Другие инструменты и утилиты

## Если вы хотите помочь проекту

*   Напишите реализацию на вашем любимом языке.
*   Напишите более полноценный гейт в классический MQTT или иной разумный протокол
*   Добавьте поддержку MQTT/UDP в ваш любимый брокер MQTT или IoT систему (OpenHAB?)

Это несложно, протокол действительно невероятно простой.

## Причины НЕ использовать MQTT/UDP

*   Большой объём данных пакета. Если включается фрагментирование, надёжность падает. А некоторые реализации UDP/IP вообще не умеют фрагментирование.
*   Необходимость быть уверенным в доставке (событийные пакеты, а не апдейт меняющихся значений)

## Варианты расширения MQTT/UDP

*   Было бы здорово добавить цифровую подпись, чтобы защититься от подделки отправителя.
*   Заменить бродкаст на мультикаст. Опять же, помощь приветствуется.

## Как начать работу

*   Клонируйте или скачайте репозиторий к себе на диск (зелёная кнопка Clone of Download)
*   Прочитайте файл [HOWTO](https://raw.githubusercontent.com/dzavalishin/mqtt_udp/master/HOWTO)

## Инструментарий разработчика

В этом репозитории есть также инструменты для поддержки интегрирования протокола MQTT/UDP в Ваши программы:

*   GUI программа для отображения трафика пакетов и последнего состояния по каждому топику в каталоге [tools/viewer](https://github.com/dzavalishin/mqtt_udp/tree/master/tools/viewer); см. также [руководство по этой программе](https://github.com/dzavalishin/mqtt_udp/wiki/MQTT-UDP-Viewer-Help).
*   Генератор пакетов со случайным содержанием (random_to_udp.py) в каталоге [lang/python3/examples](https://github.com/dzavalishin/mqtt_udp/tree/master/python3/examples) 
*   Генератор/проверяльщик пакетов с последовательными номерами. Примеры seq_storm_send.py and seq_storm_check.py в каталоге [python3/examples](https://github.com/dzavalishin/mqtt_udp/tree/master/python3/examples) 
*   Модуль разбора протокола для WireShark чтобы смотреть пакеты MQTT/UDP; в каталоге [lang/lua/wireshark](https://github.com/dzavalishin/mqtt_udp/tree/master/lua/wireshark)

## Примеры использования

### Python

**Send data:**

```python
import mqttudp.engine

if __name__ == "__main__":
    mqttudp.engine.send_publish( "test_topic", "Hello, world!" )
```

**Listen for data:**

```python
import mqttudp.engine

def recv_packet(ptype,topic,value,pflags,addr):
    if ptype != "publish":
        print( ptype + ", " + topic + "\t\t" + str(addr) )
        return
    print( topic+"="+value+ "\t\t" + str(addr) )

if __name__ == "__main__":
    mqttudp.engine.listen(recv_packet)
```

Скачать [пакет pypi](https://pypi.org/project/mqttudp/)


### Java

**Send data:**

```java
PublishPacket pkt = new PublishPacket(topic, value);
pkt.send();

```

**Listen for data:**


```java
PacketSourceServer ss = new PacketSourceServer();
ss.setSink( pkt -> { System.out.println("Got packet: "+pkt); });

```

Скачать [JAR](https://github.com/dzavalishin/mqtt_udp/blob/master/build/mqtt_udp-0.4.1.jar)


### C

**Send data:**

```c
int rc = mqtt_udp_send_publish( topic, value );

```

**Listen for data:**

```c

int main(int argc, char *argv[])
{
    ...

    int rc = mqtt_udp_recv_loop( mqtt_udp_dump_any_pkt );

    ...
}

int mqtt_udp_dump_any_pkt( struct mqtt_udp_pkt *o )
{

    printf( "pkt %x flags %x, id %d",
            o->ptype, o->pflags, o->pkt_id
          );

    if( o->topic_len > 0 )
        printf(" topic '%s'", o->topic );

    if( o->value_len > 0 )
        printf(" = '%s'", o->value );

    printf( "\n");
}


```


### Lua


**Send data:**


```lua
local mq = require "mqtt_udp_lib"
mq.publish( topic, val );

```

**Listen for data:**


```lua
local mq = require "mqtt_udp_lib"

local listener = function( ptype, topic, value, ip, port )
    print("'"..topic.."' = '"..val.."'".."	from: ", ip, port)
end

mq.listen( listener )
```

Скачать [LuaRock](http://luarocks.org/modules/dzavalishin/mqttudp)



