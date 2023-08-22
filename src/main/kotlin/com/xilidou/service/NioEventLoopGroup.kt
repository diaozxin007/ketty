package com.xilidou.service

class NioEventLoopGroup(threads: Int): EventLoopGroup() {

    private lateinit var nioEventLoop: Array<NioEventLoop>

    init {
        for( i in 0 .. threads){
            nioEventLoop[i] = NioEventLoop()
        }
    }


}