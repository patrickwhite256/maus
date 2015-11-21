#!/usr/bin/env python

import bluetooth as bt
import pyautogui

pyautogui.FAILSAFE = False

X_SCALE_FACTOR = -10
Y_SCALE_FACTOR = 10

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
x_a = 0
y_a = 0
x_v = 0
y_v = 0

while data:
    data = client_sock.recv(1024)
    data_parts = [p for p in str(data).split('\n') if p]
    for part in data_parts:
        k, v = part.split(':')
        if k == 'dx':
            x_a += float(v)
        elif k == 'dy':
            y_a += float(v)
        elif k == 'lc':
            pyautogui.click()
        elif k == 'rc':
            pyautogui.click(button='right')
        x_v += x_a
        y_v += y_a
        x_a *= 0.7
        y_a *= 0.7
        if(x_a < 0.3):
            x_v *= 0.7
        if(y_a < 0.3):
            y_v *= 0.7
#       print(x_a, y_a, x_v, y_v)
        pyautogui.moveRel(x_v * X_SCALE_FACTOR, y_v * Y_SCALE_FACTOR)


client_sock.close()
server_sock.close()
