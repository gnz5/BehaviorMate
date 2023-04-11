from UdpComm import UdpComm
import time
import json

total_time  = 0;
comm = UdpComm("192.168.1.101", 5000)
message = json.dumps({'valves':{'pin':52, 'action':'create'}})
comm.sendMessage(message)
message = {'valves':{'pin':52, 'action':'open', 'duration': 100}}
message = {'fail': 'test'}
frame_delay = 0.0005
msg_count = 0
start = time.time()
try:
    while (total_time < 120):
        message['id'] = 1000+msg_count
        message['fail'] = msg_count
        comm.sendMessage(json.dumps(message))
        msg_count += 1
        total_time += frame_delay
        time.sleep(frame_delay)
except:
    print '\n\n%d messages sent in %.2f seconds' % (msg_count, time.time()-start)
