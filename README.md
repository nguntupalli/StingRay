# StingRay

### Running the test to send email notifications to users

Unix/Linux/Mac:

java -cp libs/stingray-app-1.0-SNAPSHOT-jar-with-dependencies.jar:libs/stingray-app-1.0-SNAPSHOT.jar com.stingray.app.StingRayApp http://informee.net test 100

The above command uses 3 args: "http://informee.net test 100", where first arg is the api url, 
second arg is "test" for running the notifications test, third arg is the total emails to be sent.

### Running a simple test to fetch notification types

Unix/Linux/Mac:

java -cp libs/stingray-app-1.0-SNAPSHOT-jar-with-dependencies.jar:libs/stingray-app-1.0-SNAPSHOT.jar com.stingray.app.StingRayApp http://informee.net notificationTypes

### Building the project

mvn clean package assembly:single

### TODO

Make api calls in parallel.

### Issues

1. Each api call requires a token, so to do the parallel notifications test we need to make 
two api calls - one to fetch the tokens and the other to send notification. This seems unnecessary (I think) since each token
is valid for 5 mins.

2. The JSON response returned needs to be consistent, getNotificationTypes needs to have a 
kv pair, {"data": [array of notifications]} rather than just [array of notifications] which makes client parsing hard.
