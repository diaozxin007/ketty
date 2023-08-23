package com.xilidou.service

import org.slf4j.LoggerFactory
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel

abstract class SingleThreadEventLoop :SingleThreadEventExecutor(),EventLoop{

    override fun next(): EventLoop {
        return this
    }

    override fun register(channel: SocketChannel, nioEventLoop: NioEventLoop){
        nioEventLoop.execute{
            channel.configureBlocking(false)
            channel.register(nioEventLoop.selector(),SelectionKey.OP_READ)
        }
    }
    companion object{
        val logger = LoggerFactory.getLogger(SingleThreadEventLoop::class.java)!!
    }

}