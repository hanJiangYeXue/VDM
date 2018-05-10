package com.ingbyr.vdm.views

import com.ingbyr.vdm.models.DownloadTaskConfig
import com.ingbyr.vdm.utils.EngineType
import com.ingbyr.vdm.utils.ProxyType
import com.ingbyr.vdm.utils.VDMConfigUtils
import com.ingbyr.vdm.utils.VDMProxy
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXTextField
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import tornadofx.*
import java.util.*


class CreateDownloadTaskView : View() {
    init {
        messages = ResourceBundle.getBundle("i18n/CreateDownloadTaskView")
    }

    override val root: VBox by fxml("/fxml/CreateDownloadTaskView.fxml")

    private val tfURL: JFXTextField by fxid()
    private val btnMoreSettings: JFXButton by fxid()
    private val btnConfirm: JFXButton by fxid()
    private val labelStoragePath: Label by fxid()
    private val btnChangeStoragePath: JFXButton by fxid()

    private val cu = VDMConfigUtils(app.config)

    // TODO add task to task list view of main view
    init {
        // validation for the url text field
        ValidationContext().addValidator(tfURL, tfURL.textProperty()) {
            if (!it!!.startsWith("http")) error(messages["inputCorrectURL"]) else null
        }

        loadVDMConfig()
        initListeners()
    }

    private fun loadVDMConfig() {
        labelStoragePath.text = cu.load(VDMConfigUtils.STORAGE_PATH)
    }

    private fun initListeners() {
        btnMoreSettings.setOnMouseClicked {
            find(PreferencesView::class).openWindow()
        }

        btnConfirm.setOnMouseClicked {
            val engineType = EngineType.valueOf(cu.load(VDMConfigUtils.ENGINE_TYPE))
            val url = tfURL.text
            val outputPath = cu.load(VDMConfigUtils.STORAGE_PATH)
            val dtc = DownloadTaskConfig(engineType, url, outputPath)

            val proxyType = ProxyType.valueOf(cu.load(VDMConfigUtils.PROXY_TYPE))
            var address = ""
            var port = ""
            when (proxyType) {
                ProxyType.SOCKS5 -> {
                    address = cu.load(VDMConfigUtils.SOCKS5_PROXY_ADDRESS)
                    port = cu.load(VDMConfigUtils.SOCKS5_PROXY_PORT)
                }
                ProxyType.HTTP -> {
                    address = cu.load(VDMConfigUtils.HTTP_PROXY_ADDRESS)
                    port = cu.load(VDMConfigUtils.HTTP_PROXY_PORT)
                }
                ProxyType.NONE -> {
                }
            }
            val vp = VDMProxy(proxyType, address, port)

            find<MediaFormatsView>(mapOf("dtc" to dtc, "vp" to vp)).openWindow()
            this.close()
        }

        btnChangeStoragePath.setOnMouseClicked {
            val file = DirectoryChooser().showDialog(primaryStage)
            file?.apply {
                val newPath = this.absoluteFile.toString()
                app.config[VDMConfigUtils.STORAGE_PATH] = newPath
                labelStoragePath.text = newPath
                cu.saveToConfigFile()
            }
        }
    }
}