/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * Copyright (C) 2017-2019 Dmitry Zavalishin, dz@dz.ru
 *
 * Topic name match
 *
 *
**/


#include "config.h"

#include <string.h>

#include "mqtt_udp.h"


#define true 1
#define false 0

/**
 * 
 * @brief Compare topic name against wildcard or topic name.
 * 
 * @param in filter Topic name or wildcard to compare with.
 * 
 * @param in topicName Topic name to compare.
 * 
 * @return Non-zero if topicName matches filter.
 * 
**/
int mqtt_udp_match( char *filter, char *topicName )
{

    int tc = 0;
    int fc = 0;

    int tlen = strnlen( topicName, PKT_BUF_SIZE );
    int flen = strnlen( filter, PKT_BUF_SIZE );

    while(1)
    {
        // begin of path part

        if( filter[fc] == '+')
        {
            fc++; // eat +
            // matches one path part, skip all up to / or end in topic
            while( (tc < tlen) && (topicName[tc] != '/') )
                tc++; // eat all non slash

            // now either both have /, or both at end

            // both finished
            if( (tc == tlen) && ( fc == flen ) )
                return true;

            // one finished, other not
            if( (tc == tlen) != ( fc == flen ) )
                return false;

            // both continue
            if( (topicName[tc] == '/') && (filter[fc] == '/') )
            {
                tc++; fc++;
                continue; // path part eaten
            }
            // one of them is not '/' ?
            return false;
        }

        // TODO check it to be at end?
        // we came to # in filter, done
        if( filter[fc] == '#')
            return true;

        // check parts to be equal
        while(true)
        {
            // both finished
            if( (tc == tlen) && ( fc == flen ) )
                return true;

            // one finished
            if( (tc == tlen) || ( fc == flen ) )
                return false;

            // both continue
            if( (topicName[tc] == '/') && (filter[fc] == '/') )
            {
                tc++; fc++;
                break; // path part eaten
            }
            // both continue

            if( topicName[tc] != filter[fc] )
            {
                return false;
            }

            // continue
            tc++; fc++;
        }


    }




}
