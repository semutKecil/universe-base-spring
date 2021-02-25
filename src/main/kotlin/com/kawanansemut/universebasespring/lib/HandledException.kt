package com.kawanansemut.universebasespring.lib

import org.apache.commons.lang3.StringUtils
import org.springframework.http.HttpStatus
import java.time.LocalDateTime


class HandledException(message: String, private val errorData: MutableMap<String, Any> = mutableMapOf(), val HttpErrorStatus: HttpStatus = HttpStatus.UNPROCESSABLE_ENTITY) : Exception(message) {
    fun errorHolder(): MutableMap<String, Any> = mutableMapOf(
        Pair("message", message ?: ""),
        Pair("data", errorData),
        Pair("time", LocalDateTime.now()),
        Pair("path", Helper.currentUrl())
    )
}

abstract class HandledExceptionValidation(val subject: String, val value: Any?) {
    val errorMessage = "invalid value"
    abstract fun errorData(): MutableMap<String, Any>
    abstract fun message(): String
    abstract fun isValid(): Boolean
    fun errorDataBuilder(): MutableMap<String, Any> {
        val dt = errorData()
        dt["subject"] = subject
        dt["value"] = value.toString()
        return dt
    }

    private var _customMessage: String? = null
    fun setCustomMessage(customMessage: String?): HandledExceptionValidation {
        _customMessage = customMessage
        return this
    }

    fun validate() {
        if (!isValid()) {
            throw HandledException(if (_customMessage != null) _customMessage!! else message(), errorDataBuilder())
        }
    }
}

class CustomValidation(subject: String,
                       value: String,
                       val validation: () -> Boolean,
                       val data: () -> MutableMap<String, Any>,
                       val eMessage: (cv: MutableMap<String, Any>) -> String
) : HandledExceptionValidation(subject, value) {
    override fun errorData() = if (isValid()) mutableMapOf() else data()
    override fun message() = eMessage(errorDataBuilder())
    override fun isValid() = validation()
}

class RequiredValidation(subject: String, val valueStr: String?) : HandledExceptionValidation(subject, valueStr) {
    override fun errorData() = mutableMapOf<String, Any>()
    override fun message() = "$subject is required."
    override fun isValid() = !(StringUtils.isBlank(valueStr) || StringUtils.isEmpty(valueStr))
}

class FormatValidation(subject: String, val valueStr: String, val reg: Regex) : HandledExceptionValidation(subject, valueStr) {
    override fun errorData() = mutableMapOf<String, Any>(
        Pair("regex", reg.toString())
    )

    override fun message() = "Invalid format."

    override fun isValid() = reg.matches(valueStr)
}

class RangeNumberValidation(subject: String, val valueNum: Int, val min: Int, val max: Int) : HandledExceptionValidation(subject, valueNum) {
    override fun errorData() = mutableMapOf<String, Any>(
        Pair("min", min),
        Pair("max", max)
    )

    override fun message() = "$subject value must be between $min and $max."
    override fun isValid() = !(valueNum < min || valueNum > max)
}

class RangeLengthValidation(subject: String, val valueStr: String, val min: Int, val max: Int) : HandledExceptionValidation(subject, valueStr) {
    override fun errorData() = mutableMapOf<String, Any>(
        Pair("min", min),
        Pair("max", max)
    )

    override fun message() = "$subject length must be between $min and $max."
    override fun isValid() = !(valueStr.length < min || valueStr.length > max)
}

class MaxLengthValidation(subject: String, val valueStr: String, val max: Int) : HandledExceptionValidation(subject, valueStr) {
    override fun errorData() = mutableMapOf<String, Any>(
        Pair("max", max)
    )

    override fun message() = "$subject maximum length is $max."
    override fun isValid() = valueStr.length <= max
}

class MinLengthValidation(subject: String, val valueStr: String, val min: Int) : HandledExceptionValidation(subject, valueStr) {
    override fun errorData() = mutableMapOf<String, Any>(
        Pair("min", min)
    )

    override fun message() = "$subject minimum length is $min."
    override fun isValid() = valueStr.length >= min
}