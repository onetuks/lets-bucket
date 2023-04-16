package com.example.letsbucket.util

import com.example.letsbucket.R
import com.example.letsbucket.data.LifeTypeItem
import com.example.letsbucket.data.BucketItem
import java.util.jar.Manifest

object DataUtil {
    var THIS_YEAR_LIST: ArrayList<BucketItem> = arrayListOf()

    var LIFE_LIST: Array<ArrayList<BucketItem>> = arrayOf(
        arrayListOf(), arrayListOf(), arrayListOf(), arrayListOf(), arrayListOf(), arrayListOf(), arrayListOf(),
    )

    var LIFE_TYPE_LIST: ArrayList<LifeTypeItem> = arrayListOf(
        LifeTypeItem(R.drawable.health, R.string.health),
        LifeTypeItem(R.drawable.trip, R.string.trip),
        LifeTypeItem(R.drawable.hobby, R.string.hobby),
        LifeTypeItem(R.drawable.develope, R.string.develope),
        LifeTypeItem(R.drawable.money, R.string.money),
        LifeTypeItem(R.drawable.relation, R.string.relation),
        LifeTypeItem(R.drawable.etc, R.string.etc),
    )

    var DATA_CHANGED_LISTENER: DataChangedListener? = null

    enum class FROM_TYPE {
        LIFE, THIS_YEAR
    }

    val permissionList = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
}