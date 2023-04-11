from UdpComm import UdpComm
import pygtk
import json
import gtk

class Base:
    def __init__(self, behavior_comm):
        self.window = gtk.Window(gtk.WINDOW_TOPLEVEL)
        self._behavior_comm = behavior_comm
        
        self.box1 = gtk.VBox(False, 0)
        self.window.add(self.box1)

        self.hbox1 = gtk.HBox(False, 0)
        self.pin_label = gtk.Label("Pin:")
        self.pin_label.set_width_chars(10)
        self.pin_entry = gtk.Entry(max=0)
        self.pin_entry.set_text("7")
        self.hbox1.pack_start(self.pin_label, True, True, 0)
        self.hbox1.pack_start(self.pin_entry, True, True, 0)
        self.box1.pack_start(self.hbox1, True, True, 0)

        self.hbox2 = gtk.HBox(False, 0)
        self.duration_label = gtk.Label("Duration:")
        self.duration_label.set_width_chars(10)
        self.duration_entry = gtk.Entry(max=0)
        self.duration_entry.set_text("500")
        self.hbox2.pack_start(self.duration_label, True, True, 0)
        self.hbox2.pack_start(self.duration_entry, True, True, 0)
        self.box1.pack_start(self.hbox2, True, True, 0)

        self.button = gtk.Button("open")
        self.button.connect("clicked", self.open_valve, None)
        self.box1.pack_start(self.button, True, True, 0)

        self.duration_entry.show()
        self.pin_entry.show()
        self.button.show()
        self.pin_label.show()
        self.duration_label.show()
        self.hbox1.show()
        self.hbox2.show()
        self.box1.show()
        self.window.show()

    def open_valve(self, widget, data=None):
        message = {'valves': {'pin': int(self.pin_entry.get_text()),
                'action': 'create'}}

        self._behavior_comm.sendMessage(json.dumps(message))
        message['valves']['action'] = 'open'
        message['valves']['duration'] = int(self.duration_entry.get_text())
        self._behavior_comm.sendMessage(json.dumps(message))

    def main(self):
        gtk.main()


#comm = UdpComm('127.0.0.1', 5010)
#comm.sendMessage('{"test":"passed"}')

def main():
    base = Base(UdpComm('127.0.0.1', 5010))
    base.main()

if __name__ == '__main__':
    main()
