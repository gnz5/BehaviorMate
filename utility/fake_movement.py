from UdpComm import UdpComm
from time import sleep
import json

total_time  = 0;
comm = UdpComm("127.0.0.1", 5005)
behavior_comm = UdpComm("127.0.0.1", 5000)
position_message = json.dumps({'position':{'dy':25}})
lick_message = json.dumps({'lick':{'action':'start'}})
frame_delay = 0.02

lick_delay = 1
lick_interval = 0.1

while (total_time < 150):
    comm.sendMessage(position_message)
    if total_time > lick_delay:
        behavior_comm.sendMessage(lick_message)
        lick_delay += lick_interval

    total_time += frame_delay
    sleep(frame_delay)
