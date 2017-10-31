# SendUDPBroadcast
这是一个局域网发送UDP广播实现握手并发送消息的demo，首先客户端发送UDP广播，广播地址是发送之前与服务端协商好的，并且在同一个局域网下，当服务端接收到广播之后，就向客户端发送自己当前网络的IP地址，这样，客户端也可以收到服务端的IP地址，所以客户端和服务端都知道对方的IP地址，双方就建立了连接，之后就可以通过Socket通信进行消息发送。
![image1](https://github.com/oscarhscc/SendUDPBroadcast/blob/master/app/src/main/res/drawable-xhdpi/Screenshot_1.png)
如图就是主界面，目前只可点击Start send broast发送UDP广播，广播发送之后，如果服务端有相应，就会收到服务端的IP地址，这个时候客户端已经和服务端建立了连接，完成握手。
![image2](https://github.com/oscarhscc/SendUDPBroadcast/blob/master/app/src/main/res/drawable-xhdpi/Screenshot_3.png)
这个时候就可以向服务端发送消息了。
![image2](https://github.com/oscarhscc/SendUDPBroadcast/blob/master/app/src/main/res/drawable-xhdpi/Screenshot_2.png)
