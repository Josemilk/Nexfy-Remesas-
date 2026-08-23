import re

with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

intent_filters = """            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="geo" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="http" android:host="maps.google.com" />
                <data android:scheme="https" android:host="maps.google.com" />
                <data android:scheme="http" android:host="goo.gl" />
                <data android:scheme="https" android:host="goo.gl" />
            </intent-filter>"""

if "android:scheme=\"geo\"" not in content:
    content = content.replace("</intent-filter>\n        </activity>", "</intent-filter>\n" + intent_filters + "\n        </activity>")
    with open("app/src/main/AndroidManifest.xml", "w") as f:
        f.write(content)
