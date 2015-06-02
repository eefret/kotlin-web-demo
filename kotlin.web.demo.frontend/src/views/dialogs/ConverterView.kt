/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package views.dialogs

import html4k.dom.append
import html4k.js.div
import html4k.js.textArea
import jquery.jq
import providers.ConverterProvider
import utils.KeyCode
import views.CodeMirror
import views.button
import views.find
import views.keydown
import kotlin.browser.document
import kotlin.browser.window


/**
 * Created by Semyon.Atamas on 5/18/2015.
 */

class ConverterView(converterProvider: ConverterProvider) {

    fun open() {
        dialog.open()
        javaEditor.refresh();
        kotlinEditor.refresh();
    }

    private val dialogElement = document.body!!.append.div {
        id = "myPopupForConverterFromJavaToKotlin"
        title = "Convert your java code to kotlin"
    }

    private val leftHalf = dialogElement.append.div {};
    private val javaEditor = CodeMirror.fromTextArea(leftHalf.append.textArea {}, json(
            "lineNumbers" to true,
            "styleActiveLine" to true,
            "matchBrackets" to true,
            "autoCloseBrackets" to true,
            "continueComments" to true,
            "extraKeys" to json(
                    "Shift-Tab" to false,
                    "Ctrl-Alt-L" to "indentAuto",
                    "Ctrl-/" to "toggleComment"
            ),
            "mode" to "text/x-java",
            "tabSize" to 4
    ));

    private val rightHalf = dialogElement.append.div {};
    private val kotlinEditor = CodeMirror.fromTextArea(rightHalf.append.textArea {}, json(
            "lineNumbers" to true,
            "matchBrackets" to true,
            "styleActiveLine" to true,
            "readOnly" to true,
            "mode" to "text/kotlin",
            "tabSize" to 4
    ));

    private val dialog = Dialog(
            dialogElement,
            modal = true,
            autoOpen = false,
            resizable = true,
            width = 700,
            height = 700,
            resizeStop = {
                javaEditor.refresh();
                kotlinEditor.refresh();
            },
            buttons = arrayOf(Button(
                    text = "Convert to Kotlin",
                    click = {
                        getDialogButton(dialogElement, 1).button("disable");
                        converterProvider.convert(
                                javaEditor.getValue(),
                                onSuccess = { text ->
                                    kotlinEditor.setValue(text);
                                    var lineCount: Int = kotlinEditor.lineCount();
                                    kotlinEditor.operation({
                                        for (i in 0..lineCount) kotlinEditor.indentLine(i);
                                    });
                                },
                                onFail = { errors ->
                                    for (error in errors) {
                                        // todo
                                        window.alert(error.exception);
                                    }
                                },
                                onComplete = {
                                    getDialogButton(dialogElement, 1).button("enable");
                                }
                        );
                    }
            ))
    )


    init {
        jq(dialogElement).keydown({ event ->
            if (event.keyCode == KeyCode.ENTER.code && (event.ctrlKey || event.metaKey)) {
                jq(dialogElement).parent().find("button:eq(1):enabled").click();
            }
            event.stopPropagation();
        });
    }
}