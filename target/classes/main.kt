import district.District
import district.readDistricts
import misc.Color
import misc.Point
import processing.core.PApplet
import kotlin.random.Random
import kotlinx.coroutines.runBlocking
import misc.Map
import kotlin.concurrent.thread
import kotlin.math.pow
import kotlin.math.sqrt

const val size = 400
const val scale = 1

var districts: MutableList<District> = mutableListOf()
var map: Map = Map(size)
val points: MutableList<Point> = mutableListOf()
val popCenters = mutableListOf<Point>()

class GerrymanderingDraw : PApplet() {
    var drawMode: Int = 0

    companion object Factory {
        fun run() {
            val art = GerrymanderingDraw()
            art.runSketch()
        }
    }

    override fun settings() {
        size(size*scale, size*scale)
    }

    override fun setup() {
        frameRate(15f)
    }

    override fun mousePressed() {
        if(mouseButton == LEFT) {
            drawMode++
        } else {
            drawMode--
        }
    }

    override fun draw() {
        background(255)

        loadPixels()
        for (i in pixels.indices) {
            var color = Color(0, 255, 0)
            if (map[i].district != null) {
                when {
                    drawMode % 3 == 0 -> {
                        color = map[i].district!!.color // District coloration
                    }
                    drawMode % 3 == 1 -> {
                        val ratio = map[i].pop/400*255
                        color = Color(ratio.toInt(), ratio.toInt(), ratio.toInt()) //Pop coloration
                    }
                    drawMode % 3 == 2 -> {
                        var highestPop = 0f
                        for (district in districts) {
                            if (district.pop > highestPop) highestPop = district.pop
                        }
                        val ratio = map[i].district!!.pop/highestPop*255
                        color = Color(ratio.toInt(), ratio.toInt(), ratio.toInt()) //District Pop coloration
                    }
                }
            }

            pixels[i] = color(color.r, color.g, color.b)

            color = map[i].district!!.color

            // Paint border
            //if (map[i].border) pixels[i] = color(color.r - 50, color.g - 50 ,color.b - 50) // Darken color
            //if (map[i].border) pixels[i] = color(0, 0 ,0) // Black
            if (map[i].border) pixels[i] = color(color.r, color.g ,color.b) // Border color
        }
        updatePixels()

        for (point in popCenters) ellipse(point.x, point.y, 5f, 5f)
    }
}

fun main() {start()}

fun start() = runBlocking {
    println("Hello!")
    println(System.getProperty("user.dir"))

    // Add actual districts
    districts = readDistricts("./repoblaccenso2011-11.xlsx")

    // Add test districts
    //for (i in 0 until 3) {
    //    districts.add(District(1.0, 1.0, 1.0, 0f, 0.0, mutableListOf(), color = (Color(100, 100, 100))))
    //}

    // Create 'Population centers'
    for (i in 0 until 4) {
        popCenters.add(Point(Random.nextFloat() * size, Random.nextFloat() * size, District()))
    }

    // Calculate pixel pop
    for (i in map.indices) {
        var shortestDistance = 99999999999999999999f
        for (point in popCenters) {
            val x = i % size
            val y = i / size

            val distance = sqrt((x - point.x).pow(2) + (y - point.y).pow(2))
            if (distance < shortestDistance) shortestDistance = distance
        }
        val invertedDistance = size/2
        val pop = (invertedDistance - shortestDistance) * 3

        if (pop <= 10) {
            map[i].pop = 10f
        } else {
            map[i].pop = pop
        }
    }

    // Create districts
    for (district in districts) {
        val col = Color(Random.nextInt(100,255), Random.nextInt(100,255), Random.nextInt(100,255))
        district.color = col

        val point = Point(Random.nextFloat() * size, Random.nextFloat() * size, district)
        points.add(point)
    }

    map.voronoyTessellation(points)
    map.generateBorder()

    // Generate district pop
    for (pixel in map) {
        pixel.district!!.pop += pixel.pop
    }

    thread {
        GerrymanderingDraw.run()
    }

    println("Finished launching window")

    while (true) {
        val originalStandardDev = standardDev()
        val originalAAtP = AverageAtP()

        map.flipRandomPixel()

        val AAtP = AverageAtP()

        val baseChance = 0.25f // Original 0.25f
        var chance = baseChance
        //chance += sigmoid(originalStandardDev-standardDev(), -300f, 300f) // Improvement on population standard deviation
        chance += sigmoid(originalStandardDev-standardDev(), -2f, 2f)

        chance += sigmoid((AAtP - originalAAtP) * -1, -40f, 40f) // Improvement on higher Area over Perimeter

        //println("StdDev: ${sigmoid(originalStandardDev-standardDev(), -300f, 300f)}")
        //println("StdDev: ${sigmoid(originalStandardDev-standardDev(), -2f, 2f)}")
        //println("StdDev: ${originalStandardDev-standardDev()}")
        //println("AAtP: ${sigmoid(AAtP, 1f, 30f)}")
        println((AAtP - originalAAtP) * 100000)
        println("Chance: $chance")

        if (normalize(Random.nextFloat(), 0f, 1f, -1f, 1f + baseChance) < chance) continue
        try {
            map.undoFlip()
        } catch (e: IndexOutOfBoundsException) {
            println("Tried undoing a flip before one was ever excecuted")
            continue
        }
    }
}

fun standardDev(): Float {
    var dividend = 0.0
    val divisor = districts.size

    // Calculate mean
    var mean = 0f
    for (district in districts) {
        mean += district.pop
    }
    mean /= districts.size

    // Calculate dividend
    for (district in districts) {
        dividend += (district.pop - mean).pow(2)
    }

    return sqrt(dividend/divisor).toFloat()
}

fun sigmoid(x: Float, min: Float, max: Float): Float {
    require(min < max) {"min should be less than max"}

    val value: Float = normalize(x, min, max, -5f, 5f)

    return (1/(1+(Math.E).pow(-value.toDouble()))-0.5).toFloat() * 2
}

fun normalize(value: Float, min: Float = -100f, max: Float = 100f, a: Float = 0f, b: Float = 1f): Float {
    require(min < max) {"min should be less than max"}
    require(a < b) {"a should be less than b"}

    return (b - a) * ((value - min)/(max - min)) + a
}

fun AverageAtP(): Float {
    var averageAtp = 0f
    for (atp in map.areaToPerimeter(districts)) {
        averageAtp += atp
    }
    averageAtp /= districts.size

    return averageAtp
}