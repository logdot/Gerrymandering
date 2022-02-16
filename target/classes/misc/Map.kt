package misc

import com.google.gson.Gson
import district.District
import processing.core.PApplet
import kotlin.math.pow
import kotlin.random.Random

class Map (val width: Int) {
    var pixels: MutableList<Pixel> = MutableList(width*width) {Pixel()}
    var borders: MutableList<Pixel> = mutableListOf()

    private var lastPixelFlipped = Pixel()
    private var lastDistrictFlipped = District()

    val indices: IntRange
    get() {return pixels.indices}

    val size: Int
    get() {return pixels.size}

    init {
        require(width > 0)
    }

    fun voronoyTessellation(points: MutableList<Point>) {
        for (i in pixels.indices) {
            val x = i % width
            val y = i / width

            var shortestPoint: Point? = null
            for (point in points) {
                shortestPoint = shortestPoint ?: point

                val distance = PApplet.sqrt((point.x - x).pow(2) + (point.y - y).pow(2))
                val shortestDistance = PApplet.sqrt((shortestPoint.x - x).pow(2) + (shortestPoint.y - y).pow(2))

                if (distance <= shortestDistance) shortestPoint = point
            }

            pixels[i].district = shortestPoint!!.district
            pixels[i].district!!.memberPixels++
        }
    }

    fun generateBorder() {
        for (pixel in pixels) {
            checkBorder(pixel, false)
        }
    }
    private fun checkBorder(pixel: Pixel, doRecurse: Boolean = true) {
        val originalBorderState = pixel.border

        if (pixel.border) removeBorder(pixel)

        val cardinals = getCardinals(pixel)
        for (cardinal in cardinals) {
            if (cardinal == null) { // Are we on the edge of the map?
                addBorder(pixel)
                break
            }
            if (pixel.district != cardinal!!.district) {
                addBorder(pixel)
                break
            }
        }

        if (!doRecurse) return
        if (originalBorderState != pixel.border) {
            for (neighbour in getNeighbours(pixel)) neighbour?.let{checkBorder(neighbour)}
        }
    }
    private fun addBorder(pixel: Pixel) {
        pixel.border = true
        borders.add(pixel)
    }
    private fun removeBorder(pixel: Pixel) {
        pixel.border = false
        borders.remove(pixel)
    }

    private fun splitCheck(pixel: Pixel): Boolean {
        val cardinals = getCardinals(pixel)
        val neighbours = getNeighbours(pixel)

        val district = pixel.district

        // Splitting up and down
        // if top and bot same district, but left and right not same district
        if (district == cardinals[0]?.district && district == cardinals[2]?.district) {
            if (pixel.district != cardinals[1]?.district && pixel.district != cardinals[3]?.district) {
                return true
            }
        }
        // if left and right same district, but top and down not same district
        else if (district == cardinals[1]?.district && district == cardinals[3]?.district) {
            if (pixel.district != cardinals[0]?.district && pixel.district != cardinals[2]?.district) {
                return true
            }
        }

        // Splitting on a L formation
        // if top and right friendly and all others negative
        if (neighbours[1]?.district == district && neighbours[3]?.district == district) {
            var enemies = 0
            for (neighbour in neighbours) {
                if (neighbour?.district != district) enemies++
                return (enemies == 6)
            }
        }
        // if right and bot friendly and all others negative
        else if (neighbours[3]?.district == district && neighbours[5]?.district == district) {
            var enemies = 0
            for (neighbour in neighbours) {
                if (neighbour?.district != district) enemies++
                return (enemies == 6)
            }
        }
        // if bot and left friendly and all others negative
        else if (neighbours[5]?.district == district && neighbours[7]?.district == district) {
            var enemies = 0
            for (neighbour in neighbours) {
                if (neighbour?.district != district) enemies++
                return (enemies == 6)
            }
        }
        // if left and top friendly and all others negative
        else if (neighbours[7]?.district == district && neighbours[1]?.district == district) {
            var enemies = 0
            for (neighbour in neighbours) {
                if (neighbour?.district != district) enemies++
                return (enemies == 6)
            }
        }

        return false
    }

