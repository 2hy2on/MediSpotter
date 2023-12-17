package ddwucom.mobile.medispotter.network

import android.util.Log
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
        val LONGOTUDE_TAG = "longitude"
        val DUTYNAME_TAG = "wgs84Lon"
        val DUTYDIVNAM_TAG = "dutyDivNam"
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
                else -> skip(parser)
            }
        }
        return  Hospital(dutyAddr, dutyDivName, latitude,logitude, name)
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