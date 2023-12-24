package ddwucom.mobile.medispotter.network

import android.util.Xml
import ddwucom.mobile.medispotter.data.Hospital
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

class HospitalParser {
    private  val ns : String? = null

    companion object {
        val FAULT_RESULT = "faultResult"    // OpenAPI 결과에 오류가 있을 때에 생성하는 정보를 위해 지정
        val ITEMS_TAG = "items"
        val ITEM_TAG = "item"
        val ADDRESS_TAG = "dutyAddr"
        val LATITUDE_TAG = "wgs84Lat"
        val LONGOTUDE_TAG = "wgs84Lon"
        val DUTYNAME_TAG = "dutyName"
        val DUTYDIVNAM_TAG = "dutyDivNam"
        val DUTYINF = "dutyInf"
        val DUTYTEL = "dutyTel1"

        val TIME1C = "dutyTime1c"
        val TIME1S ="dutyTime1s"
        val TIME2C ="dutyTime2c"
        val TIME2S ="dutyTime2s"
        val TIME3C ="dutyTime3c"
        val TIME3S="dutyTime3s"
        val TIME4C="dutyTime4c"
        val TIME4S="dutyTime4s"
        val TIME5C ="dutyTime5c"
        val TIME5S="dutyTime5s"
        val TIME6C ="dutyTime6c"
        val TIME6S="dutyTime6s"
        val TIME7C="dutyTime7c"
        val TIME7S="dutyTime7s"
        val TIME8C="dutyTime8c"
        val TIME8S="dutyTime8s"
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream?): List<Hospital> {

        inputStream.use {inputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            //Log.d("여기 parse", inputStream.toString())
            /*Parser 의 동작 정의, next() 호출 전 반드시 호출 필요*/
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)

            /* Paring 대상이 되는 inputStream 설정 */
            parser.setInput(inputStream, null)

            /*Parsing 대상 태그의 상위 태그까지 이동*/
            while (parser.name != ITEMS_TAG) {
                parser.next()
            }
            return readItem(parser)
        }
    }


    @Throws(XmlPullParserException::class, IOException::class)
    private fun readItem(parser: XmlPullParser): List<Hospital>{
        val hospital = mutableListOf<Hospital>()

        parser.require(XmlPullParser.START_TAG, ns, ITEMS_TAG)
        while(parser.next() != XmlPullParser.END_TAG){
            if(parser.eventType != XmlPullParser.START_TAG){
                continue
            }
            if(parser.name == ITEM_TAG){
                hospital.add(readHospital(parser))
            }else{
                skip(parser)
            }
        }
        return hospital
    }

    private fun readHospital(parser:XmlPullParser): Hospital{
        parser.require(XmlPullParser.START_TAG, ns, ITEM_TAG)
        var dutyAddr: String? = null
        var dutyDivName: String? = null
        var latitude: String? = null
        var logitude: String? = null
        var name: String?=null
        var dutyInfo: String?= null
        var dutyTel: String? = null
        var dutyTime1c: String?= null
        var dutyTime1s: String?= null
        var dutyTime2c: String?= null
        var dutyTime2s: String?= null
        var dutyTime3c: String?= null
        var dutyTime3s: String?= null
        var dutyTime4c: String?= null
        var dutyTime4s: String?= null
        var dutyTime5c: String?= null
        var dutyTime5s: String?= null
        var dutyTime6c: String?= null
        var dutyTime6s: String?= null
        var dutyTime7c: String?= null
        var dutyTime7s: String?= null
        var dutyTime8c: String?= null
        var dutyTime8s: String?= null

        while (parser.next() != XmlPullParser.END_TAG){
            if(parser.eventType != XmlPullParser.START_TAG){
                continue
            }
            when(parser.name){
                ADDRESS_TAG-> dutyAddr = readTextInTag(parser, ADDRESS_TAG)
                DUTYNAME_TAG-> name = readTextInTag(parser, DUTYNAME_TAG)
                LATITUDE_TAG-> latitude = readTextInTag(parser, LATITUDE_TAG)
                LONGOTUDE_TAG -> logitude = readTextInTag(parser, LONGOTUDE_TAG)
                DUTYDIVNAM_TAG -> dutyDivName = readTextInTag(parser, DUTYDIVNAM_TAG)
                DUTYINF -> dutyInfo = readTextInTag(parser, DUTYINF)
                DUTYTEL -> dutyTel = readTextInTag(parser, DUTYTEL)
                TIME1C-> dutyTime1c = readTextInTag(parser, TIME1C)
                TIME1S-> dutyTime1s = readTextInTag(parser, TIME1S)
                TIME2C -> dutyTime2c = readTextInTag(parser, TIME2C)
                TIME2S -> dutyTime2s = readTextInTag(parser, TIME2S)
                TIME3C -> dutyTime3c = readTextInTag(parser, TIME3C)
                TIME3S -> dutyTime3s = readTextInTag(parser, TIME3S)
                TIME4C -> dutyTime4c = readTextInTag(parser, TIME4C)
                TIME4S -> dutyTime4s = readTextInTag(parser, TIME4S)
                TIME5C -> dutyTime5c = readTextInTag(parser, TIME5C)
                TIME5S -> dutyTime5s= readTextInTag(parser, TIME5S)
                TIME6C -> dutyTime6c = readTextInTag(parser, TIME6C)
                TIME6S -> dutyTime6s = readTextInTag(parser, TIME6S)
                TIME7C -> dutyTime7c = readTextInTag(parser, TIME7C)
                TIME7S -> dutyTime7s = readTextInTag(parser, TIME7S)
                TIME8C -> dutyTime8c = readTextInTag(parser, TIME8C)
                TIME8C -> dutyTime8s = readTextInTag(parser, TIME8S)

                else -> skip(parser)
            }
        }
        return  Hospital(null,dutyAddr, dutyDivName, latitude,logitude, name, dutyInfo, dutyTel,
            dutyTime1c, dutyTime1s, dutyTime2c, dutyTime2s, dutyTime3c, dutyTime3s, dutyTime4c, dutyTime4s,
            dutyTime5c, dutyTime5s, dutyTime6c, dutyTime6s,dutyTime7c, dutyTime7s,
            dutyTime8c, dutyTime8s, null, null, 0)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTextInTag(parser:XmlPullParser, tag: String): String{
        parser.require(XmlPullParser.START_TAG, ns, tag)
        var text = ""
        if (parser.next() == XmlPullParser.TEXT) {
            text = parser.text
            parser.nextTag()
        }
        parser.require(XmlPullParser.END_TAG, ns, tag)
        return text
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}