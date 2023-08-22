package com.xilidou.service

import org.slf4j.LoggerFactory
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel

abstract class SingleThreadEventLoop :SingleThreadEventExecutor(){

    fun register(socketChannel: SocketChannel,nioEventLoop: NioEventLoop){

        if(inEventLoop(Thread.currentThread())){
            register0(socketChannel,nioEventLoop)
        }else{
            nioEventLoop.execute {
                register0(socketChannel,nioEventLoop)
                logger.info("客户端的 channel 已注册到新线程的多路复用器了")
            }
        }

    }

    fun register0(channel: SocketChannel,nioEventLoop: NioEventLoop){
        channel.configureBlocking(false)
        channel.register(nioEventLoop.selector(),SelectionKey.OP_READ)

    }

    companion object{
        val logger = LoggerFactory.getLogger(SingleThreadEventLoop::class.java)!!
    }

}