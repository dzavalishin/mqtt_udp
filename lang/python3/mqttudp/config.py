import configparser
config = configparser.ConfigParser()


config['openhab-gate'] = {'port': "8080" }

config['mqtt-gate'] = {
#    'host': 'smart.',
    'port': "1883",
    'subscribe': '#'
    }


config.read('mqtt-udp.ini')


#for key in config['mqtt-gate']:  
#    print(key)
def dump():
    for sec in config:  
        print( '['+ sec + ']' )
        for key in config[sec]:
            print( '\t' + key + "=" + config[sec][key] )




