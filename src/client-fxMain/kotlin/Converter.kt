
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import kotlinx.serialization.modules.SerializersModule
import penta.SerialNotation

class Converter {

}

fun main() {
    val unquotedJsonString = "[{type:penta.SerialNotation.InitGame,players:[square,triangle,cross]},{type:penta.SerialNotation.SwapOwnPiece,player:triangle,piece:p11,otherPiece:p10,from:B,to:A,setGrey:false},{type:penta.SerialNotation.SwapOwnPiece,player:cross,piece:p24,otherPiece:p23,from:E,to:D,setGrey:false},{type:penta.SerialNotation.MovePlayer,player:square,piece:p02,from:C,to:e,setBlack:true,setGrey:false},{type:penta.SerialNotation.SetBlack,from:e,to:B-2-C},{type:penta.SerialNotation.SwapOwnPiece,player:triangle,piece:p14,otherPiece:p13,from:E,to:D,setGrey:false},{type:penta.SerialNotation.SwapOwnPiece,player:cross,piece:p20,otherPiece:p21,from:A,to:B,setGrey:false},{type:penta.SerialNotation.MovePlayer,player:square,piece:p00,from:A,to:c,setBlack:true,setGrey:false},{type:penta.SerialNotation.SetBlack,from:c,to:E-2-A},{type:penta.SerialNotation.SwapOwnPiece,player:triangle,piece:p14,otherPiece:p12,from:D,to:C,setGrey:false},{type:penta.SerialNotation.MovePlayer,player:cross,piece:p24,from:D,to:a,setBlack:true,setGrey:false},{type:penta.SerialNotation.SetBlack,from:a,to:D-4-b},{type:penta.SerialNotation.MovePlayer,player:square,piece:p00,from:c,to:b,setBlack:true,setGrey:false},{type:penta.SerialNotation.SetBlack,from:b,to:b-1-c},{type:penta.SerialNotation.SwapHostilePieces,player:triangle,otherPlayer:square,piece:p14,otherPiece:p02,from:C,to:e,setGrey:true},{type:penta.SerialNotation.SetGrey,from:null,to:e},{type:penta.SerialNotation.MovePlayer,player:cross,piece:p24,from:a,to:e,setBlack:false,setGrey:true},{type:penta.SerialNotation.SetGrey,from:null,to:e},{type:penta.SerialNotation.MovePlayer,player:square,piece:p00,from:b,to:a,setBlack:false,setGrey:true},{type:penta.SerialNotation.SetGrey,from:null,to:a},{type:penta.SerialNotation.MovePlayer,player:triangle,piece:p13,from:E,to:d,setBlack:true,setGrey:true},{type:penta.SerialNotation.SetBlack,from:d,to:c-1-d},{type:penta.SerialNotation.SetGrey,from:null,to:c-2-d},{type:penta.SerialNotation.SwapOwnPiece,player:cross,piece:p23,otherPiece:p21,from:E,to:A,setGrey:false},{type:penta.SerialNotation.MovePlayer,player:square,piece:p02,from:C,to:a,setBlack:false,setGrey:false},{type:penta.SerialNotation.MovePlayer,player:triangle,piece:p11,from:A,to:b-1-c,setBlack:true,setGrey:false},{type:penta.SerialNotation.SetBlack,from:b-1-c,to:b-2-c},{type:penta.SerialNotation.MovePlayer,player:cross,piece:p21,from:E,to:b,setBlack:false,setGrey:true},{type:penta.SerialNotation.SetGrey,from:null,to:D-4-a},{type:penta.SerialNotation.SwapOwnPiece,player:square,piece:p02,otherPiece:p04,from:a,to:E,setGrey:false},{type:penta.SerialNotation.MovePlayer,player:triangle,piece:p11,from:b-1-c,to:b,setBlack:false,setGrey:true},{type:penta.SerialNotation.SetGrey,from:null,to:c},{type:penta.SerialNotation.MovePlayer,player:cross,piece:p23,from:A,to:d,setBlack:false,setGrey:true},{type:penta.SerialNotation.SetGrey,from:null,to:A-6-c},{type:penta.SerialNotation.MovePlayer,player:square,piece:p04,from:a,to:e,setBlack:false,setGrey:true},{type:penta.SerialNotation.SetGrey,from:null,to:d}]"

    val unquotedJson = Json(JsonConfiguration(unquoted = true), context = SerializersModule {
        SerialNotation.install(this)
    })

    val json = Json(JsonConfiguration(unquoted = false, allowStructuredMapKeys = true, prettyPrint = true, classDiscriminator = "type"), context = SerializersModule {
        SerialNotation.install(this)
    })

    println("test")
//    "/test.json".asResource {
        val list = unquotedJson.parse(SerialNotation.serializer().list, unquotedJsonString)
        val jsonString = json.stringify(SerialNotation.serializer().list, list)
        println(jsonString)
//    }
}

fun String.asResource(work: (String) -> Unit) {
    val content = Converter::class.java.classLoader.getResource(this).readText()
    work(content)
}

