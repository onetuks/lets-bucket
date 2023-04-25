package com.bucket.letsbucket.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.icu.util.Calendar
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import coil.load
import com.bucket.letsbucket.R
import com.bucket.letsbucket.data.BucketItem
import com.bucket.letsbucket.data.DetailData
import com.bucket.letsbucket.databinding.ActivityDetailBinding
import com.bucket.letsbucket.db.LifeBucketDB
import com.bucket.letsbucket.db.ThisYearBucketDB
import com.bucket.letsbucket.util.DataUtil
import com.bucket.letsbucket.util.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class DetailActivity : AppCompatActivity() {
    private val TAG = "DetailActivity"

    // Binding
    private lateinit var binding: ActivityDetailBinding

    // Gallery Task
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        LogUtil.d(TAG, "URI : " + uri.toString())
        if (uri != null) {
            this.uri = uri.toString()
        }
    }

    // Camera Task -> Store
    private var pictureUri: Uri? = null
    private val cameraStoreLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            this.uri = pictureUri.toString()
        }
    }

    // Camera Task -> Preview (Not Store)
//    private val cameraPreviewLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
//        if (bitmap != null) {
//            binding.bucketImage.setImageBitmap(bitmap)
//        }
//    }

    // Parcelable Data
    private lateinit var data: DetailData
    private lateinit var fromType: DataUtil.FROM_TYPE

    // Variable which changes View
    private var done: Boolean by Delegates.observable(false) { property, oldValue, newValue ->
        if (newValue) {
            binding.bucketCheck.setImageResource(R.drawable.checked)
        } else {
            binding.bucketCheck.setImageResource(R.drawable.unchecked)
        }
    }
    private var date: String by Delegates.observable("") { property, oldValue, newValue ->
        binding.calendarText.text = newValue
    }
    private var uri: String by Delegates.observable("") { property, oldValue, newValue ->
        if (newValue == "") {
            binding.noImageHintText.visibility = View.VISIBLE
            binding.layoutImage.setBackgroundResource(R.color.grey)
        } else {
            binding.noImageHintText.visibility = View.GONE
            binding.layoutImage.setBackgroundResource(R.color.pastel_orange)
            binding.bucketImage.load(newValue)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailBinding.inflate(layoutInflater)

        data = intent.getParcelableExtra("DATA")!!
        this.fromType = DataUtil.FROM_TYPE.values()[data.from]
        this.done = data.done
        this.date = data.date.toString()
        this.uri = data.uri.toString()

        checkInvalidAccess()
        setupBinding()

        setContentView(binding.root)
    }

    private fun checkInvalidAccess() {
        if (fromType == DataUtil.FROM_TYPE.LIFE) {
            if (data.lifetype == null) {
                LogUtil.d(TAG, "lifeType is null -> invalid access")
                onBackPressed()
            } else if (data.idx < 0 || data.idx >= DataUtil.LIFE_LIST[data.lifetype!!].size) {
                LogUtil.d(TAG,"index out of range -> invalid access")
                onBackPressed()
            }
        } else if (fromType == DataUtil.FROM_TYPE.THIS_YEAR) {
            if (data.idx < 0 || data.idx >= DataUtil.THIS_YEAR_LIST.size) {
                LogUtil.d(TAG,"index out of range -> invalid access")
                onBackPressed()
            }
        }
    }

    private fun setupBinding() {
        binding.let {
            it.bucketText.setText(data.text)
            it.calendarText.setText(data.date)
            if (data.done) {
                it.bucketCheck.setImageResource(R.drawable.checked)
            } else {
                it.bucketCheck.setImageResource(R.drawable.unchecked)
            }

            // 뒤로가기 버튼
            it.buttonBack.setOnClickListener { onBackPressed() }

            // 확인 버튼
            it.buttonConfirm.setOnClickListener {
                if (binding.bucketText.text.length > 0) {
                    modifyToList()
                    modifyToDB()
//                    item.printBucketItem()
                    DataUtil.DATA_CHANGED_LISTENER?.dataChanged()
                    onBackPressed()
                } else {
                    Toast.makeText(this, "버킷리스트를 작성해주세요!", Toast.LENGTH_SHORT).show()
                }
            }

            // 아이템 완료 버튼
            it.bucketCheck.setOnClickListener { this.done = !this.done }

            // 캘린더뷰
            it.calendarLayout.setOnClickListener {
                val today = GregorianCalendar()
                DatePickerDialog(
                    this,
                    R.style.DatePickerStyle,
                    { view, year, month, dayOfMonth ->
                        this.date = "${year}/${month + 1}/${dayOfMonth}"
                    },
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH)
                ).let {
                    it.setIcon(R.drawable.calendar)
                    it.show()
                }
            }

            // 이미지뷰
            it.bucketImage.setOnClickListener {
                LogUtil.d(TAG, "choose image from local gallery")
                val wayItems = arrayOf("갤러리에서 선택할래요", "카메라로 찍을래요")
                var selectedItem: Int? = null
                AlertDialog.Builder(this)
                    .setTitle("인증샷 선택하기")
//                    .setMessage("갤러리에서 선택하실래요, 사진을 찍으실래요?")
                    .setSingleChoiceItems(wayItems, -1) { dialog, which ->
                        selectedItem = which
                    }
                    .setPositiveButton("확인") { dialog, which ->
                        LogUtil.d(TAG, "${selectedItem} Task Selected")
                        if (selectedItem != null) {
                            when (selectedItem) {
                                0 -> galleryLauncher.launch("image/*")
                                1 -> {
                                    try {
                                        pictureUri = createImageFile()
                                        cameraStoreLauncher.launch(pictureUri)
                                    } catch (e: NullPointerException) {
                                        e.printStackTrace()
                                    }
                                }
//                                2 -> cameraPreviewLauncher.launch(null)
                            }
                        }
                    }
                    .setNegativeButton("취소") { dialog, which ->
                        LogUtil.d(TAG, "Cancel Image Select Task")
                    }
                    .setIcon(R.drawable.basic)
                    .show()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun createImageFile(): Uri? {
        val now = SimpleDateFormat("yyMMdd_HHmmss").format(Date())
        val content = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "img_bucket_$now.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, content)
    }

    private fun modifyToList() {
        when (fromType) {
            DataUtil.FROM_TYPE.THIS_YEAR -> {
                DataUtil.THIS_YEAR_LIST.set(
                    data.idx,
                    BucketItem(
                        id = data.id,
                        text = binding.bucketText.text.toString(),
                        done = this.done,
                        lifetype = data.lifetype,
                        date = this.date,
                        uri = this.uri
                    )
                )
            }
            DataUtil.FROM_TYPE.LIFE -> {
                DataUtil.LIFE_LIST[data.lifetype!!].set(
                    data.idx,
                    BucketItem(
                        id = data.id,
                        text = binding.bucketText.text.toString(),
                        done = this.done,
                        lifetype = data.lifetype,
                        date = this.date,
                        uri = this.uri
                    )
                )
            }
            else -> {}
        }
    }

    private fun modifyToDB() {
        // 아이템 텍스트 변경 시 DB 작업
        CoroutineScope(Dispatchers.Main).launch {
            CoroutineScope(Dispatchers.IO).async {
                when (fromType) {
                    DataUtil.FROM_TYPE.THIS_YEAR -> {
                        val modifiedItem = DataUtil.THIS_YEAR_LIST.get(data.idx)
                        ThisYearBucketDB.getInstance(this@DetailActivity)!!.thisYearBucketDao()
                            .updateItem(
                                modifiedItem.itemText,
                                modifiedItem.itemDone,
                                modifiedItem.itemDate,
                                modifiedItem.itemUri,
                                modifiedItem.itemId
                            )
                    }
                    DataUtil.FROM_TYPE.LIFE -> {
                        val modifiedItem = DataUtil.LIFE_LIST[data.lifetype!!].get(data.idx)
                        LifeBucketDB.getInstance(this@DetailActivity)!!.lifebucketDao().updateItem(
                            modifiedItem.itemText,
                            modifiedItem.itemDone,
                            modifiedItem.itemDate,
                            modifiedItem.itemUri,
                            modifiedItem.itemId
                        )
                    }
                    else -> {}
                }

            }.await()
        }
    }
}