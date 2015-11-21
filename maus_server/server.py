#!/usr/bin/env python

import bluetooth as bt
import pyautogui

X_SCALE_FACTOR = 40
Y_SCALE_FACTOR = 40

server_sock = bt.BluetoothSocket(bt.RFCOMM)
server_sock.bind(("", bt.PORT_ANY))
server_sock.listen(1)

port = server_sock.getsockname()[1]

uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

print bt.is_valid_uuid(uuid)

bt.advertise_service(
    server_sock, "SampleServer",
    service_id=uuid,
    # service_classes=[uuid, bt.SERIAL_PORT_CLASS],
    # profiles=[bt.SERIAL_PORT_PROFILE],
)

client_sock, address = server_sock.accept()
print("Accepted connection from {0}".format(address))

data = "!"

while data:
    data = client_sock.recv(1024)
    data_parts = [p for p in str(data).split('\n') if p]
    for part in data_parts:
        print("Data received:", part)
        k, v = part.split(':')
        if k == 'dx':
            value = float(v)
            move_amt = round(Y_SCALE_FACTOR * value)
            print 'x::{0}'.format(move_amt)
            pyautogui.moveRel(move_amt, None)
        if k == 'dy':
            move_amt = round(Y_SCALE_FACTOR * value)
            print 'y::{0}'.format(move_amt)
            pyautogui.moveRel(None, move_amt)


client_sock.close()
server_sock.close()
