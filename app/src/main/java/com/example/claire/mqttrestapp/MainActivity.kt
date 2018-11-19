package com.example.claire.mqttrestapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage

import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.squareup.picasso.Picasso


class MainActivity : AppCompatActivity() {
    // I'm using lateinit for these widgets
    // because I read that repeated calls to findViewById
    // are energy intensive
    lateinit var textView: TextView
    lateinit var syncButton: Button
    lateinit var retrieveButton: Button
    lateinit var textView0: TextView
    lateinit var textView1: TextView
    lateinit var textView2: TextView

    lateinit var imageView0: ImageView


    lateinit var queue: RequestQueue
    lateinit var gson: Gson
    lateinit var mostRecentWeatherResult: WeatherResult

    // I'm doing a late init here because I need this to be an instance variable but I don't
    // have all the info I need to initialize it yet
    lateinit var mqttAndroidClient: MqttAndroidClient

    // you may need to change this depending on where your MQTT broker is running
    val serverUri = "tcp://192.168.4.20"
    // you can use whatever name you want to here
    val clientId = "AndroidClient"

    //these should "match" the topics on the "other side" (i.e, on the Raspberry Pi)
    val subscribeTopic = "steps"
    val publishTopic = "weather"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = this.findViewById(R.id.text)
        syncButton = this.findViewById(R.id.syncButton)

        // when the user presses the syncbutton, this method will get called
        syncButton.setOnClickListener({syncWithPi()})

        textView0 = this.findViewById(R.id.text0)
        textView1 = this.findViewById(R.id.text1)
        textView2 = this.findViewById(R.id.text2)

        imageView0 = this.findViewById(R.id.image0)

        retrieveButton = this.findViewById(R.id.retrieveButton)
        retrieveButton.setOnClickListener({ requestWeather() })

        queue = Volley.newRequestQueue(this)
        gson = Gson()

        // initialize the paho mqtt client with the uri and client id
        mqttAndroidClient = MqttAndroidClient(getApplicationContext(), serverUri, clientId);

        // when things happen in the mqtt client, these callbacks will be called
        mqttAndroidClient.setCallback(object: MqttCallbackExtended {

            // when the client is successfully connected to the broker, this method gets called
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                println("Connection Complete!!")
                // this subscribes the client to the subscribe topic
                mqttAndroidClient.subscribe(subscribeTopic, 0)



                // this msg payload will send to RPi and RPi can receive it from on_message func in python file
                //val message0 = MqttMessage()
                val message1 = MqttMessage()
                //message0.payload = (""+textView0.text).toByteArray()
                message1.payload = (""+textView0.text).toByteArray()

                // this publishes a message to the publish topic
                //mqttAndroidClient.publish(publishTopic, message0)
                mqttAndroidClient.publish(publishTopic, message1)

            }

            // this method is called when a message is received that fulfills a subscription
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                textView.setText(message.toString())
                println(message)
            }

            override fun connectionLost(cause: Throwable?) {
                println("Connection Lost")
            }

            // this method is called when the client succcessfully publishes to the broker
            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                println("Delivery Complete")
            }
        })
        // this method just connects the paho mqtt client to the broker
    }
    fun requestWeather(){
        val url = StringBuilder("https://api.openweathermap.org/data/2.5/weather?id=4254010&appid=e62689bdb1b1f510424fb370506b1436").toString()
        val stringRequest = object : StringRequest(com.android.volley.Request.Method.GET, url,
                com.android.volley.Response.Listener<String> { response ->
                    //textView.text = response
                    mostRecentWeatherResult = gson.fromJson(response, WeatherResult::class.java)
                    textView0.text = "weather description: "+mostRecentWeatherResult.weather.get(0).description
                    textView1.text = "humidity: "+mostRecentWeatherResult.main.humidity.toString()
                    textView2.text = "icon: "+mostRecentWeatherResult.weather.get(0).icon

                    //Picasso allows for hassle-free image loading in your application—often in one line of code!
                    //Picasso.get().load("http://openweathermap.org/img/w/"+mostRecentWeatherResult.weather.get(0).icon+".png").into(imageView0);
                    Picasso.with(this)
                            .load("http://openweathermap.org/img/w/"+mostRecentWeatherResult.weather.get(0).icon+".png")
                            .into(imageView0);

                },
                com.android.volley.Response.ErrorListener { println("******That didn't work!") }) {}
        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }
    fun syncWithPi(){
        println("+++++++ Connecting...")
        mqttAndroidClient.connect()
    }
}
class WeatherResult(val id: Int, val name: String, val cod: Int, val coord: Coordinates, val main: WeatherMain, val weather: Array<Weather>)
class Coordinates(val lon: Double, val lat: Double)
class Weather(val id: Int, val main: String, val description: String, val icon: String)
class WeatherMain(val temp: Double, val pressure: Double, val humidity: Int, val temp_min: Double, val temp_max: Double)

