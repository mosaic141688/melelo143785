package com.melelo.melelo

import org.json.JSONObject

class Contact(number:String,sms_enabled:Boolean,call_enabled:Boolean) {
    lateinit var number:String
    var sms_enabled:Boolean
    var call_enabled: Boolean

    init {
        this.sms_enabled = sms_enabled
        this.number = number
        this.call_enabled = call_enabled
    }


    override fun toString():String{
        val obj = JSONObject()
        obj.put("sms_enabled",sms_enabled)
        obj.put("number",number)
        obj.put("call_enabled",call_enabled)
        return obj.toString()
    }
}