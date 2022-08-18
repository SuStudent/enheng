# enheng
内网穿透 base on Netty


### 运行客户端程序

1. cd eheng-client
2. mvn clean install
3. cd target 得到 enheng-client-1.0.0.jar
4. 将application.properties 和  enheng-client-1.0.0.jar 同一级目录下
5. 修改application.properties

```
enheng.client.remote-host 和 enheng.client.remote-port 远程服务地址。（须通过公网访问）
enheng.client.app-key 为eheng-server部署者提供


>>  可将enheng.client.remote-host 改为“106.14.252.68” 进行测试
```

6. java -jar enheng-client-1.0.0.jar --subdomain=xxxxx --port=31001 --host=10.15.47.219

```
subdomain 为子域名。host为要代理的IP，(为空时去本地) port为要代理的端口
启动成功后访问。http://xxxxx.susudad.cn
```



目前仅支持Http穿透，穿透有风险，使用请谨慎。有问题可联系。[联系我](mailto:sustudent@qq.com)

