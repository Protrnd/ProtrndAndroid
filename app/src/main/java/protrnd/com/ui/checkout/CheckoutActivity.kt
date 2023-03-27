package protrnd.com.ui.checkout

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import co.paystack.android.Paystack
import co.paystack.android.PaystackSdk
import co.paystack.android.Transaction
import co.paystack.android.model.Card
import co.paystack.android.model.Charge
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.ActivityCheckoutBinding
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.isNetworkAvailable
import protrnd.com.ui.payment.PaymentViewModel

class CheckoutActivity :
    BaseActivity<ActivityCheckoutBinding, PaymentViewModel, PaymentRepository>() {
    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)
        PaystackSdk.initialize(this)
        PaystackSdk.setPublicKey("pk_test_2e5cd2f5435099fd111779f7bbcdd4d66d7194f3")

        var amount = intent!!.getIntExtra("amount", 0)
        amount *= 100

        binding.creditCardExpiry.addTextChangedListener {
            if (it.toString().length == 2 && !it.toString().contains("/")) {
                it!!.append("/")
            }
        }

        binding.btnMakePayment.setOnClickListener {
            val cardExpiry = binding.creditCardExpiry.text.toString().trim()
            val cardNumber = binding.creditCardNumber.text.toString().trim()
            val cvv = binding.creditCardCvv.text.toString().trim()
            val cardExpiryArray = cardExpiry.split("/").toTypedArray()
            val expiryMonth = cardExpiryArray[0].toInt()
            val expiryYear = cardExpiryArray[1].toInt()

            val card = Card(cardNumber, expiryMonth, expiryYear, cvv)
            val charge = Charge()
            charge.amount = amount
            charge.email = currentUserProfile.email
            charge.card = card
            //here you can check for network availability first, if the network is available, continue
            if (isNetworkAvailable()) {
                binding.btnMakePayment.visibility = View.GONE

                PaystackSdk.chargeCard(this, charge, object : Paystack.TransactionCallback {
                    override fun onSuccess(transaction: Transaction) {
                        Toast.makeText(
                            this@CheckoutActivity,
                            transaction.reference,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun beforeValidate(transaction: Transaction?) {
                    }

                    override fun onError(error: Throwable, transaction: Transaction?) {
                        Toast.makeText(
                            this@CheckoutActivity,
                            error.localizedMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            } else {
                Toast.makeText(this, "Please check your internet", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityCheckoutBinding.inflate(inflater)

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getActivityRepository(): PaymentRepository {
        return PaymentRepository()
    }
}