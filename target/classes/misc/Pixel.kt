package misc

import district.District

class Pixel(
    var pop: Float,
    var district: District?,
    var border: Boolean
) {

    constructor() : this(0f, null, false)
    constructor(district: District, border: Boolean) : this(0f, district, border)
}