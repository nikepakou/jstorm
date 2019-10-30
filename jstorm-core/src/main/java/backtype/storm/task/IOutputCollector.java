/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package backtype.storm.task;

import backtype.storm.tuple.Tuple;
import java.util.Collection;
import java.util.List;

public interface IOutputCollector extends IErrorReporter {
    /**
     * Returns the task ids that received the tuples.
     *
     */
    /**
     *  用来向外发送数据,它的返回值是该消息所有发送目标的TaskId集合
     *  输入参数:
     *  streamId:消息将被输出到的流;
     *  anchors:输出消息的标记,通常代表该条消息是由哪些消息产生的,主要用于消息的Ack系统;
     *  tuple:要输出的消息,为一个Object列表
     */
    List<Integer> emit(String streamId, Collection<Tuple> anchors, List<Object> tuple);

    /**
     * 与上述方法emit类似,区别在于,emitDirect方法发送的消息只有指定taskId的Task才可以接收.
     * 该方法要求streamId对应的流必须为直接流而且接收端的Task必须通过直接分组的方式来接收消息,否则会抛出异常.
     * 这也就意味着:如果没有下游节点接收该消息,则此类消息其实并没有真正被发送
     */
    void emitDirect(int taskId, String streamId, Collection<Tuple> anchors, List<Object> tuple);

    void ack(Tuple input);

    void fail(Tuple input);
}
