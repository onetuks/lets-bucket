package com.example.letsbucket.activity

import android.app.DatePickerDialog
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import coil.load
import com.example.letsbucket.R
import com.example.letsbucket.data.BucketItem
import com.example.letsbucket.data.DetailData
import com.example.letsbucket.databinding.ActivityDetailBinding
import com.example.letsbucket.db.LifeBucketDB
import com.example.letsbucket.db.ThisYearBucketDB
import com.example.letsbucket.util.DataUtil
import com.example.letsbucket.util.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import kotlin.properties.Delegates

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        binding.bucketImage.load(uri)
    }

    private lateinit var data: DetailData
    private lateinit var fromType: DataUtil.FROM_TYPE

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailBinding.inflate(layoutInflater)

        data = intent.getParcelableExtra("DATA")!!
        this.fromType = DataUtil.FROM_TYPE.values()[data.from]
        this.done = data.done
        this.date = data.date.toString()

        checkInvalidAccess()
        setupBinding()

        setContentView(binding.root)
    }

    private fun checkInvalidAccess() {
        if (fromType == DataUtil.FROM_TYPE.LIFE) {
            if (data.lifetype == null) {
                LogUtil.d(" TYPE is null")
                onBackPressed()
            } else if (data.idx < 0 || data.idx >= DataUtil.LIFE_LIST[data.lifetype!!].size) {
                LogUtil.d("index out of range")
                onBackPressed()
            }
        } else if (fromType == DataUtil.FROM_TYPE.THIS_YEAR) {
            if (data.idx < 0 || data.idx >= DataUtil.THIS_YEAR_LIST.size) {
                LogUtil.d("index out of range")
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
                    { view, year, month, dayOfMonth ->
                        this.date = "${year}/${month + 1}/${dayOfMonth}"
                    },
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            // 이미지뷰
            it.bucketImage.setOnClickListener {
                LogUtil.d("이미지뷰 클릭")
                launcher.launch("image/*")
            }
        }
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
                        date = this.date
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
                        date = this.date
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
                                modifiedItem.itemId
                            )
                    }
                    DataUtil.FROM_TYPE.LIFE -> {
                        val modifiedItem = DataUtil.LIFE_LIST[data.lifetype!!].get(data.idx)
                        LifeBucketDB.getInstance(this@DetailActivity)!!.lifebucketDao().updateItem(
                            modifiedItem.itemText,
                            modifiedItem.itemDone,
                            modifiedItem.itemDate,
                            modifiedItem.itemId
                        )
                    }
                    else -> {}
                }

            }.await()
        }
    }
}