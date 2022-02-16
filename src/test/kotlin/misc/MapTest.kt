package misc

import district.District
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import kotlin.math.exp

internal class MapTest {
    @Nested
    inner class Constructor {
        @Test
        fun `Error when width 0`() {
            assertThrows(IllegalArgumentException::class.java) { Map(0) }
        }
    }

    @Nested
    inner class GetNeighbours {
        @Test
        fun `Corner Check`() {
            val map = Map(5)

            var neighbours = map.getNeighbours(0)
            assertAll("Top Left",
                { assertEquals(null, neighbours[0], "Top left") },
                { assertEquals(null, neighbours[1], "Top") },
                { assertEquals(null, neighbours[2], "Top right") },
                { assertEquals(map[1], neighbours[3], "Right") },
                { assertEquals(map[6], neighbours[4], "Bottom right") },
                { assertEquals(map[5], neighbours[5], "Bottom") },
                { assertEquals(null, neighbours[6], "Bottom left") },
                { assertEquals(null, neighbours[7], "Left") }
            )

            neighbours = map.getNeighbours(4)
            assertAll("Top Right",
                { assertEquals(null, neighbours[0], "Top left") },
                { assertEquals(null, neighbours[1], "Top") },
                { assertEquals(null, neighbours[2], "Top right") },
                { assertEquals(null, neighbours[3], "Right") },
                { assertEquals(null, neighbours[4], "Bottom right") },
                { assertEquals(map[9], neighbours[5], "Bottom") },
                { assertEquals(map[8], neighbours[6], "Bottom left") },
                { assertEquals(map[3], neighbours[7], "Left") }
            )

            neighbours = map.getNeighbours(20)
            assertAll("Bot Left",
                { assertEquals(null, neighbours[0], "Top left") },
                { assertEquals(map[15], neighbours[1], "Top") },
                { assertEquals(map[16], neighbours[2], "Top right") },
                { assertEquals(map[21], neighbours[3], "Right") },
                { assertEquals(null, neighbours[4], "Bottom right") },
                { assertEquals(null, neighbours[5], "Bottom") },
                { assertEquals(null, neighbours[6], "Bottom left") },
                { assertEquals(null, neighbours[7], "Left") }
            )

            neighbours = map.getNeighbours(24)
            assertAll("Bot Right",
                { assertEquals(map[18], neighbours[0], "Top left") },
                { assertEquals(map[19], neighbours[1], "Top") },
                { assertEquals(null, neighbours[2], "Top right") },
                { assertEquals(null, neighbours[3], "Right") },
                { assertEquals(null, neighbours[4], "Bottom right") },
                { assertEquals(null, neighbours[5], "Bottom") },
                { assertEquals(null, neighbours[6], "Bottom left") },
                { assertEquals(map[23], neighbours[7], "Left") }
            )
        }

        @Test
        fun `(Pixel) is same as (Int)`() {
            val map = Map(5)

            val center = (map.size - 1) / 2 // Only on odd number width maps
            val pixel = map[center]

            assertEquals(map.getNeighbours(center), map.getNeighbours(pixel))
        }

        @Test
        fun `(Int) is same as (Pixel)`() {
            val map = Map(5)

            val center = (map.size - 1) / 2 // Only on odd number width maps
            val pixel = map[center]

            assertEquals(map.getNeighbours(pixel), map.getNeighbours(center))
        }
    }

