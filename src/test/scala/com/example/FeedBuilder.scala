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

import org.apache.commons.lang3.RandomStringUtils

object FeedBuilder
{
  def randomPartialVersion: String = {
    RandomStringUtils.randomNumeric(1) + "." + RandomStringUtils.randomNumeric(1)
  }

  def buildReleaseFeed: Iterable[Map[String,String]] =
    Stream.range(0, 20).map(_ => {
      val groupId = ("com.example." + RandomStringUtils.randomAlphabetic(5)).toLowerCase
      Map(
        "group" -> groupId,
        "groupPath" -> groupId.replace(".", "/"),
        "artifact" -> RandomStringUtils.randomAlphabetic(16).toLowerCase,
        "version" -> randomPartialVersion,
        "repositoryPath" -> "/repository/maven-releases",
        "username" -> "admin",
        "password" -> "admin123",
        "repo" -> "maven-releases"
      )}
    )
}
