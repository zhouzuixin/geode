package demo/*
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
import org.apache.geode.distributed.ConfigurationProperties
import org.apache.geode.distributed.LocatorLauncher

fun main(args: Array<String>) {
    val build = LocatorLauncher.Builder().apply {
        port = args[0].toInt()
        set(ConfigurationProperties.NAME, args[1])
        set(ConfigurationProperties.STATISTIC_SAMPLING_ENABLED, "true")
        set(ConfigurationProperties.ENABLE_TIME_STATISTICS, "true")
        set(ConfigurationProperties.JMX_MANAGER, "false")
        set(ConfigurationProperties.JMX_MANAGER_START, "false")
        set(ConfigurationProperties.ENABLE_CLUSTER_CONFIGURATION, "false")
        hostnameForClients = args[2]
        set(ConfigurationProperties.BIND_ADDRESS, args[3])
        set(ConfigurationProperties.LOCATORS, args[4])
        set(ConfigurationProperties.ENABLE_NETWORK_PARTITION_DETECTION, "false")
    }.build()

    build.start()

    while (true) {
        Thread.sleep(5000)
    }
}