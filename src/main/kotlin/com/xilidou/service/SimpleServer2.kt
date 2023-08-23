package com.xilidou.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel


fun main(){
    SimpleServer2().run()
}

class SimpleServer2 {

    private lateinit var workerGroup:EventLoopGroup

    fun setEventLoopGroup(workGroup: EventLoopGroup){
        this.workerGroup = workGroup
    }

    fun run() {
        val serverSocketChannel = ServerSocketChannel.open()
        serverSocketChannel.configureBlocking(false)
        val selector = Selector.open()
        val selectionKey = serverSocketChannel.register(selector, 0, serverSocketChannel)
        selectionKey.interestOps(SelectionKey.OP_ACCEPT)
        serverSocketChannel.bind(InetSocketAddress(8080))

        setEventLoopGroup(NioEventLoopGroup(2))

        while (true) {
            logger.info("main函数阻塞在这里吧。。。。。。。")
            selector.select()
            val selectedKeys = selector.selectedKeys()
            val keyIterator = selectedKeys.iterator()
            while (keyIterator.hasNext()) {
                val key = keyIterator.next()
                keyIterator.remove()
                if (key.isAcceptable) {
                    val channel = key.channel() as ServerSocketChannel
                    val socketChannel = channel.accept()
                    workerGroup.register(socketChannel,workerGroup.next())
                }
            }
        }
    }



    companion object {
        private val logger: Logger = LoggerFactory.getLogger(Work::class.java)
    }

}