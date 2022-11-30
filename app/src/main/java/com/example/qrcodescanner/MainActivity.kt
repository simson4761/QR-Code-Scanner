package com.example.qrcodescanner

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.qrcodescanner.databinding.ActivityMainBinding
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions


class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")

    private lateinit var binding : ActivityMainBinding
    private var barcodeScannerOptions : BarcodeScannerOptions? = null
    private var barcodeScanner : BarcodeScanner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (!cameraPermission()){
            requestCameraPermission()
        }
        if(!storagePermission()){
            requestStoragePermission()
        }


        barcodeScannerOptions = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()

        barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions!!)


        binding.scanButton.setOnClickListener {
            //val scanner = IntentIntegrator(this)
            //scanner.initiateScan()
            barcodeLauncher.launch(ScanOptions())

        }


        binding.openGallery.setOnClickListener {
            if (cameraPermission()){
                val galleryIntent = Intent(Intent.ACTION_PICK)
                galleryIntent.type = "image/*"
                galleryActivityResultLauncher.launch(galleryIntent)
            }
        }
    }

    private fun requestStoragePermission() {

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 101)
    }

    private fun requestCameraPermission() {

        ActivityCompat.requestPermissions(this , arrayOf(android.Manifest.permission.CAMERA) , 100)
    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){result->
        if (result.resultCode == Activity.RESULT_OK){
            val data = result.data
            val imageURI = data?.data

            if (imageURI == null){
                Toast.makeText(this, "pick image first" , Toast.LENGTH_SHORT)
            }
            else{

                identityResultFromImage(imageURI)
            }


        }
        else{
            Toast.makeText(this, "cancelled" , Toast.LENGTH_SHORT).show()
        }

    }

    private fun identityResultFromImage(imageURI: Uri) {
            try {
                val inputImage = InputImage.fromFilePath(this , imageURI)
                val barcodeResult = barcodeScanner!!.process(inputImage)
                    .addOnSuccessListener {barcodes->
                        extractBarcode(barcodes)

                    }
                    .addOnCanceledListener {
                        Toast.makeText(this, "Failed scanning ", Toast.LENGTH_LONG).show()
                    }
            }
            catch (_: Exception){
                Toast.makeText(this, "failed to scan", Toast.LENGTH_LONG).show()
            }
    }

    private fun extractBarcode(barcodes: List<Barcode>) {
            for (barcode in barcodes){
                val rawInfo = barcode.rawValue
                Toast.makeText(this , "Results = $rawInfo" , Toast.LENGTH_LONG).show()
                binding.resultsTextView.text = rawInfo

                when(barcode.valueType){
                    Barcode.TYPE_URL -> {
                        AlertDialog.Builder(this).setTitle("Do you want to follow this URL?")
                            .setPositiveButton("Yes"){_,_->
                                val openURL = Intent(Intent.ACTION_VIEW)
                                openURL.data = Uri.parse("${barcode.rawValue}")
                                startActivity(openURL)
                            }
                            .show()
                    }
                }
            }
    }


    private val barcodeLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(
                this,
                "Scanned: " + result.contents,
                Toast.LENGTH_LONG
            ).show()
            binding.resultsTextView.text = result.contents
        }
    }

    private fun cameraPermission() : Boolean {
        return ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.CAMERA)  == PackageManager.PERMISSION_GRANTED
    }

    private fun storagePermission() : Boolean {
        return ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_GRANTED
    }




    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            100 ->{
                if (grantResults.isNotEmpty()){
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if(cameraAccepted && storageAccepted){
                        barcodeLauncher.launch(ScanOptions())
                    }
                    else{
                        Toast.makeText(this , "permission required" , Toast.LENGTH_SHORT).show()
                    }
                }
            }

            101->{
                if (grantResults.isNotEmpty()){

                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if(storageAccepted){
                        barcodeLauncher.launch(ScanOptions())
                    }
                    else{
                        Toast.makeText(this , " storage permission required" , Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


}