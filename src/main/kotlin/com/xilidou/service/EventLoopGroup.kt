package com.xilidou.service

import java.nio.channels.SocketChannel

interface EventLoopGroup {

    fun next():EventLoop
    fun register(channel:SocketChannel, nioEventLoop: NioEventLoop)

}