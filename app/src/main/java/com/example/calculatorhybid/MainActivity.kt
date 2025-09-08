package com.example.calculatorhybid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import net.pubnative.lite.sdk.HyBid
import net.pubnative.lite.sdk.VideoListener
import net.pubnative.lite.sdk.interstitial.HyBidInterstitialAd
import net.pubnative.lite.sdk.models.Ad
import net.pubnative.lite.sdk.models.AdSize
import net.pubnative.lite.sdk.utils.Logger
import net.pubnative.lite.sdk.views.HyBidAdView
import net.pubnative.lite.sdk.views.HyBidBannerAdView
import java.text.DecimalFormat
import net.pubnative.lite.sdk.testing.TestUtil
import net.pubnative.lite.sdk.api.RequestManager
class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var display: TextView
    private lateinit var bannerAdView: HyBidBannerAdView
    private var interstitialAd: HyBidInterstitialAd? = null

    private var currentInput = ""
    private var operator = ""
    private var firstOperand = 0.0
    private var isOperatorPressed = false
    private var calculationCount = 0

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeHyBidSDK()

    }

    private fun initializeViews() {
        Logger.e("HyBid", "Initializing views")
        display = findViewById(R.id.display)
        bannerAdView = findViewById(R.id.bannerAdView)
        Logger.e("HyBid", "Views initialized: display=$display, banner=$bannerAdView")

        // Number buttons
        val numberButtons = arrayOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        )

        // Operator buttons
        val operatorButtons = arrayOf(
            R.id.btnAdd, R.id.btnSubtract, R.id.btnMultiply, R.id.btnDivide,
            R.id.btnEquals, R.id.btnClear, R.id.btnDecimal
        )

        // Set click listeners
        numberButtons.forEach { id -> findViewById<Button>(id).setOnClickListener(this) }
        operatorButtons.forEach { id -> findViewById<Button>(id).setOnClickListener(this) }
    }

    private fun initializeHyBidSDK() {
        Logger.e("HyBid", "Initializing HyBid SDK")
        HyBid.setTestMode(true) // set before initialize
        HyBid.setReportingEnabled(true)
        HyBid.initialize("R.string.hybid_api_key", application) { success ->
            Logger.e("HyBid", "SDK initialization callback. Success=$success")
            val sdkVersion = HyBid.getSDKVersionInfo()
            Logger.e("HyBid", "Current HyBid SDK version: $sdkVersion")
            if (success) {
                handler.post {
                    initializeViews()
                    setupBannerAd()
                   loadInterstitialAd() // start loading interstitials
                }
            } else {
                Logger.e("HyBid", "Failed to initialize HyBid SDK")
                Toast.makeText(this, "HyBid SDK failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBannerAd() {
        Logger.e("HyBid", "Setting ad size and loading banner")
       // val methods = bannerAdView.javaClass.methods
        //methods.forEach { method ->
            //if (method.name.contains("load")) {
                //Logger.e("HyBid", "Available load method: ${method.name} - ${method.parameterTypes.contentToString()}")
            //}
       // }

        // Debug: Check the actual class type
        //Logger.e("HyBid", "bannerAdView class: ${bannerAdView.javaClass.name}")
        //Logger.e("HyBid", "bannerAdView superclass: ${bannerAdView.javaClass.superclass?.name}")
        // Set ad size first
        bannerAdView.setAdSize(AdSize.SIZE_320x50)

        // Log dimensions (they might be 0 if not laid out yet, but that's okay)
        Logger.e("HyBid", "Banner view width=${bannerAdView.width}, height=${bannerAdView.height}")

        val listner = object : HyBidAdView.Listener {
            override fun onAdLoaded() {
                Logger.e("HyBid", "Banner ad loaded successfully")
                Toast.makeText(this@MainActivity, "Banner ad loaded", Toast.LENGTH_SHORT).show()
            }

            override fun onAdLoadFailed(error: Throwable?) {
                Logger.e("HyBid", "Banner ad failed to load: ${error?.message}")
                Toast.makeText(this@MainActivity, "Banner ad failed", Toast.LENGTH_SHORT).show()
            }

            override fun onAdImpression() {
                Logger.e("HyBid", "Banner ad impression")
            }

            override fun onAdClick() {
                Logger.e("HyBid", "Banner ad clicked")
                Toast.makeText(this@MainActivity, "Banner ad clicked", Toast.LENGTH_SHORT).show()
            }
        }

        val ad = TestUtil.createTestBannerAd()

        bannerAdView.renderAd(ad, listner)
    }


    private fun loadInterstitialAd() {
        Logger.e("HyBid", "🔄 Starting to load interstitial ad with zone ID: 1")

        interstitialAd = HyBidInterstitialAd(
            this,
            "1",
            object : HyBidInterstitialAd.Listener {
                override fun onInterstitialLoaded() {
                    Logger.e("HyBid", "Interstitial ad loaded successfully!")
                }

                override fun onInterstitialLoadFailed(error: Throwable?) {
                    Logger.e("HyBid", "Interstitial ad FAILED to load: ${error?.message}")
                    handler.postDelayed({ loadInterstitialAd() }, 2000)
                }

                override fun onInterstitialImpression() {
                    Logger.e("HyBid", "Interstitial ad impression")
                }

                override fun onInterstitialDismissed() {
                    Logger.e("HyBid", "Interstitial ad dismissed")
                    handler.postDelayed({ loadInterstitialAd() }, 500)
                }

                override fun onInterstitialClick() {
                    Logger.e("HyBid", "Interstitial ad clicked")
                }
            }
        )

        Logger.e("HyBid", "🔄 About to call interstitialAd.load()...")
        interstitialAd?.load()
        interstitialAd?.setMediation(true);
        val testInterstitialAd = TestUtil.createTestInterstitialAd()
        interstitialAd?.prepareAd(testInterstitialAd)
        Logger.e("HyBid", "interstitialAd.load() called")
    }


    override fun onClick(view: View?) {
        Logger.e("HyBid", "Button clicked!")
        val button = view as Button
        val buttonText = button.text.toString()
        Logger.e("HyBid", "Button text: $buttonText")

        when (buttonText) {
            in "0".."9" -> {
                Logger.e("HyBid", "Number button pressed: $buttonText")
                handleNumberInput(buttonText)
            }
            "." -> {
                Logger.e("HyBid", "Decimal button pressed")
                handleDecimalInput()
            }
            "+", "-", "×", "÷" -> {
                Logger.e("HyBid", "Operator button pressed: $buttonText")
                handleOperatorInput(buttonText)
            }
            "=" -> {
                Logger.e("HyBid", "EQUALS button pressed!")
                handleEqualsInput()
            }
            "C" -> {
                Logger.e("HyBid", "Clear button pressed")
                handleClearInput()
            }
        }
    }

    private fun handleNumberInput(number: String) {
        if (isOperatorPressed) { currentInput = ""; isOperatorPressed = false }
        currentInput += number
        display.text = currentInput
    }

    private fun handleDecimalInput() {
        if (isOperatorPressed) { currentInput = ""; isOperatorPressed = false }
        if (!currentInput.contains(".")) {
            currentInput += if (currentInput.isEmpty()) "0." else "."
            display.text = currentInput
        }
    }

    private fun handleOperatorInput(op: String) {
        if (currentInput.isNotEmpty()) {
            if (operator.isNotEmpty() && !isOperatorPressed) calculateResult()
            else firstOperand = currentInput.toDouble()
        }
        operator = op
        isOperatorPressed = true
    }

    private fun handleEqualsInput() {
        Logger.e("HyBid", "=== handleEqualsInput called ===")
        Logger.e("HyBid", "currentInput: '$currentInput'")
        Logger.e("HyBid", "operator: '$operator'")
        Logger.e("HyBid", "calculationCount: $calculationCount")

        if (currentInput.isNotEmpty() && operator.isNotEmpty()) {
            Logger.e("HyBid", "Conditions met, calculating result...")
            calculateResult()
            operator = ""
            calculationCount++
            Logger.e("HyBid", "calculationCount after increment: $calculationCount")

            if (calculationCount >= 1 && interstitialAd?.isReady() == true) {
                interstitialAd?.show()
                calculationCount = 0
            } else {
                Logger.e("HyBid", "Not showing ad - calculationCount: $calculationCount, isReady: ${interstitialAd?.isReady()}")
            }
        } else {
            Logger.e("HyBid", "Conditions NOT met for calculation")
            Logger.e("HyBid", "currentInput.isNotEmpty(): ${currentInput.isNotEmpty()}")
            Logger.e("HyBid", "operator.isNotEmpty(): ${operator.isNotEmpty()}")
        }
    }

    private fun handleClearInput() {
        currentInput = ""
        operator = ""
        firstOperand = 0.0
        isOperatorPressed = false
        display.text = "0"
    }

    private fun calculateResult() {
        val secondOperand = currentInput.toDouble()
        val result = when (operator) {
            "+" -> firstOperand + secondOperand
            "-" -> firstOperand - secondOperand
            "×" -> firstOperand * secondOperand
            "÷" -> if (secondOperand != 0.0) firstOperand / secondOperand else Double.NaN
            else -> secondOperand
        }
        if (result.isNaN()) {
            display.text = "Error"
            currentInput = ""
        } else {
            val df = DecimalFormat("#.##########")
            val formatted = df.format(result)
            display.text = formatted
            currentInput = formatted
            firstOperand = result
        }
        isOperatorPressed = true
    }

    override fun onDestroy() {
        super.onDestroy()
        bannerAdView.destroy()
    }
}
