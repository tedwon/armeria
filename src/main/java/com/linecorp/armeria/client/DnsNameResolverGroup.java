/*
 * Copyright 2015 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.armeria.client;

import java.net.InetSocketAddress;

import io.netty.channel.ChannelFactory;
import io.netty.channel.EventLoop;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.channel.socket.DatagramChannel;
import io.netty.resolver.NameResolver;
import io.netty.resolver.NameResolverGroup;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.resolver.dns.DnsServerAddresses;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.internal.StringUtil;

/**
 * A {@link NameResolverGroup} of {@link DnsNameResolver}s.
 */
class DnsNameResolverGroup extends NameResolverGroup<InetSocketAddress> {

    private static final InetSocketAddress ANY_LOCAL_ADDR = new InetSocketAddress(0);

    private final ChannelFactory<? extends DatagramChannel> channelFactory;
    private final InetSocketAddress localAddress;
    private final DnsServerAddresses nameServerAddresses;

    DnsNameResolverGroup(
            Class<? extends DatagramChannel> channelType, DnsServerAddresses nameServerAddresses) {
        this(channelType, ANY_LOCAL_ADDR, nameServerAddresses);
    }

    DnsNameResolverGroup(
            Class<? extends DatagramChannel> channelType,
            InetSocketAddress localAddress, DnsServerAddresses nameServerAddresses) {
        this(new ReflectiveChannelFactory<DatagramChannel>(channelType), localAddress, nameServerAddresses);
    }

    DnsNameResolverGroup(
            ChannelFactory<? extends DatagramChannel> channelFactory, DnsServerAddresses nameServerAddresses) {
        this(channelFactory, ANY_LOCAL_ADDR, nameServerAddresses);
    }

    DnsNameResolverGroup(
            ChannelFactory<? extends DatagramChannel> channelFactory,
            InetSocketAddress localAddress, DnsServerAddresses nameServerAddresses) {
        this.channelFactory = channelFactory;
        this.localAddress = localAddress;
        this.nameServerAddresses = nameServerAddresses;
    }

    @Override
    protected final NameResolver<InetSocketAddress> newResolver(EventExecutor executor) throws Exception {
        if (!(executor instanceof EventLoop)) {
            throw new IllegalStateException(
                    "unsupported executor type: " + StringUtil.simpleClassName(executor) +
                    " (expected: " + StringUtil.simpleClassName(EventLoop.class));
        }

        return newResolver((EventLoop) executor, channelFactory, localAddress, nameServerAddresses);
    }

    /**
     * Creates a new {@link DnsNameResolver}. Override this method to create an alternative {@link DnsNameResolver}
     * implementation or override the default configuration.
     */
    protected DnsNameResolver newResolver(
            EventLoop eventLoop, ChannelFactory<? extends DatagramChannel> channelFactory,
            InetSocketAddress localAddress, DnsServerAddresses nameServerAddresses) throws Exception {

        return new DnsNameResolver(
                eventLoop, channelFactory, localAddress, nameServerAddresses);
    }
}
