/*
 * Licensed to STRATIO (C) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  The STRATIO (C) licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.stratio.meta.server

import akka.actor.{ Props, ActorSystem}
import akka.contrib.pattern.ClusterReceptionistExtension
import com.stratio.meta.server.actors.ServerActor
import com.stratio.meta.core.engine.Engine
import com.stratio.meta.server.config.ServerConfig
import scala.annotation.tailrec
import org.apache.log4j.Logger


object MetaApplication extends App {
  val metaServer:MetaServer =new MetaServer
  @tailrec
  private def commandLoop(): Unit = {
    Console.readLine() match {
      case "quit" | "exit" => return
      case _ =>
    }
    commandLoop()
  }


  metaServer.init(null)
  metaServer.start()
  commandLoop()
  metaServer.stop()
  metaServer.destroy()
}


