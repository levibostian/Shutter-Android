package com.levibostian.shutter_android.type

enum class ShutterActionType {

    TAKE_PHOTO {
        override fun <E> accept(visitor: Visitor<E>): E = visitor.visitTakePhoto()
    };

    abstract fun <E> accept(visitor: Visitor<E>): E

    interface Visitor<out E> {
        fun visitTakePhoto(): E
    }

}