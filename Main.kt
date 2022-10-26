package cryptography

import java.awt.Color
import java.io.File
import javax.imageio.ImageIO

fun main() {
    while(true) {
        println("Task (hide, show, exit):")
        when (val input = readln()) {
            "exit" -> { println("Bye!")
                break }
            "hide" -> { hide() }
            "show" -> { show() }
            else -> println("Wrong task: $input")
        }
    }
}

fun hide() {
    println("Input image file:")
    val inputImage = readln()
    println("Output image file:")
    val outputImage = readln()
    println("Message to hide:")
    val message = readln()
    println("Password:")
    val password = readln()

    try {
        val image = ImageIO.read(File(inputImage))
        val message = messageToBits(message, password)

        //getting bits from image blue channel
        val lsbits = mutableListOf<Int>()
        for (i in 0 until image.height) {
            for (k in 0 until image.width) {
                val pixelColor = Color(image.getRGB(k,i))
                lsbits += pixelColor.blue
            }
        }
        //validating image and message sizes
        if (message.size > lsbits.size) { println("The input image is not large enough to hold this message.")}

        //changing bits in image blue channel according to message bits
        for (i in message.indices) {
            if (lsbits[i] % 2 == 0) { if (message[i] == 1) lsbits[i] = lsbits[i] or 1 }
            else { if (message[i] == 0) lsbits[i] = (lsbits[i] xor 1) }
        }
        //adding changed bits to image
        for (i in 0 until image.height) {
            for (k in 0 until image.width) {
                val pixColor = Color(image.getRGB(k,i))
                image.setRGB(k, i,Color(pixColor.red, pixColor.green, lsbits[i*image.width + k]).rgb)
            }
        }
        //saving image
        ImageIO.write(image, "png", File(outputImage))
        println("Message saved in ${outputImage.split("/").last()} image.")
    } catch (e: Exception) {
        println("Can't read input file!")
    }
}

fun messageToBits(message: String, password: String): List<Int> {
    //getting message as string of bits
    var encodedMessage = ""
    message.toByteArray(Charsets.UTF_8).forEach { encodedMessage += it.toString(2).padStart(8, '0') }

    //getting password as string of bits
    var encodedPassword = ""
    password.toByteArray(Charsets.UTF_8).forEach { encodedPassword += it.toString(2).padStart(8, '0') }

    //getting sized password
    val finalPassword = getSizedPassword(encodedMessage, encodedPassword)

    //encrypting message with password as getting encrypted message as List of bits
    val encryptedMessage = mutableListOf<Int>()
    for (i in encodedMessage.indices) {
        encryptedMessage += encodedMessage[i].toInt() xor finalPassword[i].toInt()
    }

    //marking the end of message with 3 Bytes and returning result
    return encryptedMessage + listOf(
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 1, 1
    )
}

fun getSizedPassword(message: String, password: String) : String {
    var finalPassword = ""
    if (message.length > password.length) {
        val n = message.length / password.length
        val r = message.length % password.length
        repeat(n) {finalPassword += password}
        for (i in 0 until r) {
            finalPassword += password[i]
        }
    }
    return finalPassword
}

fun show() {
    println("Input image file:")
    val image = ImageIO.read(File(readln()))
    println("Password:")
    val password = readln()
    val imageWithMessage = mutableListOf<Int>()

    for (i in 0 until image.height) {
        for (k in 0 until image.width) {
            val pixelColor = Color(image.getRGB(k,i))
            imageWithMessage += (pixelColor.blue).toString(2).takeLast(1).toInt()
        }
    }
    val encryptedMessageFromImage = imageWithMessage.joinToString("").split("000000000000000000000011").first()

    //Getting password in bits array
    val encodedPasswordStr = mutableListOf<String>()
    password.toByteArray(Charsets.UTF_8).forEach { encodedPasswordStr += it.toString(2).padStart(8, '0') }

    val encodedPassword = encodedPasswordStr.joinToString("")

//    getting sized password

    val finalPassword = getSizedPassword(encryptedMessageFromImage, encodedPassword)

    //decrypting message with password
    val decryptedMessageInBits = mutableListOf<Int>()
    var decryptedMessage = ""
    for (i in encryptedMessageFromImage.indices) {
        decryptedMessageInBits += encryptedMessageFromImage[i].toInt() xor finalPassword[i].toInt()
    }
    decryptedMessageInBits.joinToString("").chunked(8).forEach { decryptedMessage += it.toInt(2).toChar() }
    println("Message:")
    println(decryptedMessage)
}
