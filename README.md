# tilt-based-gesture-typing

This project includes two parts. The first part is Python scripts that reads Gyro data from Arduino device and sends them to Android App via socket. The second part is the Android App which takes the Gyro data and send events to Android phone using NDK. If you need to change the C code, you need to have the NDK set up (https://developer.android.com/ndk/).

## Set up
Make sure your laptop and Android phone are connected to the same internet

### Android studio
1. Download Android studio
2. Import the project into Android studio
3. Install ADB in Android Studio: https://stackoverflow.com/questions/7609270/not-able-to-access-adb-in-os-x-through-terminal-command-not-found/27821139
4. Build and run the project
5. You should see an IP address on the Android App.

### Python
1. Install PySerial: http://pyserial.readthedocs.io/en/latest/pyserial.html
2. Connect the Arduino device via USB
3. In arduino-conn.py, change the name and port number in line 33 to the name and port number of the Arduino device. Do the same thing in client.py line 44.
4. In terminal, use python arduino-conn.py" to start the script. Make sure you see data running in the terminal. If so, stop the script.
5. In client.py line 41 change the IP address to the IP address on the Android App
6. In terminal, use command "python client.py" to start the script


## User study instruction
1. Restart the phone
2. Uninstall ‘My application’
3. Make sure both phone and laptop are connected to Linksys 2 Wi-Fi
4. Open TEMA app and create a new session with
    1. Participant id: find this in calendar
    2. Session id: e.g. day 3 session 1 = 50, day 3 session 2 = 60
    3. Number of phrases: 11
5. In GestureTypingService.java line 402 set the log file path to "/TEMA_logs/{participant_id}_{session_id}_A_events.tema"
6. Start the Android app
    1. Run > Debug app
7. If you have restarted the phone, in Android Studio terminal, do:
    1. > adb shell
    2. > su
    3. > chmod 777 /dev/input/event2
    4. > chmod 777 /dev/input
    5. > chmod 777 /dev
    6. > exit
    7. > exit
8. Start the socket connection
    1. > python client.py
9. Test if typing and deletion works, (test deletion with two words of different length) and then finish the first phrase.
10. Insert the phone into the goggle and let user start typing
11. Record the time for the session

Notes:
1. Restart the phone if there’s 30 minutes gap between users
2. Keep the phone charging whenever possible
3. Say if something went wrong on session id 50 and you need to restart the TEMA app, the next session id should be 51.
4. Everyday after all the users finished, charge the phone, and restart the phone.
