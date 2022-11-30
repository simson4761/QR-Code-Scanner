package com.example.qrcodescanner

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.qrcodescanner.databinding.ActivityQrcodeGeneratorBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

class QRCodeGeneratorActivity : AppCompatActivity() {
  private lateinit var binding : ActivityQrcodeGeneratorBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrcodeGeneratorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageView.setImageResource(R.drawable.ic_baseline_qr_code_24)
        binding.generateButton.setOnClickListener {
            val sample = binding.inputTextView.text.toString()
            if (sample.isEmpty()){
                Toast.makeText(this , "EMPTY!!", Toast.LENGTH_SHORT).show()
            }
            else{
                val writer = QRCodeWriter()
                try {
                    val bitMatrix = writer.encode(sample , BarcodeFormat.QR_CODE , 512, 512)
                    val height = bitMatrix.height
                    val width = bitMatrix.width
                    val bitmap = Bitmap.createBitmap(width, height , Bitmap.Config.RGB_565)
                    for (x in 0 until width){
                        for (y in 0 until height){
                            bitmap.setPixel(x,y, if(bitMatrix[x,y])  Color.BLACK else Color.WHITE )
                        }
                    }
                    binding.imageView.setImageBitmap(bitmap)

                }
                catch (e : WriterException){
                    e.printStackTrace()
                }
            }
        }

        binding.floatingActionButton.setOnClickListener{
            val scanActivity = Intent(this , MainActivity::class.java)
            startActivity(scanActivity)
        }
    }
}