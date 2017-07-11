package com.wongxd.absolutedomain.base.rx;

public enum ThreadMode {

    /**
     * android main thread
     */
    MAIN,

    /**
     * new thread
     */
    NEW_THREAD,


    COMPUTATION,


    IO,


    TRAMPOLINE,


    CURRENT


}