//package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.activities
//
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.util.Log
//import android.view.View
//import android.view.Window
//import android.view.WindowManager
//import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.billing.BillingViewModel
//import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.ActivitySubcriptionBinding
//import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.*
//import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions.Companion.isFromSplash
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import org.koin.androidx.viewmodel.ext.android.viewModel
//
//class SubscriptionActivity : BaseActivity() {
//    lateinit var binding: ActivitySubcriptionBinding
//
//    private val billingViewModel: BillingViewModel by viewModel()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivitySubcriptionBinding.inflate(layoutInflater)
//
//        hideStatusBar()
//
//        setContentView(binding.root)
//
//        binding.closeBtn.visibility = View.GONE
//        binding.radioYear.isChecked = true
//        binding.radioMonth.isChecked = false
//        binding.trailTv.visibility = View.VISIBLE
//        isMonth = false
//
//        setupObserver()
//
//        Handler(Looper.getMainLooper()).postDelayed({
//            binding.closeBtn.visibility = View.VISIBLE
//        }, 2000)
//        binding.closeBtn.setOnClickListener {
//            goBack()
//        }
//        binding.upgradeBtn.setOnClickListener {
//            purchase()
//        }
//        binding.tosTxt.setOnClickListener {
//            openUrl("https://itechsolutionapps.wordpress.com/")
//        }
//        binding.ppTxt.setOnClickListener {
//            openUrl("https://itechsolutionapps.wordpress.com/")
//        }
//        binding.rpTxt.setOnClickListener {
//            purchase()
//        }
//        binding.card1Year.setOnClickListener {
//            binding.radioYear.isChecked = true
//            binding.radioMonth.isChecked = false
//            binding.trailTv.visibility = View.VISIBLE
//            isMonth = false
//            setupObserver()
//        }
//        binding.card2Monthly.setOnClickListener {
//            binding.radioYear.isChecked = false
//            binding.radioMonth.isChecked = true
//            binding.trailTv.visibility = View.GONE
//            isMonth = true
//            setupObserver()
//        }
//    }
//
//    private fun purchase() {
//        val packageKey = if (binding.radioMonth.isChecked) {
//            Constants.SUBS_SKUS[0]
//        } else {
//            Constants.SUBS_SKUS[1]
//        }
//        billingViewModel.getBySkuId(packageKey)?.let {
//            billingViewModel.makePurchase(this, it)
//        }
//    }
//
//    var isMonth = false
//
//    private fun setupObserver() {
//        billingViewModel.subSkuDetailsListLiveData.observe(this,
//            { skuList ->
//                if (!skuList.isNullOrEmpty()) {
//                    Log.d("Subscription_Event_0", "${skuList[0].title}")
//                    Log.d("Subscription_Event_2", "${skuList[0].freeTrialPeriod}")
//                    Log.d("Subscription_Event_2", "${skuList[1].freeTrialPeriod}")
//                    binding.monthlyPrice = skuList[1].price
//                    binding.annualPrice = skuList[0].price
//
//                    if(!isMonth)
//                    binding.descTxt.text = "After the ${skuList[0].freeTrialPeriod}-day free trial," +
//                                " the subscription automatically renews at ${skuList[0].price} per year" +
//                                " and is charged to your Google Play account until cancelled." +
//                                " You may cancel anytime by going to Subscription in Play Store."
//                    else
//                        binding.descTxt.text = "This subscription will charge ${skuList[1].price} per month" +
//                                " and automatically renews to your Google Play account until cancelled." +
//                                " You may cancel anytime by going to Subscription in Play Store."
//
//                    if (!skuList[0].canPurchase || !skuList[1].canPurchase) {
//                        GlobalScope.launch(Dispatchers.Main) {
//                            utilsViewModel.setPremiumUser(true)
//                            utilsViewModel.setAutoAdsRemoved(true)
//                            Companions.isPurchased = true
//                        }.invokeOnCompletion {
//                            if (Companions.isPurchased) {
//                                showToast("Successfully Purchased")
//                                finishAffinity()
//                                openActivity(MainActivity::class.java)
//                            }
//                        }
//                    } else {
//                        utilsViewModel.setPremiumUser(false)
//                        utilsViewModel.setAutoAdsRemoved(false)
//                        Companions.isPurchased = false
//                    }
//                }
//            })
//        /// free trail
//    }
//
//    private val TAG = "SubscriptionActivity"
//
//    private fun goBack() {
//        if (binding.closeBtn.visibility == View.VISIBLE) {
//            finish()
//            if (isFromSplash) {
//                openActivity(MainActivity::class.java)
//                isFromSplash = false
//            }
//        } else
//            showToast("Wait")
//    }
//
//    override fun onBackPressed() {
//        goBack()
//    }
//}