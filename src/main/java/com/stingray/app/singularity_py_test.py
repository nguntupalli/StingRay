## Demo script to send notifications from Python

import requests
import json
from pprint import pprint

print("Connecting to server\n")

baseURL = "http://informee.net"
method = "/token"
headers = {'content-type': 'application/x-www-form-urlencoded'}

data = "username=user@user.com&password=User$23&grant_type=password"

print("Attempting login\n")

response = requests.get(baseURL + method, data = data,headers = headers)

result = json.loads(response.text)

token = result["access_token"]

print("Login succeeded\n")

print("Server returned token: " + token)

method = "/api/notifications/SendNotification"
headers = {'content-type':'application/json','Authorization':'Bearer ' + token}

data = {
	'notificationName':'KD-PY-Scheduled-Test2',
	'notificationBody':'Body',
	'notificationSubject':'Subject',
	'sendToUser':'user@user.com',
	'scheduledDate':'9/3/2016 12:00:00 AM',
	'notificationTypeID':''
	}

print("Sending new notification\n")

response = requests.post(baseURL + method, headers=headers, data=json.dumps(data))

if (response.status_code != 200):
    print("Send notification fail\n")
    print(response.text + "\n")
else:
    result = response.text
    print("Send notification success. Notification ID " + result + "\n")