    @Nested
    inner class FlipPixel {
        // Create test for the flipping of pop?

        @Test
        fun `Assert center flip with half and half map`(){
            // Map Indices
            // 0  1  2  3
            // 4  5  6  7
            // 8  9  10 11
            // 12 13 14 15

            // We will flip 5 and copy from 6

            val left = District(Color(255, 0, 0))
            val right = District(Color(0, 255, 0))

            // Manually create map
            val map = Map(4)
            map[0] = Pixel(left, false)
            map[1] = Pixel(left, true)
            map[2] = Pixel(right, true)
            map[3] = Pixel(right, false)

            map[4] = Pixel(left, false)
            map[5] = Pixel(left, true)
            map[6] = Pixel(right, true)
            map[7] = Pixel(right, false)

            map[8] = Pixel(left, false)
            map[9] = Pixel(left, true)
            map[10] = Pixel(right, true)
            map[11] = Pixel(right, false)

            map[12] = Pixel(left, false)
            map[13] = Pixel(left, true)
            map[14] = Pixel(right, true)
            map[15] = Pixel(right, false)

            val expected = Map(4)
            expected[0] = Pixel(left, false)
            expected[1] = Pixel(left, true)
            expected[2] = Pixel(right, true)
            expected[3] = Pixel(right, false)

            expected[4] = Pixel(left, true)
            expected[5] = Pixel(right, true)
            expected[6] = Pixel(right, false)
            expected[7] = Pixel(right, false)

            expected[8] = Pixel(left, false)
            expected[9] = Pixel(left, true)
            expected[10] = Pixel(right, true)
            expected[11] = Pixel(right, false)

            expected[12] = Pixel(left, false)
            expected[13] = Pixel(left, true)
            expected[14] = Pixel(right, true)
            expected[15] = Pixel(right, false)

            map.flipPixel(map[5], map[6])

            for (i in map.pixels.indices) {
                assertEquals(expected[i].district, map[i].district)
                assertEquals(expected[i].border, map[i].border)
            }
        }

        @Test
        fun `Split check center`() {
            // This should return the map unchanged

            // Map Indices
            // 0  1  2  3
            // 4  5  6  7
            // 8  9  10 11
            // 12 13 14 15

            val splitter = District(Color(255, 0, 0))
            val toBeSplit = District(Color(0, 255, 0))

            // Manually create map
            val map = Map(3)
            map[0] = Pixel(toBeSplit, true)
            map[1] = Pixel(toBeSplit, false)
            map[2] = Pixel(toBeSplit, true)

            map[3] = Pixel(splitter, true)
            map[4] = Pixel(toBeSplit, true)
            map[5] = Pixel(splitter, true)

            map[6] = Pixel(toBeSplit, true)
            map[7] = Pixel(toBeSplit, false)
            map[8] = Pixel(toBeSplit, true)


            val expected = Map(3)
            expected[0] = Pixel(toBeSplit, true)
            expected[1] = Pixel(toBeSplit, false)
            expected[2] = Pixel(toBeSplit, true)

            expected[3] = Pixel(splitter, true)
            expected[4] = Pixel(toBeSplit, true)
            expected[5] = Pixel(splitter, true)

            expected[6] = Pixel(toBeSplit, true)
            expected[7] = Pixel(toBeSplit, false)
            expected[8] = Pixel(toBeSplit, true)

            for (i in map.pixels.indices) {
                assertEquals(expected[i].district, map[i].district)
                assertEquals(expected[i].border, map[i].border)
            }
        }

        @Test
        fun `Split check edge`() {
            // This should return the map unchanged

            // Map Indices
            // 0  1  2  3
            // 4  5  6  7
            // 8  9  10 11
            // 12 13 14 15

            val splitter = District(Color(255, 0, 0))
            val toBeSplit = District(Color(0, 255, 0))

            // Manually create map
            val map = Map(3)
            map[0] = Pixel(toBeSplit, false)
            map[1] = Pixel(toBeSplit, true)
            map[2] = Pixel(toBeSplit, true)

            map[3] = Pixel(toBeSplit, true)
            map[4] = Pixel(splitter, true)
            map[5] = Pixel(splitter, true)

            map[6] = Pixel(toBeSplit, false)
            map[7] = Pixel(toBeSplit, true)
            map[8] = Pixel(toBeSplit, true)


            val expected = Map(3)
            expected[0] = Pixel(toBeSplit, false)
            expected[1] = Pixel(toBeSplit, true)
            expected[2] = Pixel(toBeSplit, true)

            expected[3] = Pixel(toBeSplit, true)
            expected[4] = Pixel(splitter, true)
            expected[5] = Pixel(splitter, true)

            expected[6] = Pixel(toBeSplit, false)
            expected[7] = Pixel(toBeSplit, true)
            expected[8] = Pixel(toBeSplit, true)

            for (i in map.pixels.indices) {
                assertEquals(expected[i].district, map[i].district)
                assertEquals(expected[i].border, map[i].border)
            }
        }
    }