    fun flipRandomPixel() {
        val index = Random.nextInt(borders.size)
        val pixel = borders[index]

        val cardinals = getCardinals(pixel)
        val borders = mutableListOf<Pixel>()
        for (cardinal in cardinals) {
            if (cardinal?.district != pixel.district) cardinal?.let{borders.add(cardinal)}
        }

        // The more borders we have the more we want to flip it
        val flipChance = borders.size * 0.25 // 0.10f before
        if (Random.nextFloat() < flipChance) {
            val border = borders[Random.nextInt(borders.size)] // Get a random element from borders
            flipPixel(pixel, border)
        }
    }

    fun flipPixel(pixel: Pixel, copyFrom: Pixel) {
        // Track last flip
        lastPixelFlipped = pixel
        lastDistrictFlipped = pixel.district!!

        val cardinals = getCardinals(pixel)
        if (splitCheck(pixel)) return

        for (cardinal in cardinals) {
            cardinal?.border = true
        }
        // Update district pop
        pixel.district!!.pop -= pixel.pop
        pixel.district!!.memberPixels--
        copyFrom.district!!.pop += pixel.pop
        copyFrom.district!!.memberPixels++

        pixel.district = copyFrom.district
        for (neighbour in getNeighbours(pixel)) neighbour?.let{checkBorder(neighbour)}


    }
    private fun flipPixel(pixel: Pixel, districtData: District) {
        val cardinals = getCardinals(pixel)
        splitCheck(pixel)

        for (cardinal in cardinals) {
            cardinal?.border = true
        }
        // Update district pop
        pixel.district!!.pop -= pixel.pop
        pixel.district!!.memberPixels--
        districtData.pop += pixel.pop
        districtData.memberPixels++

        pixel.district = districtData
        for (neighbour in getNeighbours(pixel)) neighbour?.let{checkBorder(neighbour)}

        // Track last flip
        lastPixelFlipped = pixel
        lastDistrictFlipped = districtData
    }
    fun undoFlip() {
        flipPixel(lastPixelFlipped, lastDistrictFlipped)
    }

    fun areaToPerimeter(districts: MutableList<District>) = sequence {
        for (district in districts) {
            val perimeter = (borders.filter { it.district == district}).size //TODO: SLOW!!!
            val area = district.memberPixels

            yield(area/perimeter.toFloat())
        }
    }

    fun getNeighbours(index: Int): MutableList<Pixel?> {
        val top = index < width
        val right = index % width == width - 1
        val bot = index >= width * width - width
        val left = index % width == 0

        val borders: MutableList<Pixel?> = mutableListOf()

        if (!top and !left) borders.add(pixels[index - width - 1]) else borders.add(null)
        if (!top) borders.add(pixels[index - width]) else borders.add(null)
        if (!top and !right) borders.add(pixels[index - width + 1]) else borders.add(null)
        if (!right) borders.add(pixels[index + 1]) else borders.add(null)
        if (!bot and !right) borders.add(pixels[index + width + 1]) else borders.add(null)
        if (!bot) borders.add(pixels[index + width]) else borders.add(null)
        if (!bot and !left) borders.add(pixels[index + width - 1]) else borders.add(null)
        if (!left) borders.add(pixels[index - 1]) else borders.add(null)

        return borders
    }
    fun getNeighbours(pixel: Pixel): MutableList<Pixel?> {
        val index = pixels.indexOf(pixel)
        return getNeighbours(index)
    }

    fun getCardinals(index: Int): MutableList<Pixel?> {
        val n = getNeighbours(index)

        return mutableListOf(n[1], n[3], n[5], n[7])
    }
    fun getCardinals(pixel: Pixel): MutableList<Pixel?> {
        val n = getNeighbours(pixel)

        return mutableListOf(n[1], n[3], n[5], n[7])
    }

    // Operators
    operator fun get(i: Int): Pixel {
        return pixels[i]
    }

    operator fun iterator(): MutableIterator<Pixel> {
        return pixels.iterator()
    }

    operator fun set(i: Int, value: Pixel) {
        pixels[i] = value
    }
}