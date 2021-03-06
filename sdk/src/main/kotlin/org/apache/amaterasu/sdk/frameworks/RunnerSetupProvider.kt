/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.amaterasu.sdk.frameworks

import org.apache.amaterasu.common.dataobjects.ActionData
import org.apache.amaterasu.common.utils.ArtifactUtil
import org.apache.amaterasu.common.utils.FileUtil
import org.apache.amaterasu.common.logging.Logging
import com.uchuhimo.konf.Config

abstract class RunnerSetupProvider : Logging() {

    private val actionFiles = arrayOf("env.yaml", "runtime.yaml", "datasets.yaml")

    abstract val runnerResources: Array<String>

    abstract fun getCommand(jobId: String, actionData: ActionData, env: Config, executorId: String, callbackAddress: String): String

    protected fun getDownloadableActionSrcPath(jobId: String, actionData: ActionData): String {
        return "$jobId/${actionData.name}/${actionData.src}"
    }

    abstract fun getActionUserResources(jobId: String, actionData: ActionData): Array<String>

    fun getActionResources(jobId: String, actionData: ActionData): Array<String> =
            actionFiles.map { f -> "$jobId/${actionData.name}/$f" }.toTypedArray() +
                    getActionUserResources(jobId, actionData)

    abstract fun getActionDependencies(jobId: String, actionData: ActionData): Array<String>

    fun getActionExecutable(jobId: String, actionData: ActionData): String {

        // if the action is artifact based
        return if (actionData.hasArtifact) {

            val util = ArtifactUtil(listOf(actionData.repo), jobId)
            util.getLocalArtifacts(actionData.artifact).first().path

        } else {

            //action src can be URL based, so we first check if it needs to be downloaded
            val fileUtil = FileUtil()

            if (fileUtil.isSupportedUrl(actionData.src)) {
                fileUtil.downloadFile(actionData.src)
            } else {
                 //"repo/src/${actionData.name}/${actionData.src}"
                 "repo/src/${actionData.src}"
            }
        }
    }

    abstract val hasExecutor: Boolean
        get
}