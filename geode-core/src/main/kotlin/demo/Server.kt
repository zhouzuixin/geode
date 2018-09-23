/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package demo

import org.apache.geode.cache.CacheFactory
import org.apache.geode.distributed.ConfigurationProperties
import java.util.*

fun main(args: Array<String>) {

    val properties = Properties().apply {
        set(ConfigurationProperties.LOCATORS, args[0])
        set(ConfigurationProperties.NAME, args[1])
        set(ConfigurationProperties.CACHE_XML_FILE, args[4])
        set(ConfigurationProperties.BIND_ADDRESS, args[3])
        set(ConfigurationProperties.MCAST_PORT, "0")
        set(ConfigurationProperties.STATISTIC_SAMPLING_ENABLED, "true")
        set(ConfigurationProperties.JMX_MANAGER, "false")
        set(ConfigurationProperties.JMX_MANAGER_START, "false")
        set(ConfigurationProperties.ENABLE_CLUSTER_CONFIGURATION, "false")
        set(ConfigurationProperties.USE_CLUSTER_CONFIGURATION, "false")
        set(ConfigurationProperties.CONSERVE_SOCKETS, "false")
        set(ConfigurationProperties.ENABLE_NETWORK_PARTITION_DETECTION, "false")
    }
    val cache = CacheFactory(properties).create()
    cache.addCacheServer().apply {
        port = 0
        bindAddress = args[3]
        hostnameForClients = args[2]
    }.start()
}