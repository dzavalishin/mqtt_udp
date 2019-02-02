/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 *
 * Copyright (C) 2017-2019 Dmitry Zavalishin, dz@dz.ru
 *
 * @file
 * @brief Passive remote configuration.
 * 
 * Device keeps configuration items locally in file/flash/nvram.
 * Configuration software (for example, /tools/viewer) requests
 * list of configurable topics (SUBSCRIBE $SYS/#) and device
 * responds with PUBLISH for all configuration topics. Config
 * software then can set config parameters values with PUBLISH.
 *
 * @see ru.dz.mqtt_udp.config.Controller Java class
 *
**/

#include "config.h"
#include "mqtt_udp.h"
#include "mqtt_udp_defs.h"

#include <stdint.h>
#include <stdio.h>
#include <string.h>

/// Topic prefix for remote configuration topics
//#define SYS_CONF_PREFIX "$SYS/conf"


static int rconfig_listener( struct mqtt_udp_pkt *pkt );

static void rconfig_send_topic_list( void );
static void rconfig_send_topic_by_pos( int pos );

static int rconfig_find_by_topic( const char *topic );
static void rconfig_read_all( void );

static int find_by_full_topic( const char *topic );


static char * rconfig_mac_address_string = 0;

static char * topic_prefix = 0;
static int topic_prefix_len = 0;

static mqtt_udp_rconfig_rw_callback user_rw_callback;

static mqtt_udp_rconfig_item_t * rconfig_list;
static int rconfig_list_size;

/** 
 * 
 * @brief Called from user code to setup remote configuration.
 * 
 * @param mac_address_string   MAC address of current device (packed: "020698010000") or other unique id for current node.
 * @param cb                   Callback function to call when remote config engine needs parameter to be loaded or saved.
 * @param rconfig_items        Pointer to array of configurable items.
 * @param n_items              Number of elements in rconfig_items.
 * 
 * @return 0 on success, error code on error.
 * 
**/
int mqtt_udp_rconfig_client_init(char *mac_address_string, mqtt_udp_rconfig_rw_callback cb, mqtt_udp_rconfig_item_t *rconfig_items, int n_items )
{
    //printf( "RConfig client init with mac '%s'\n", mac_address_string );

    rconfig_list = rconfig_items;
    rconfig_list_size = n_items;

    user_rw_callback = cb;

    rconfig_mac_address_string = strdup( mac_address_string );
    if( rconfig_mac_address_string == 0 )
        return mqtt_udp_global_error_handler( MQ_Err_Memory, -1, "out of mem for mac str", rconfig_mac_address_string );

    int mac_len = strnlen( rconfig_mac_address_string, PKT_BUF_SIZE );
    if( rconfig_mac_address_string[mac_len] )
        return mqtt_udp_global_error_handler( MQ_Err_Memory, -1, "mac str too long", rconfig_mac_address_string );

    topic_prefix_len = mac_len + 11;


    topic_prefix = malloc( topic_prefix_len + 1 );

    if(topic_prefix == 0)
        return mqtt_udp_global_error_handler( MQ_Err_Memory, -1, "out of mem for topic", rconfig_mac_address_string );

    //sprintf( topic_prefix, "$SYS/%s/conf/", rconfig_mac_address_string );
    sprintf( topic_prefix, SYS_CONF_PREFIX "/%s/", rconfig_mac_address_string );
    
    //printf("topic_prefix_len %d len %d\n", topic_prefix_len, strlen(topic_prefix));

    mqtt_udp_add_packet_listener( rconfig_listener );

    rconfig_read_all();
    rconfig_send_topic_list();
    
    return 0;
}



/**
 * 
 * @brief Set value of configuration parameter with string input.
 * 
 * @todo Convert for numeric/boolean parameters.
 * 
 * @param pos     Position in items array to set value for.
 * @param string  New parameter value.
 * 
 * @return 0 on success, or error code.
 * 
**/ 
int mqtt_udp_rconfig_set_string( int pos, char *string )
{
    if( (pos < 0) || (pos >= rconfig_list_size) ) return -1; // TODO error

    if( rconfig_list[pos].type != MQ_CFG_TYPE_STRING ) return -2; // TODO error

    int slen = strnlen( string, PKT_BUF_SIZE );

    mqtt_udp_rconfig_item_t *item = rconfig_list + pos;

    //if( 0 == item->value.s ) return -3;

    if( item->value.s ) free( item->value.s );
    //item->value.s = 0;

    item->value.s = malloc( slen );

    if( 0 == item->value.s ) return -4;

    strcpy( item->value.s, string );
    return 0;
}



//#define SYS_WILD "$SYS/#"
/**
 * 
 * @brief Process incoming packets.
 * 
 * Process PUBLISH and SUBSCRIBE requests for config items.
 * 
 * @param pkt Packet to process.
 * 
 * @return 0 on success, or error code.
 * 
**/
static int rconfig_listener( struct mqtt_udp_pkt *pkt )
{
    //printf("rconf\n");

    // Got request
    if( pkt->ptype == PTYPE_SUBSCRIBE )
    {
        // is `$SYS/#` or `$SYS/conf/#` or `$SYS/conf/{our MAC}/`
        //if( 0 == strcmp( pkt->topic, SYS_WILD ) ) { rconfig_send_topic_list(); return 0; }
        if( mqtt_udp_match( pkt->topic, topic_prefix ) ) 
        { 
            rconfig_send_topic_list(); 
            return 0; 
        }

        int pos = find_by_full_topic( pkt->topic );
        if( pos < 0 ) return 0;
        //printf("rconf got subscribe '%s' pos = %d\n", pkt->topic, pos );

        rconfig_send_topic_by_pos( pos );
    }

    // Got data
    if( pkt->ptype == PTYPE_PUBLISH )
    {
        int pos = find_by_full_topic( pkt->topic );
        if( pos < 0 ) return 0;
        //printf("rconf set '%s'='%s' pos = %d\n", pkt->topic, pkt->value, pos );

        int rc = mqtt_udp_rconfig_set_string( pos, pkt->value );
        if( rc ) mqtt_udp_global_error_handler( MQ_Err_Other, rc, "rconfig_set_string failed", pkt->value );

        //rc =
        user_rw_callback( pos, 1 ); // Ask user to write item to local storage and use it

    }

    return 0;
}


/**
 * 
 * @brief Find config item number (position in array) by full incoming topic name.
 * 
 * Topic name must include "$SYS/{MAC address}/conf/" prefix.
 * 
 * @param topic   Full topic name to parse and find.
 * 
 * @return Position in array or negative error code.
 * 
**/
static int find_by_full_topic( const char *topic )
{
    //printf("topic  '%s'\n", topic );
    //printf("prefix '%s'\n", topic_prefix );

    if( strncmp( topic_prefix, topic, topic_prefix_len ) ) return -1;

    const char *suffix = topic + topic_prefix_len;

    return rconfig_find_by_topic( suffix );
}





/**
 * 
 * @brief Send out current value for configuration item.
 * 
 * @param pos Configuration item position in array.
 * 
**/
static void rconfig_send_topic_by_pos( int pos )
{

    //printf("rconfig_send_topic_by_pos %d: ", pos );

    // TODO do more
    if( rconfig_list[pos].type != MQ_CFG_TYPE_STRING )
        return;

    const char *subtopic = rconfig_list[pos].topic;

    char topic[80];
    //snprintf( topic, sizeof(topic)-1, "$SYS/%s/conf/%s", rconfig_mac_address_string, subtopic );
    sprintf( topic, SYS_CONF_PREFIX "/%s/%s", rconfig_mac_address_string, subtopic );

    char *val = rconfig_list[pos].value.s;

    if( val == 0 ) val = "";

    mqtt_udp_send_publish( topic, val );
    //printf("'%s'='%s'\n", topic, val );
}



/**
 * 
 * @brief Send out current value for all configuration items.
 * 
**/
static void rconfig_send_topic_list( void )
{
    int i;
    for( i = 0; i < rconfig_list_size; i++ )
        rconfig_send_topic_by_pos( i );
}







/**
 * 
 * @brief Find config item number (position in array) by short topic name (end of full name).
 * 
 * Topic name must NOT include "$SYS/{MAC address}/conf/" prefix. Just final part.
 * 
 * @param topic  Topic name suffix to find.
 * 
 * @return Item position in array or -1 if not found.
 * 
**/
static int rconfig_find_by_topic( const char *topic )
{
    int i;
    for( i = 0; i < rconfig_list_size; i++ )
    {
        if( 0 == strcmp( rconfig_list[i].topic, topic ) )
            return i;
    }

    return -1;
}


/**
 * 
 * @brief Request all items values from user code.
 * 
 * 
**/
static void rconfig_read_all( void )
{
    int i;
    for( i = 0; i < rconfig_list_size; i++ )
    {
        user_rw_callback( i, 0 ); // Ask user to read item from local storage
    }
}


// -----------------------------------------------------------------------
//
// Helpers for user to work with rconfig_list  
//
// Not used in lib and not required to use
//
// -----------------------------------------------------------------------


// -----------------------------------------------------------------------
//
// Topic to index and back
//
// -----------------------------------------------------------------------



/**
 *
 * @brief Find config item number (position in array) by string value.
 *
 * Used to find io channel number by topic name. Remote config item
 * supposed to contain topic name.
 *
 * It is supposed that item index is equal to io channel nubmer.
 * Usually it means that topic related items are at the beginning
 * of item array and their position in array is important.
 *
 * @param search  String (topic name?) to find in item _value_.
 *
 * @param kind    Expected kind og the item, sanity check.
 *
 * @return Item position in array or -1 if not found.
 *
**/
int rconfig_find_by_string_value( const char *search, mqtt_udp_rconfig_inetm_kind_t kind )
{
    int i;
    for( i = 0; i < rconfig_list_size; i++ )
    {
        if( rconfig_list[i].type != MQ_CFG_TYPE_STRING )
            continue;

        if( rconfig_list[i].kind != kind )
            continue;

        if( 0 == strcmp( rconfig_list[i].value.s, search ) )
            return i;
    }

    return -1;
}

/**
 *
 * @brief Get config item string by item number (position in array).
 *
 * Used to find topic for io channel by channel number.
 * supposed to contain topic name
 *
 * It is supposed that item index is equal to io channel nubmer.
 * Usually it means that topic related items are at the beginning
 * of item array and their position in array is important.
 *
 * @todo Convert other types to string?
 *
 * @param pos Position in array.
 *
 * @param kind Expected kind og the item, sanity check.
 *
 * @return Item _value_ (string) or 0 if intem type is not string.
 *
**/
const char * rconfig_get_string_by_item_index( int pos, mqtt_udp_rconfig_inetm_kind_t kind )
{
    if( rconfig_list[pos].type != MQ_CFG_TYPE_STRING )
    {
        mqtt_udp_global_error_handler( MQ_Err_Invalid, 0, "string_by_item_index !str", 0 );
        return 0;
    }

    if( rconfig_list[pos].kind != kind )
    {
        mqtt_udp_global_error_handler( MQ_Err_Invalid, 0, "string_by_item_index !kind", 0 );
        return 0;
    }

    return rconfig_list[pos].value.s;
}



