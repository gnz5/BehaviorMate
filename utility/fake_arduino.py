import sys
from time import sleep
import json
from UdpComm import UdpComm

def communicator(msg, comm):
    if msg['action'] == 'test':
        comm.sendMessage(json.dumps({'communicator':{'test':'passed'}}))

def valves(msg, comm):
    if msg['action'] == 'open':
        comm.sendMessage(json.dumps({
            'valve': {
                'action': 'open',
                'pin': msg['pin']
            }
        }))

def contexts(msg, comm):
    if msg['action'] == 'start':
        comm.sendMessage(json.dumps({
            'context': {
                'action': 'start',
                'id': msg['id']
            }
        }))
    elif msg['action'] == 'stop':
        comm.sendMessage(json.dumps({
            'context': {
                'action': 'stop',
                'id': msg['id']
            }
        }))

def main():
    comm = UdpComm('127.0.0.1', 5000, recv_port=4096)
    buf = [None]

    while True:
        comm.receiveMessage(buf)
        print(f'received {buf[0]}')
        message = json.loads(buf[0])
        for key in message.keys():
            try:
                globals()[key](message[key], comm)
            except:
                pass
        sleep(0.01)

if __name__ == '__main__':
    main()
