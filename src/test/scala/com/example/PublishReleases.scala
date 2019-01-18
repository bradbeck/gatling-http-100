/*
 * Copyright (c) 2019-present Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.example

import java.util.concurrent.BlockingDeque

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder

object PublishReleases
{
  val jarSize: Int = 131072

  val gavs: Iterable[Map[String, String]] = FeedBuilder.buildReleaseFeed

  def release(queue: BlockingDeque[Map[String, String]]): ChainBuilder = {
    feed(Stream.from(0).flatMap(i => gavs.map(_ + ("index" -> i))).toIterator)
        .exec(MavenClient.publishRelease(jarSize))
        .exec(session => {
          queue.addLast(
            Map(
              "username" -> session("username").as[String],
              "password" -> session("password").as[String],
              "group" -> session("group").as[String],
              "groupPath" -> session("groupPath").as[String],
              "artifact" -> session("artifact").as[String],
              "version" -> session("version").as[String],
              "index" -> session("index").as[String],
              "repo" -> session("repo").as[String]
            )
          )
          session}
        )
  }
}
