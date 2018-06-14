# tilt-based-gesture-typing

## Set up

### Android studio
1. Download Android studio
2. Import the project into Android studio
3. Install ADB in Android Studio: https://stackoverflow.com/questions/7609270/not-able-to-access-adb-in-os-x-through-terminal-command-not-found/27821139
4. Build and run the project

### Python
1. Install PySerial: http://pyserial.readthedocs.io/en/latest/pyserial.html

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
