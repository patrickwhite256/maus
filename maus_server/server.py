#!/usr/bin/env python

import bluetooth as bt
import pyautogui

X_SCALE_FACTOR = 10
Y_SCALE_FACTOR = -10

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
x_v = 0
y_v = 0

while data:
    data = client_sock.recv(1024)
    data_parts = [p for p in str(data).split('\n') if p]
    for part in data_parts:
        k, v = part.split(':')
        if k == 'dx':
            x_v += float(v)
            if x_v > 0.3:
                print 'x::{0}'.format(x_v)
        if k == 'dy':
            y_v += float(v)
            if y_v > 0.3:
                print 'y::{0}'.format(y_v)
        x_v *= 0.7
        y_v *= 0.7
        print(x_v, y_v)
        pyautogui.moveRel(x_v * X_SCALE_FACTOR, y_v * Y_SCALE_FACTOR)


client_sock.close()
server_sock.close()
