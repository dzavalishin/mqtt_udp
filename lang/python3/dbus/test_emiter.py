#!/usr/bin/python
# Emmitter functionality
import dbus
import dbus.service
import dbus.glib


class Emitter(dbus.service.Object):
#   Emitter DBUS service object

    def __init__(self, bus_name, object_path):
#   Initialize the emitter DBUS service object
        dbus.service.Object.__init__(self, bus_name, object_path)
        self.dummy = ""

    @dbus.service.signal('tld.domain.sub.event')
    def test(self):
#   Emit a test signal.
        print( 'Emitted a test signal' )
        self.dummy = ""

    @dbus.service.signal('tld.domain.sub.event')
    def quit_signal(self):
#   Emit a quit signal
        print( 'Emitted a quit signal' )
        self.dummy = ""


# Emit a test signal on the dbus.
# Emit a receiver_quit signal which should stop the receiver.

session_bus = dbus.SessionBus()
bus_name = dbus.service.BusName('sub.domain.tld', bus=session_bus)
emitter = Emitter(bus_name, '/tld/domain/sub/event')
emitter.test()
emitter.quit_signal()
