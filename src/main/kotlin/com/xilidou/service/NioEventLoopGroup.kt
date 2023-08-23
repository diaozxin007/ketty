package com.xilidou.service

import java.nio.channels.SocketChannel

class NioEventLoopGroup(threads: Int): EventLoopGroup {

    private lateinit var nioEventLoop: Array<NioEventLoop>

    private var index = 0

    init {
        for( i in 0 .. threads){
            nioEventLoop[i] = NioEventLoop()
        }
    }

    override fun next(): EventLoop {
        val id = index % nioEventLoop.size
        index ++
        return nioEventLoop[id]
    }

    override fun register(channel: SocketChannel, nioEventLoop: EventLoop) {
        TODO("Not yet implemented")
    }

}