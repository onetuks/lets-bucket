package com.example.letsbucket.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsbucket.fragment.AddPopupDialog
import com.example.letsbucket.R
import com.example.letsbucket.adaptor.BucketAdapter
import com.example.letsbucket.databinding.ActivityLifeBinding
import com.example.letsbucket.util.DataUtil
import com.example.letsbucket.util.LogUtil

class LifeActivity : AppCompatActivity() {

    private var TAG: String = "LifeActivity"

    private lateinit var binding: ActivityLifeBinding
    private lateinit var lifeAdapter: BucketAdapter

    private var lifeType: Int? = null
    var subjectImgRes: MutableLiveData<Int> = MutableLiveData()
    var subjectString: MutableLiveData<String> = MutableLiveData()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifeType = intent.getIntExtra("LIFE_TYPE", -1)

        LogUtil.d(lifeType.toString())

        binding = DataBindingUtil.setContentView<ActivityLifeBinding?>(this, R.layout.activity_life)
            .apply {
                lifecycleOwner = this@LifeActivity
                activity = this@LifeActivity

                subjectImgRes.value = DataUtil.LIFE_TYPE_LIST.get(lifeType!!).lifeImage
                subjectString.value = getString(DataUtil.LIFE_TYPE_LIST.get(lifeType!!).lifeString)


                lifeAdapter = BucketAdapter(
                    this@LifeActivity,
                    DataUtil.FROM_TYPE.LIFE,
                    DataUtil.LIFE_LIST[lifeType!!]
                )
                lifeBucketList.adapter = lifeAdapter
                lifeBucketList.layoutManager =
                    LinearLayoutManager(this@LifeActivity, LinearLayoutManager.VERTICAL, false)

                fab.setOnClickListener(View.OnClickListener {
                    AddPopupDialog(
                        this@LifeActivity,
                        DataUtil.FROM_TYPE.LIFE,
                        lifeType
                    ).let {
                        it.setOnDismissListener {
                            lifeAdapter.notifyDataSetChanged()
                        }
                        it.show()
                    }
                })
            }
    }

    override fun onResume() {
        super.onResume()
        for (item in DataUtil.LIFE_LIST[lifeType!!]) {
            LogUtil.d(item.itemId.toString() + " " + item.itemText + " " + item.itemDone)
        }
    }
}