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

TTL_val = 0
pkt_len = 0
pkt_size = 56

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

def receive_ping_response(send_socket, pid, timeout):

    max_buffer_size = 1024
    timeRemaining = timeout
    startTime = time.time()
    while True:

        select_op = select.select([send_socket], [], [], timeRemaining)
        #time_for_select_op = (time.time() - startTime)
        if select_op[0] == []:  # Timeout
            print("Request timed out")
            return None

        recvdPacket, recvdAddr = send_socket.recvfrom(max_buffer_size)
        timeReceived = time.time()
        icmp_header = recvdPacket[20:28]
        type, code, checksum, packetID, sequence = struct.unpack("bbHHh", icmp_header)

        version, type, payload_length, id, flags, ttl, protocol, checksum, src_ip, dest_ip = struct.unpack("!BBHHHBBHII", recvdPacket[:20])

        global TTL_val
        TTL_val = ttl

        if( timeReceived - startTime < timeout):
            if(packetID == pid):
                return (timeReceived - startTime)
        else:
            return None

    return None

def send_ping(send_socket, destination, pid):

    icmp_echo_req_size = 8
    checksum = 0
    #payload_data_size = 56
    global pkt_size
    dest_addr = socket.gethostbyname(destination)

    header = struct.pack("bbHHh", icmp_echo_req_size, 0, checksum, pid, 1)          # b- 8, H- 16, h- unsigned 16
    payload_bytes = b'*' * (pkt_size)
    msg = header + payload_bytes

    checksum = checksum_calc(msg)
    val = socket.htons(checksum)
    header = struct.pack("bbHHh", icmp_echo_req_size, 0, socket.htons(val), pid, 1)
    packet = header + payload_bytes

    send_socket.sendto(packet, (dest_addr, 1))
    global pkt_len
    pkt_len = len(packet)

def send_request(destination, timeout):

    icmp_protocol = socket.getprotobyname("icmp")
    send_socket = socket.socket(socket.AF_INET, socket.SOCK_RAW, icmp_protocol)
    pid = os.getpid() & 0xFFFF
    send_ping(send_socket, destination, pid)
    time_taken = receive_ping_response(send_socket, pid, timeout)
    send_socket.close()

    return time_taken

def main():

    global pkt_size
    wait_time = 1
    timeout = 3
    count = 4
    if len(sys.argv) >= 2:

        if("-t" in sys.argv):
            timeout = int(sys.argv[2])
            destination = sys.argv[3]


        elif ("-i" in sys.argv):
            wait_time = int(sys.argv[2])
            print("wait_time",wait_time)
            destination = sys.argv[3]


        elif ("-s" in sys.argv):
            pkt_size = int(sys.argv[2])
            destination = sys.argv[3]


        else:
            destination = sys.argv[1]


        print ("Ping to ",destination)
        print()

        for i in range(count):
            #print("PING ", i+1)
            time_taken = send_request(destination, timeout)

            if (time_taken == None):
                print("PING FAILED")
            else:
                print("Reply from ", socket.gethostbyname(destination), ", Time = ", int(time_taken*1000), "ms, bytes sent = ", pkt_size, ", ttl = ",TTL_val)

            time.sleep(wait_time)
    else:
        print("Enter destination as argument")


main()

