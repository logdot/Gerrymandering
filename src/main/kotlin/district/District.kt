package district

import misc.Color
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.json.JSONObject
import java.io.File

class District(val province: Double, val canton: Double, val district: Double, var pop: Float, var votes: Double, val parties: MutableList<Double>, var color: Color, var memberPixels: Int = 0) {
    val code: String = if (canton < 10) {
        "${province.toString().substringBefore('.')}0${canton.toString().substringBefore('.')}${district.toString().substringBefore('.')}"
    } else {
        "${province.toString().substringBefore('.')}${canton.toString().substringBefore('.')}${district.toString().substringBefore('.')}"
    }

    constructor(color: Color): this(0.0, 0.0, 0.0, 0f, 0.0, mutableListOf(), color)
    constructor(): this(Color(0, 0, 0))
}

fun readExcel(path: String): MutableList<District> {
    val workbook = XSSFWorkbook(path)
    val sheet = workbook.getSheetAt(0)

    val districts = mutableListOf<District>()

    for (row in sheet) {
        if(row.getCell(2) == null) continue
        if(row.getCell(3) == null) continue
        if(row.getCell(26) == null) continue
        if(row.getCell(27) == null) continue
        if(row.getCell(28) == null) continue

        var province: Double = 0.0
        var canton: Double = 0.0
        var district: Double = 0.0
        var pop : Double = 0.0
        var votes : Double = 0.0

        if (row.getCell(26).cellType == CellType.NUMERIC) province = row.getCell(26).numericCellValue
        if (row.getCell(27).cellType == CellType.NUMERIC) canton = row.getCell(27).numericCellValue
        if (row.getCell(28).cellType == CellType.NUMERIC) district = row.getCell(28).numericCellValue
        if (row.getCell(2).cellType == CellType.NUMERIC) pop = row.getCell(2).numericCellValue
        if (row.getCell(3).cellType == CellType.NUMERIC) votes = row.getCell(3).numericCellValue

        val numOfParties = 13
        val startOfParties = 4
        val parties: MutableList<Double> = mutableListOf()
        for (i in 0 until numOfParties) {
            if(row.getCell(startOfParties + i).cellType == CellType.NUMERIC) parties.add(row.getCell(startOfParties + i).numericCellValue)
        }

        districts.add(District(province, canton, district, pop.toFloat(), votes, parties, Color(0, 0, 0)))
    }

    workbook.close()
    return districts
}

fun updateGeoJSON(districts: MutableList<District>) {
    val jo = JSONObject(File("Distritos_de_Costa_Rica.geojson").readText())
    val features = jo.getJSONArray("features")
    for (district in districts) {
        for (i in 0 until features.length()) {
            val properties = features.getJSONObject(i).getJSONObject("properties")

            val codigo = properties.get("CODIGO")
            if (codigo == district.code) {
                properties.put("POP", district.pop)
                properties.put("VOTES", district.votes)
            }
        }
    }

    File("Distritos_de_Costa_Rica2.geojson").writeText(jo.toString())
}

fun readDistricts(path: String): MutableList<District> {
    val districts = readExcel(path)
    updateGeoJSON(districts)
    return districts
}