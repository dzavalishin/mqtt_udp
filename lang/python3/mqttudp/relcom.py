import mqttudp.mqtt_udp_defs as defs
import mqttudp.engine as me
import mqttudp.engine.Paket as Paket
import mqttudp.engine.PaketType as PaketType

__outgoing = {}



def publish( topic, value ):
    global __outgoing
    pkt = Packet()
    pkt.topic = topic
    pkt.value = value




#
# Must be called from main program with each packet received
#
def recv_packet(pkt):
    if pkt.ptype == PacketType.PubAck:
        print( "ack " + pkt.reply_to + "\t\t" + str(addr) )
        send_asked_rconf_items( pkt.topic )
        return



__sender = threading.Thread(target=relcom_send_thread, args=())
__sender.start()
