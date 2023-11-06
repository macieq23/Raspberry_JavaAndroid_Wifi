import network
import socket
import machine
import ujson

#from mywifi import networksetting

#ssid, password = networksetting()

wlan = network.WLAN(network.STA_IF)
wlan.active(True)
wlan.connect("wifi_name", "wifi_password")
    
max_wait = 0
print('Waiting for connection')
while max_wait > 0:
    if wlan.status() < 0 or wlan.status() >= 3:
        break
    max_wait -= 1    
    time.sleep(1)
status = None
if wlan.status() != 3:
    raise RuntimeError('Connections failed')
else:
    status = wlan.ifconfig()
    print('connection to internet ','succesfull established!', sep=' ')
    print('IP-adress: ' + status[0])
ipAddress = status[0]


led = machine.Pin(10, machine.Pin.OUT)

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind(('0.0.0.0', 5000))
s.listen(1)
print('Waiting for a connection...')

while True:
    conn, addr = s.accept()
    print('Connection from', addr)

    # Receive a command from the client
    data = conn.recv(1024).decode()
    print(data)
    command = ujson.loads(data)
    print(command)

    # Turn the GPIO pin on or off based on the received command
    if command['gpio'] == 'on':
        print('Turning LED on...')
        led.value(1)
    elif command['gpio'] == 'off':
        print('Turning LED off...')
        led.value(0)
    else:
        print('Error')
    conn.close()