package com.example.plugin

import com.android.builder.model.SigningConfig
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class JiaguTask extends DefaultTask{

    Jiagu jiagu
    SigningConfig signingConfig
    File apk

    JiaguTask(){
        // 设置分组
        group = "jiagu"
    }

    @TaskAction
    def run() {
        // 调用命令行工具
        println "开始执行======" + jiagu.jiaguTools
        project.exec {
            // java -jar jiagu.jar -login user password
            it.commandLine("java","-jar",jiagu.jiaguTools,"-login",jiagu.userName,jiagu.password)
        }

        println "签名======"
        // 如果有证书
        if (signingConfig){
            project.exec {
                // java -jar jiagu.jar -importsign
                it.commandLine("java","-jar",jiagu.jiaguTools,"-importsign",signingConfig.storeFile.absolutePath,signingConfig.storePassword,
                        signingConfig.keyAlias,signingConfig.keyPassword)
            }
        }

        println "加固======"
        project.exec {
            // java -jar jiagu.jar -jiagu
            it.commandLine("java","-jar",jiagu.jiaguTools,"-jiagu",apk.absolutePath,apk.parent,"-autosign")
        }
    }
}