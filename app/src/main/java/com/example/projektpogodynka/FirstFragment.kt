package com.example.projektpogodynka

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import kotlin.math.roundToInt


class FirstFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the data
        callApi(view, "Gliwice")

        view.findViewById<Button>(R.id.switchSenior).apply {
            setOnClickListener {
                view.findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            }
        }


        val cityInput: EditText = view.findViewById(R.id.cityInputSenior)
        val searchButton: Button = view.findViewById(R.id.searchButtonSenior)
        searchButton.setOnClickListener {
            hideKeyboard()
            callApi(view, cityInput.text.toString())
        }
    }

    fun callApi(view: View, city: String) {
        val request = Request.Builder()
            .url(
                "https://api.openweathermap.org/data/2.5/weather?q=" +
                        "${city}&appid=f9ccc4a70b47d0ca77fa255a7ab53cf4&units=metric"
            )
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val weatherData = JSONObject(response.body()?.string()!!)
                if (weatherData.get("cod").equals(200)) {
                    activity?.runOnUiThread {
                        updateView(view, weatherData)
                    }
                } else {
                    println("City not found")
                }
            }
        })
    }


    fun updateView(view: View, weatherData: JSONObject) {
        println(weatherData)
        val currentCity: TextView = view.findViewById(R.id.currentCitySenior)
        currentCity.text = weatherData.get("name").toString()

        updateIcon(view, weatherData)

        val date: TextView = view.findViewById(R.id.dateSenior)
        //val monthFormatter = DateTimeFormatter.ofPattern("MM")
        val monthFormatter = DateTimeFormatter.ofPattern("MM")
        val dayFormatter = DateTimeFormatter.ofPattern("dd")
        date.text = buildString {
            append(LocalDate.now().format(dayFormatter))
            append(".")
            append(LocalDate.now().format(monthFormatter))
        }

        val description: TextView = view.findViewById(R.id.weatherDescription)
        description.text = JSONObject(
            weatherData.getJSONArray("weather").get(0)
                .toString()
        ).get("description").toString()

        val temperature: TextView = view.findViewById(R.id.temperatureValueSenior)
        var temperatureValue = JSONObject(
            weatherData.getJSONObject("main")
                .toString()
        ).get("temp").toString()
        temperatureValue = temperatureValue.toDouble().roundToInt().toString()
        temperatureValue += "° C"
        temperature.text = temperatureValue


        val pressure: TextView = view.findViewById(R.id.pressureValueSenior)
        pressure.text = buildString {
            append(
                JSONObject(
                    weatherData.getJSONObject("main")
                        .toString()
                ).get("pressure").toString()
            )
            append(" hPa")
        }

        val sunrise: TextView = view.findViewById(R.id.sunriseValueSenior)
        val timestampSunrise: Long = JSONObject(weatherData.getJSONObject("sys").toString())
            .get("sunrise").toString().toLong() + 7200
        sunrise.text = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(timestampSunrise))
            .substring(11, 16)

        val sunset: TextView = view.findViewById(R.id.sunsetValueSenior)
        val timestampSunset: Long = JSONObject(weatherData.getJSONObject("sys").toString())
            .get("sunset").toString().toLong() + 7200
        sunset.text = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(timestampSunset))
            .substring(11, 16)

    }

    fun updateIcon(view: View, weatherData: JSONObject) {
        val weatherIcon: ImageView = view.findViewById(R.id.weatherIconSenior)
        val iconNumber = JSONObject(weatherData.getJSONArray("weather").get(0).toString())
            .get("icon")
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        var image: Bitmap?
        executor.execute {
            val imageURL = "https://openweathermap.org/img/wn/${iconNumber}@2x.png"
            try {
                val `in` = java.net.URL(imageURL).openStream()
                image = BitmapFactory.decodeStream(`in`)
                handler.post {
                    weatherIcon.setImageBitmap(image)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
