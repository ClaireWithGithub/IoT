# IoT
## The project uses a RESTful web interface (Open Weather Map) to retrieve and process weather data, and will update the Android app to pass pieces of this retrieved weather data on to the Raspberry Pi along the MQTT connection.
<p>
### The app is capable of both MQTT communication with the Raspberry Pi and RESTful calls to the OpenWeatherMap API. Retrieve weather data from OpenWeatherMap. Then manually switch the WiFi network of your Android device. Used a weather topic in MQTT to send some bit of weather information to the Raspberry Pi. Print it to the terminal. Used a steps topic to send back a publication from the Raspberry Pi to the Android device anytime weather data is received. Display the received steps in the Android app.
