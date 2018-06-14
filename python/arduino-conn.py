import random
import serial
from one_euro_filter import OneEuroFilter

def filter_data(data, timestamp, f1, f2, f3):
    dataArr = data.split()
    yaw = float(dataArr[0].decode("utf-8"))
    row = float(dataArr[1].decode("utf-8"))
    pitch = float(dataArr[2].decode("utf-8"))

    x = dataArr[3].decode("utf-8")
    y = dataArr[4].decode("utf-8")
    z = dataArr[5].decode("utf-8")

    ind = dataArr[6].decode("utf-8")
    
    filtered_yaw = f1(yaw, timestamp)
    filtered_row = f2(row, timestamp)
    filtered_pitch = f3(pitch, timestamp)

    values = [
        str(filtered_yaw),
        str(filtered_row),
        str(filtered_pitch),
        x, y, z, ind
    ]
    join_str = " ".join(values)
    filtered_data = join_str.encode("utf-8")

    return filtered_data

if __name__=="__main__":
    arduino = serial.Serial('/dev/cu.usbserial-A105T217', 115200, timeout=.1)

    config = {
        'freq': 120,       # Hz
        'mincutoff': 1,  # FIXME
        'beta': 0,       # FIXME
        'dcutoff': 1.0     # this one should be ok
        }

    f1 = OneEuroFilter(**config)
    f2 = OneEuroFilter(**config)
    f3 = OneEuroFilter(**config)

    timestamp = 0.0 # seconds
    while True:
        data = arduino.readline()[:-2]

        if data:
            filtered_data = filter_data(data, timestamp, f1, f2, f3)
            print(filtered_data)
            print(data)
            timestamp += 1.0/config["freq"]