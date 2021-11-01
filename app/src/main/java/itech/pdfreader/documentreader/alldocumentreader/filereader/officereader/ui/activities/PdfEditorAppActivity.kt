package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.activities

import android.os.Bundle
import android.view.View
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.setNativeAd
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.utilities.ExtentionsFunctions.isInternetConnected
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.PdfEditorAppBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.ModuleType
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.*

class PdfEditorAppActivity : BaseActivity() {
    lateinit var binding : PdfEditorAppBinding
    private val appPkgName = "itech.pdfreader.editor.creator"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = PdfEditorAppBinding.inflate(layoutInflater)

        hideStatusBar()

        val type = intent.getStringExtra(Constants.TYPE)

        binding.bodyTxt.text = when(type){
            ModuleType.DocToPdf.name-> "To convert this document to pdf you need to install this application from google play store"
            ModuleType.ExcelToPdf.name->"To convert this excel file to pdf you need to install this application from google play store"
            ModuleType.ExtractText.name->"To extract text from pdf you need to install this application from google play store"
            ModuleType.Merge.name->"To merge pdf files you need to install this application from google play store"
            ModuleType.Compress.name->"To compress pdf file you need to install this application from google play store"
            ModuleType.AddRemovePassword.name->"To add/remove password from pdf you need to install this application from google play store"
            ModuleType.Edit.name->"To edit pdf file you need to install this application from google play store"
            ModuleType.Reorder.name->"To reorder pdf pages you need to install this application from google play store"
            else -> "Work on documents anywhere using the latest tools to view and share PDFs."
        }

        binding.notNowBtn.setOnClickListener {
            onBackPressed()
        }
        binding.googlePlayBtn.setOnClickListener {
            launchAnotherApplication(appPkgName)
        }
        binding.openBtn.setOnClickListener {
            launchAnotherApplication(appPkgName)
        }

        loadImage(binding.adAppIcon,"https://play-lh.googleusercontent.com/2Ax7coEMZiUOqtyyzIgOXbZm9V9Js8dcfsaNZTs2dXRIm7alecD9m7iyf_ZzSgnjS8WE=s180-rw")
        loadImage(binding.imageView9,"https://play-lh.googleusercontent.com/7d7_G7VytavlHJVjfuoOwuL6wm2Sbc7aY7opbo-MzfqHulFe0Z72wdzpRJ5EfYKhSQ=w720-h310-rw")

        setContentView(binding.root)

        refreshAdOnView()

    }

    private val TAG = "AllDocumentReaderAppAct"

    private fun refreshAdOnView() {
        /**show native ad*/
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.shimmerViewContainer.startShimmer()
        if (!utilsViewModel.isPremiumUser() && isInternetConnected()) {
            binding.adLayout.visibility = View.VISIBLE
            setNativeAd(
                utilsViewModel.isPremiumUser(),
                binding.adLayout,
                R.layout.view_screen_native_layout,
                TAG,
                adMobNativeId = getString(R.string.pageEditorScreenNativeId), onFailed = {
                    binding.shimmerViewContainer.visibility = View.GONE
                    binding.adLayout.visibility = View.GONE
                    binding.shimmerViewContainer.stopShimmer()
                }
            ) {
                binding.shimmerViewContainer.visibility = View.GONE
                binding.shimmerViewContainer.stopShimmer()
            }
        }else{
            binding.shimmerViewContainer.visibility = View.GONE
            binding.adLayout.visibility = View.GONE
        }
        /************************/
    }

    override fun onResume() {
        super.onResume()
        if(isAppInstalled(appPkgName)){
            binding.googlePlayBtn.visibility = View.INVISIBLE
            binding.openBtn.visibility = View.VISIBLE
        }
    }

}