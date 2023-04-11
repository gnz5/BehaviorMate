from UdpComm import UdpComm
import numpy as np
import json
import time

comm = UdpComm("192.168.1.101", 1234, recv_port=5000)
msg_list = []

try:
    while True:
        buf=[None]
        comm.receiveMessage(buf)
        if buf[0] is not None:
            msg = json.loads(buf[0])
            print(msg);
            if 'fail' in msg.keys():
                msg_list.append(msg['fail'])

        time.sleep(0.00001)
except:
    missed = (msg_list[-1]+1)-len(msg_list)
    print('\n\n%d messages received\n%d missed (%.2f%%)' % (
        len(msg_list), missed, (100.0*missed)/msg_list[-1]))