    @Nested
    inner class GenerateBorder {
        @Test
        fun `Vertical check`() {
            // Map Indices
            // 0  1  2  3
            // 4  5  6  7
            // 8  9  10 11
            // 12 13 14 15

            val left = District(Color(255, 0, 0))
            val right = District(Color(0, 255, 0))

            // Manually create map
            val map = Map(4)
            map[0] = Pixel(left, false)
            map[1] = Pixel(left, false)
            map[2] = Pixel(right, false)
            map[3] = Pixel(right, false)

            map[4] = Pixel(left, false)
            map[5] = Pixel(left, false)
            map[6] = Pixel(right, false)
            map[7] = Pixel(right, false)

            map[8] = Pixel(left, false)
            map[9] = Pixel(left, false)
            map[10] = Pixel(right, false)
            map[11] = Pixel(right, false)

            map[12] = Pixel(left, false)
            map[13] = Pixel(left, false)
            map[14] = Pixel(right, false)
            map[15] = Pixel(right, false)

            val expected = Map(4)
            expected[0] = Pixel(left, false)
            expected[1] = Pixel(left, true)
            expected[2] = Pixel(right, true)
            expected[3] = Pixel(right, false)

            expected[4] = Pixel(left, false)
            expected[5] = Pixel(left, true)
            expected[6] = Pixel(right, true)
            expected[7] = Pixel(right, false)

            expected[8] = Pixel(left, false)
            expected[9] = Pixel(left, true)
            expected[10] = Pixel(right, true)
            expected[11] = Pixel(right, false)

            expected[12] = Pixel(left, false)
            expected[13] = Pixel(left, true)
            expected[14] = Pixel(right, true)
            expected[15] = Pixel(right, false)

            map.generateBorder()

            for (i in map.pixels.indices) {
                assertEquals(expected[i].district, map[i].district)
                assertEquals(expected[i].border, map[i].border)
            }
        }

        @Test
        fun `Horizontal check`() {
            // Map Indices
            // 0  1  2  3
            // 4  5  6  7
            // 8  9  10 11
            // 12 13 14 15

            val top = District(Color(255, 0, 0))
            val bot = District(Color(0, 255, 0))

            // Manually create map
            val map = Map(4)
            map[0] = Pixel(top, false)
            map[1] = Pixel(top, false)
            map[2] = Pixel(top, false)
            map[3] = Pixel(top, false)

            map[4] = Pixel(top, false)
            map[5] = Pixel(top, false)
            map[6] = Pixel(top, false)
            map[7] = Pixel(top, false)

            map[8] = Pixel(bot, false)
            map[9] = Pixel(bot, false)
            map[10] = Pixel(bot, false)
            map[11] = Pixel(bot, false)

            map[12] = Pixel(bot, false)
            map[13] = Pixel(bot, false)
            map[14] = Pixel(bot, false)
            map[15] = Pixel(bot, false)

            val expected = Map(4)
            expected[0] = Pixel(top, false)
            expected[1] = Pixel(top, false)
            expected[2] = Pixel(top, false)
            expected[3] = Pixel(top, false)

            expected[4] = Pixel(top, true)
            expected[5] = Pixel(top, true)
            expected[6] = Pixel(top, true)
            expected[7] = Pixel(top, true)

            expected[8] = Pixel(bot, true)
            expected[9] = Pixel(bot, true)
            expected[10] = Pixel(bot, true)
            expected[11] = Pixel(bot, true)

            expected[12] = Pixel(bot, false)
            expected[13] = Pixel(bot, false)
            expected[14] = Pixel(bot, false)
            expected[15] = Pixel(bot, false)

            map.generateBorder()

            for (i in map.pixels.indices) {
                assertEquals(expected[i].district, map[i].district)
                assertEquals(expected[i].border, map[i].border)
            }
        }

        @Test
        fun `Diagonal check`() {
            // Map Indices
            // 0  1  2  3
            // 4  5  6  7
            // 8  9  10 11
            // 12 13 14 15

            val top = District(Color(255, 0, 0))
            val bot = District(Color(0, 255, 0))

            // Manually create map
            val map = Map(4)
            map[0] = Pixel(top, false)
            map[1] = Pixel(top, false)
            map[2] = Pixel(top, false)
            map[3] = Pixel(top, false)

            map[4] = Pixel(bot, false)
            map[5] = Pixel(top, false)
            map[6] = Pixel(top, false)
            map[7] = Pixel(top, false)

            map[8] = Pixel(bot, false)
            map[9] = Pixel(bot, false)
            map[10] = Pixel(top, false)
            map[11] = Pixel(top, false)

            map[12] = Pixel(bot, false)
            map[13] = Pixel(bot, false)
            map[14] = Pixel(bot, false)
            map[15] = Pixel(top, false)

            val expected = Map(4)
            expected[0] = Pixel(top, true)
            expected[1] = Pixel(top, false)
            expected[2] = Pixel(top, false)
            expected[3] = Pixel(top, false)

            expected[4] = Pixel(bot, true)
            expected[5] = Pixel(top, true)
            expected[6] = Pixel(top, false)
            expected[7] = Pixel(top, false)

            expected[8] = Pixel(bot, false)
            expected[9] = Pixel(bot, true)
            expected[10] = Pixel(top, true)
            expected[11] = Pixel(top, false)

            expected[12] = Pixel(bot, false)
            expected[13] = Pixel(bot, false)
            expected[14] = Pixel(bot, true)
            expected[15] = Pixel(top, true)

            map.generateBorder()

            for (i in map.pixels.indices) {
                assertEquals(expected[i].district, map[i].district)
                assertEquals(expected[i].border, map[i].border)
            }
        }
    }

