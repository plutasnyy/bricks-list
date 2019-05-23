package com.example.brickslist

import android.provider.DocumentsContract
import android.sax.Element
import org.w3c.dom.Document
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

fun writeXml() {
    val docBuilder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val doc: Document = docBuilder.newDocument()

    val rootElement = doc.createElement("person")
    rootElement.setAttribute("person-id", "1001")

    val lastName = doc.createElement("last-name")
    lastName.appendChild(doc.createTextNode("Doe"))
    rootElement.appendChild(lastName)

    val firstName = doc.createElement("first-name")
    firstName.appendChild(doc.createTextNode("John"))
    rootElement.appendChild(firstName)

    doc.appendChild(rootElement)

    val transformer = TransformerFactory.newInstance().newTransformer()
    transformer.setOutputProperty(OutputKeys.INDENT, "yes")
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")

    val path = this.fileDir
    val outDir = File(path, "Output")
    outDir.mkdir()

    val file = File(outDir, "text.xml")
    transformer.transform(DOMSource(doc), StreamResult(file))
}