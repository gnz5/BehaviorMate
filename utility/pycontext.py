import sys
import json

import argparse

from UdpComm import UdpComm



def test(ip, port):
    lick = {
            'sensors': {
                'action':'create',
                'pin': 2,
                'report_pin': 4,
                'type': 'lickport'
            }
        }
    context2 = {
            'contexts': {
                'action': 'create',
                'id': 'test_context',
                'type': '_pulsed',
                'valves': [7],
                'durations': [500]
            }
        }

    start_context = {
        'contexts': {
            'action': 'start',
            'id': 'test_context'
         }
     }

    stop_context = {
        'contexts': {
            'action': 'stop',
            'id': 'test_context'
         }
     }

    comm = UdpComm(ip, port)
    comm.sendMessage(json.dumps(lick))
    comm.sendMessage(json.dumps(context2))
    comm.sendMessage(json.dumps(start_context))
    comm.sendMessage(json.dumps(stop_context))

def testPiezo(ip, port):
    lick = {
            'sensors': {
                'action':'create',
                'pin': 0,
                'report_pin': 5,
                'type': 'piezoport',
                'lick_threashold': 3,
                'lick_duration': 200
            }
        }

    valve = {
        'valves': {
            'action': 'create',
            'pin': 7,
        }
    }

    context = {
        'contexts': {
            'action': 'create',
            'id': 'test_context',
            'type': 'operant',
            'operant_rate': 5,
            'sensor': 0,
            'valves': [7],
            'initial_open': 0,
            'durations': [50],
            '_timeout': 3000
        }
    }

    start_context = {
        'contexts': {
            'action': 'start',
            'id': 'test_context'
         }
     }


    comm = UdpComm(ip, port)
    comm.sendMessage(json.dumps(lick))
    comm.sendMessage(json.dumps(valve))
    comm.sendMessage(json.dumps(context))
    comm.sendMessage(json.dumps(start_context))


def startContext(comm):
    start_context = {
        'contexts': {
            'action': 'start',
            'id': 'test_context'
         }
     }


def testOperantContext(ip, port):
    comm = UdpComm(ip, port)
    sensor = {
        'sensors': {
            'action':'create',
            'pin': 3
        }
    }

    valve = {
        'valves': {
            'action': 'create',
            'pin': 7,
            'frequency': 262

        }
    }

    context = {
        'contexts': {
            'action': 'create',
            'id': 'test_context',
            'type': 'operant',
            'operant_rate': 2,
            'sensor': 3,
            'valves': [5],
            'initial_open': 0,
            'durations': [500],
            'timeout': 3000
        }
    }

    start_context = {
        'contexts': {
            'action': 'start',
            'id': 'test_context'
         }
     }


    comm.sendMessage(json.dumps(sensor))
    comm.sendMessage(json.dumps(valve))
    comm.sendMessage(json.dumps(context))
    comm.sendMessage(json.dumps(start_context))

def main(argv):
    #argP=argparse.ArgumentParser();
    
    test("192.168.1.101", 5000)

if __name__ == '__main__':
    main(sys.argv[1:])