    @Nested
    inner class UndoFlip {
        @Test
        fun `Assert`() {
            // Map Indices
            // 0  1  2  3
            // 4  5  6  7
            // 8  9  10 11
            // 12 13 14 15

            val left = District(Color(255, 0, 0))
            val right = District(Color(0, 255, 0))

            // Manually create map
            val map = Map(4)
            map[0] = Pixel(left, false)
            map[1] = Pixel(left, true)
            map[2] = Pixel(right, true)
            map[3] = Pixel(right, false)

            map[4] = Pixel(left, false)
            map[5] = Pixel(left, true)
            map[6] = Pixel(right, true)
            map[7] = Pixel(right, false)

            map[8] = Pixel(left, false)
            map[9] = Pixel(left, true)
            map[10] = Pixel(right, true)
            map[11] = Pixel(right, false)

            map[12] = Pixel(left, false)
            map[13] = Pixel(left, true)
            map[14] = Pixel(right, true)
            map[15] = Pixel(right, false)

            val expected = Map(4)
            expected[0] = Pixel(left, false)
            expected[1] = Pixel(left, true)
            expected[2] = Pixel(right, true)
            expected[3] = Pixel(right, false)

            expected[4] = Pixel(left, false)
            expected[5] = Pixel(left, true)
            expected[6] = Pixel(right, true)
            expected[7] = Pixel(right, false)

            expected[8] = Pixel(left, false)
            expected[9] = Pixel(left, true)
            expected[10] = Pixel(right, true)
            expected[11] = Pixel(right, false)

            expected[12] = Pixel(left, false)
            expected[13] = Pixel(left, true)
            expected[14] = Pixel(right, true)
            expected[15] = Pixel(right, false)

            map.flipPixel(map[13], map[14])
            map.undoFlip()

            for (i in map.pixels.indices) {
                assertEquals(expected[i].district, map[i].district)
                //assertEquals(expected[i].border, map[i].border)
            }
        }
    }
}