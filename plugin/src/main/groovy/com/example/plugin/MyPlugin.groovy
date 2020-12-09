package com.example.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.builder.model.SigningConfig
import org.gradle.api.Plugin
import org.gradle.api.Project

class MyPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        Jiagu jiagu = project.extensions.create("jiagu",Jiagu)
        // 在gradle配置完成之后回调
        project.afterEvaluate {
            println jiagu.userName + ":" + jiagu.password

            AppExtension android = project.extensions.android
            // applicationVariants代表app 的变体
            android.applicationVariants.all {
                ApplicationVariant variant ->
                    // 获取对应变体签名配置
                    SigningConfig signingConfig = variant.signingConfig
                    // 遍历apk的目录
                    variant.outputs.all {
                        BaseVariantOutput output ->
                            // apk 文件
                            File apk = output.outputFile
                            // 创建加固任务，baseName是指debug还是release
                            // capitalize()使首字母大写   最后创建的结果是jiaguDebug
                            JiaguTask jiaguTask = project.tasks.create("jiagu${variant.baseName.capitalize()}",JiaguTask)
                            jiaguTask.jiagu = jiagu
                            jiaguTask.signingConfig = signingConfig
                            jiaguTask.apk = apk
                    }
            }
        }
    }
}