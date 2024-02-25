package com.bdtx.mod_data.NormalBean

import com.amap.api.maps.model.LatLng

data class MapContactLocation (

    var location:LatLng,
    var number:String,
    var remark:String,
    var time:Long,  // 时间戳（秒）

)
