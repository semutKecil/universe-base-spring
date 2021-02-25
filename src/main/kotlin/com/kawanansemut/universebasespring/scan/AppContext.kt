package com.kawanansemut.universebasespring.scan

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class AppContext : ApplicationContextAware {
    override fun setApplicationContext(context: ApplicationContext) {
        CONTEXT = context
    }

    companion object {
        private var CONTEXT: ApplicationContext? = null

        fun <T> getBean(clazz: Class<T>): T {
            return CONTEXT!!.getBean(clazz)
        }
    }
}
