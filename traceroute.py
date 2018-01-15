__author__ = 'Shweta Yakkali'
# References:
# https://www.g-loaded.eu/2009/10/30/python-ping/
# https://stackoverflow.com/questions/1767910/checksum-udp-calculation-python

import socket
import os
import sys
import struct
import select
import time


hop_address =""


def carry_around_add(val1, val2):
    carry = val1 + val2
    masking = carry & 0xffff
    shift = carry >> 16

    return masking + shift

def checksum_calc(msg):
    iter_increment = 2
    convert_16 = 0
    for i in range(0, len(msg), iter_increment):

        octets = (msg[i]) + ((msg[i + 1]) << 8)
        convert_16 = carry_around_add(convert_16, octets)

    return ~convert_16 & 0xffff

def receive_ping_response(send_socket, pid, timeout, dest_addr):
    global hop_address
    max_buffer_size = 1024
    timeRemaining = timeout

    while True:
        startTime = time.time()
        select_op = select.select([send_socket], [], [], timeRemaining)

        if select_op[0] == []:  # Timeout
            #print("Request timed out")
            return


        recvdPacket, recvdAddr = send_socket.recvfrom(max_buffer_size)
        hop_address = recvdAddr[0]

        timeReceived = time.time()

        icmp_header = recvdPacket[20:28]
        type, code, checksum, packetID, sequence = struct.unpack("bbHHh", icmp_header)
        if( timeReceived - startTime < timeout):

            if(recvdAddr[0] == dest_addr):

                return timeReceived - startTime
        else:
            return None

    return None


def main():

    global hop_address
    nqueries = 3
    if(len(sys.argv) >= 2):

        if('-n' in sys.argv):
            destination = sys.argv[2]

        elif ('-q' in sys.argv):
            nqueries = int(sys.argv[2])
            destination = sys.argv[3]

        else:
            destination = sys.argv[1]

        print ("Ping to ", destination)
        count = 30
        timeout = 3
        for i in range(count):
            print("TTL = ", i+1)

            icmp_protocol = socket.getprotobyname("icmp")
            send_socket = socket.socket(socket.AF_INET, socket.SOCK_RAW, icmp_protocol)
            send_socket.setsockopt(socket.SOL_IP, socket.IP_TTL, struct.pack('I', i+1))


            pid = os.getpid() & 0xFFFF
            icmp_echo_req_size = 8
            checksum = 0
            payload_data_size = 56
            try:
                dest_addr = socket.gethostbyname(destination)
            except:
                time.sleep(5)
                print("Request Timed out. Unresolved domain, No route")
                break;
            header = struct.pack("bbHHh", icmp_echo_req_size, 0, checksum, pid, i+1)  # b- 8, H- 16, h- unsigned 16
            payload_bytes = b'*' * payload_data_size
            msg = header + payload_bytes

            checksum = checksum_calc(msg)
            val = socket.htons(checksum)

            header = struct.pack("bbHHh", icmp_echo_req_size, 0, socket.htons(val), pid, i+1)
            packet = header + payload_bytes

            #33434
            rtt_time = 0
            time_str = ''
            for nquery in range (nqueries):
                start_time_rtt = time.time()

                send_socket.sendto(packet, (dest_addr, 1))

                time_taken = receive_ping_response(send_socket, pid, timeout, dest_addr)
                rtt_time = time.time() - start_time_rtt
                time_str = time_str + str(rtt_time) + " "

            print(hop_address, "  ", time_str)

            if(time_taken != None):
                print(destination, "[", hop_address, "] reached.    Time = " + str(time_taken*1000), "  Hops = ", i+1)
                break

            send_socket.close()
            time.sleep(1)
    else:

        print("enter sys args")
main()

