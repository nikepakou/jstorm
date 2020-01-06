---
title: FAQ
layout: plain_cn
---
## 性能问题
参考性能优化

## 运行四五天之后，ack模式的kafka队列出现数据挤压，acker中打印“Acker's timeout item size”
* 原因：spout发送较快，bolt消费跟不上，导致spout发送给bolt后，在超时时间内没有进行ack，造成消息重发，进一步导致挤压的问题
* 分析：既然bolt消费速度跟不上spout速度，就减小spout发送速度，可以开启反压；另外TOPOLOGY_EXECUTOR_RECEIVE_BUFFER_SIZE不易过大，过大会导致大量消息挤压在exeQueue中，消费不及时导致超时。
* 措施1：尝试减小TOPOLOGY_EXECUTOR_RECEIVE_BUFFER_SIZE和TOPOLOGY_EXECUTOR_SEND_BUFFER_SIZE，之前设置为16384，减小到1024，并开启反压，高位线0.99，低位线0.85;
* 2010-1-5观察第二天结果：
1. 部分spout报QueryRealTimeSpout:9737-SerializeQueue is full , at 2020-01-05 09:08
   No response from Task-9738, last report time(sec) is 1578125749, at 2020-01-04 16:20
2. 部分spout报UserCartJmqSpout:9578-ExecutorQueue is full , at 2020-01-04 01:56
3. ClickStreamKafkaSpout:9779-SerializeQueue is full , at 2020-01-04 22:29
4. UserCartJdqBolt:8-ExecutorQueue is full , at 2020-01-04 22:59
5. No response from Task-1131, last report time(sec) is 1578039593, at 2020-01-05 09:16
  QueryRealtimeBolt:1131-ExecutorQueue is full , at 2020-01-05 09:17
  
6. EffectiveClickAppSceneBolt:6227-ExecutorQueue is full , at 2020-01-04 22:34
7. ClickStreamKafkaBolt:6441-ExecutorQueue is full , at 2020-01-04 22:26
8. __topology_master:9296-CtrlQueue is full , at 2020-01-03 14:52
   Task-9296 is dead on 11.26.21.2:6816, 20200103145244, at 2020-01-03 14:52
   __topology_master:9296-SerializeQueue is full , at 2020-01-05 03:31
   
9. 数据tp99延时减小了10min，平均延时待观察（貌似时变小了），单条数据处理平均耗时在流量高峰由26ms涨到，非高峰时变化不大，整体吞吐量变化不大。

* 总结：总体效果是正向的，减小队列,增加反压，减小tp99，<font color=#FF0000>而高峰时单独数据处理平均耗时增加原因还不清楚</font>；对于exeQueue和serializeQueue满,可通过同时减小高位线来解决

* 2010-1-6观察第三天结果：
1. query_poll_delay增大，明显数据源有数据挤压，可以适当增大query_bolt_num
* 措施2：高位线0.90，低位线0.80，可以适当增大UserCartJmqBolt的并发
       


## task is dead && ctrlQueue is full
* 原因：ctrlQueue队列长度是由TOPOLOGY_CTRL_BUFFER_SIZE控制，ctrlQueue队列中存放task心跳消息，task心跳是由TaskHeartbeatUpdater发送给nimbus,
* 分析：task心跳超时时间和心跳频次跟这两个参数有关，task.heartbeat.frequency.secs
       nimbus.task.timeout.secs，如果增大task.heartbeat.frequency.secs参数，会减少ctrlQueue的压力，增大nimbus.task.timeout.secs则减少task被nimbus误kill的概率
* 措施1：task.heartbeat.frequency.secs=20
* 结果：ctrlQueue is full 报错概率降低
* 措施2：nimbus.task.timeout.secs=240->360