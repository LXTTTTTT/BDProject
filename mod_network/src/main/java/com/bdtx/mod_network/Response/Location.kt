package com.bdtx.mod_network.Response

data class Location(
    var alt:Double,
    var dir:Int,
    var lat:Double,
    var lng:Double,
    var locStatus:String,
    var locType:String,
    var remark:String,
    var speed:Int,
    var time:String,
    var wgs84Lng:Double,
    var wgs84Lat:Double
)