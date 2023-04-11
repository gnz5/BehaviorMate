import socket
import subprocess
import argparse
import sys
import time
import json
import time

class UdpComm:
    def __init__(self, ip, send_port, recv_port=None):
        self._sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self._send_address = (ip,send_port)
        self._recv_port = recv_port
        if recv_port is not None:
            self._sock.bind(('', recv_port))

    def sendMessage(self, message):
        print('sending message: {} to {}'.format(message, self._send_address), file=sys.stderr)
        #print >>sys.stderr, 'sending message: {} to {}'.format(message, self._send_address)
        if (not self._sock.sendto(message.encode("utf-8"), self._send_address)):
            print('ERROR! unable to send message'.format(message), file=sys.stderr)
            #print >>sys.stderr, 'ERROR! unable to send message'.format(message)

    def receiveMessage(self, buf):
        buf[0] = self._sock.recv(self._recv_port)

def main(argv):
    argParser = argparse.ArgumentParser()
    argParser.add_argument(
        "-i", "--ip", action="store", type=str,
        help="ip address for the server to send to")
    argParser.add_argument(
        "user", action="store", type=str,
        help="user name for ssh")
    argParser.add_argument(
        "port", action="store", type=int,
        help="port address for the server to send to")

    args = argParser.parse_args(argv)
    server_ip = '156.111.43.151'
    server_path = '/home/jack/code/UdpComm.py'

    if not args.user == '_server':
        comm = UdpComm(server_ip, 80)
        comm._sock.connect(comm._send_address)
        self_ip = comm._sock.getsockname()[0]
        comm._sock.close()

        comm = UdpComm(server_ip, None, recv_port=args.port)
        p=subprocess.Popen(['ssh', '{}@{}'.format(args.user, server_ip), 'python',
            server_path, '-i', self_ip, '_server', str(args.port)])

        buff = [None]
        comm.receiveMessage(buff)
        print("received Message: {}".format(json.loads(buff[0])))
    else:
        comm = UdpComm(args.ip, args.port)
        comm.sendMessage(json.dumps({'from_server':'test'}))

if __name__ == '__main__':
    main(sys.argv[1:])
